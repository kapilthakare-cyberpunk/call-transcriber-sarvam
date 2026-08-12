#!/usr/bin/env bash
# New call-recording detection + interactive approval gate.
#
# Flow:
#   1. Poll /sdcard/Recordings for new .m4a (find -newer STATE_FILE; /sdcard is
#      FUSE so inotify cannot fire).
#   2. For each new recording, send a Telegram inline-keyboard card to YOU:
#        [ 🎤 Transcribe ]  [ 📝 Transcribe + Summary ]  [ ❌ Skip ]
#      (Sarvam credits are only spent when you tap a Transcribe button.)
#   3. On tap: decode m4a->16kHz mono wav, split into <=30s chunks (Sarvam REST
#      caps at 30s/request), POST to Sarvam Speech-to-Text (saaras:v3), stitch
#      the transcript, classify work vs personal, and deliver results on
#      Telegram. Summary (if requested) runs through a separate free/local LLM
#      so Sarvam credits aren't consumed by summarization.
set -u

# ---- Configuration -----------------------------------------------------------
CONFIG="/root/.config/call-transcriber.conf"
[ -f "$CONFIG" ] && . "$CONFIG"

RECORD_DIR="${RECORD_DIR:-/sdcard/Recordings}"
TRANSCRIPT_DIR="${TRANSCRIPT_DIR:-/sdcard/Recordings/Transcripts}"
STATE_FILE="${STATE_FILE:-/root/.transcriber-state}"
PENDING_FILE="${PENDING_FILE:-/root/.transcriber-pending}"
LOG="${LOG:-/tmp/transcriber-watcher.log}"
POLL_SECS="${POLL_SECS:-60}"
TG_OFFSET_FILE="${TG_OFFSET_FILE:-/root/.telegram-offset}"

WHISPER_BIN="${WHISPER_BIN:-/opt/transcriber/whisper.cpp-master/build/bin/whisper-cli}"
OLLAMA_URL="${OLLAMA_URL:-http://127.0.0.1:11434}"

: "${WORK_KEYWORDS:=invoice payment pay client project deadline report meeting milestone contract vendor quotation order deliverable salary business work appointment office call with client vendor call rate price bill}"
: "${PERSONAL_KEYWORDS:=dinner lunch breakfast food shopping market birthday picnic family home maha sajawat mummy mom papa dad wifey wife husband girlfriend boyfriend weekend party chai khana ghar mela health medical doctor}"

# Telegram (reuse the existing bot token from the telegram skill config)
TELEGRAM_CONFIG="/root/.config/opencode/skills-repos/the-factory/.opencode/skills/telegram/.skill.config"
[ -f "$TELEGRAM_CONFIG" ] && . "$TELEGRAM_CONFIG"

# Sarvam (off)
: "${SARVAM_API_KEY:=}"
: "${SARVAM_MODEL:=saaras:v3}"
: "${SARVAM_MODE:=transcribe}"
: "${SARVAM_LANGUAGE_CODE:=unknown}"
: "${SARVAM_MAX_CHUNK_SECS:=29}"

# Summary LLM (separate provider to spare Sarvam credits)
: "${SUMMARY_PROVIDER:=}"
: "${SUMMARY_API_URL:=}"
: "${SUMMARY_API_KEY:=}"
: "${SUMMARY_MODEL:=}"
: "${LLM_MODEL:=}"

mkdir -p "$TRANSCRIPT_DIR"
touch "$PENDING_FILE"
: "${WATCHER_PIDFILE:=/root/.transcriber-watcher.pid}"
echo $$ > "$WATCHER_PIDFILE"
trap 'rm -f "$WATCHER_PIDFILE"' EXIT
log() { echo "$(date '+%F %T') $*" >> "$LOG"; }

# ---------------------------------------------------------------- helpers -----
token_of() { printf '%s' "$1" | md5sum | cut -c1-12; }

tg_answer() { # callback_query_id, text shown as toast
  curl -s "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/answerCallbackQuery" \
    -d "callback_query_id=$1" --data-urlencode "text=$2" --data-urlencode "show_alert=false" >/dev/null 2>&1 || true
}

tg_send() { # text [reply_markup_json]
  [ -n "${TELEGRAM_BOT_TOKEN:-}" ] && [ -n "${TELEGRAM_CHAT_ID:-}" ] || return 0
  local body=(-d "chat_id=$TELEGRAM_CHAT_ID" -d "parse_mode=Markdown")
  [ -n "${2:-}" ] && body+=(--data-urlencode "reply_markup=$2")
  curl -s "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
    "${body[@]}" --data-urlencode "text=$1" >/dev/null 2>&1 || true
}

tg_edit() { # message_id text [reply_markup_json]
  local body=(-d "chat_id=$TELEGRAM_CHAT_ID" -d "message_id=$1")
  [ -n "${3:-}" ] && body+=(--data-urlencode "reply_markup=$3")
  curl -s "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/editMessageText" \
    "${body[@]}" --data-urlencode "text=$2" >/dev/null 2>&1 || true
}

was_processed() { [ -f "$STATE_FILE" ] && grep -qxF "$1" "$STATE_FILE" 2>/dev/null; }
mark_processed() { echo "$1" >> "$STATE_FILE"; }
remove_pending() { grep -vF "$1|" "$PENDING_FILE" 2>/dev/null > "$PENDING_FILE.tmp" && mv "$PENDING_FILE.tmp" "$PENDING_FILE" || true; }
pending_lookup() { grep -F "$1|" "$PENDING_FILE" 2>/dev/null | head -1; }

keyword_score() {
  local text="$1"; shift; local kw score=0
  for kw in "$@"; do
    case " $text " in
      *" $kw "*|*" ${kw},"*|*" ${kw}."*) score=$((score + 1)) ;;
    esac
  done
  echo "$score"
}

classify_transcript() {
  local text="$1" contact="$2" wscore pscore verdict contact_lc
  wscore=$(keyword_score "$text" $WORK_KEYWORDS)
  pscore=$(keyword_score "$text" $PERSONAL_KEYWORDS)
  contact_lc=$(printf '%s' "$contact" | tr 'A-Z' 'a-z')
  if [ -n "${WORK_CONTACTS:-}" ] && grep -qiE "${WORK_CONTACTS}" <<< "$contact_lc"; then echo "WORK"; return; fi
  if [ -n "${PERSONAL_CONTACTS:-}" ] && grep -qiE "${PERSONAL_CONTACTS}" <<< "$contact_lc"; then echo "NOTWORK"; return; fi
  if [ "$wscore" -gt 0 ] && [ "$wscore" -ge "$pscore" ]; then echo "WORK"; return; fi
  if [ "$pscore" -gt 0 ] && [ "$pscore" -gt "$wscore" ]; then echo "NOTWORK"; return; fi
  if [ "$wscore" -eq 0 ] && [ "$pscore" -eq 0 ]; then
    verdict=$(curl -s --max-time 300 "$OLLAMA_URL/api/generate" \
      -d "{\"model\":\"${LLM_MODEL:-qwen2.5:1.5b}\",\"stream\":false,\"options\":{\"num_predict\":8,\"temperature\":0},\"prompt\":\"Classify this phone call. Reply with exactly WORK or NOTWORK.\\nCall: \\\"$text\\\"\\nAnswer: WORK or NOTWORK?\"}" | jq -r '.response // ""' 2>/dev/null || true)
    case "$verdict" in
      *WORK*) echo "WORK" ;; *NOTWORK*) echo "NOTWORK" ;; *) echo "UNKNOWN" ;;
    esac
    return
  fi
  echo "NOTWORK"
}

# ---- Sarvam transcription ----------------------------------------------------
sarvam_transcribe() { # wav -> transcript on stdout; return 3 if no key
  local wav="$1"
  [ -n "${SARVAM_API_KEY:-}" ] || { echo "NO_SARVAM_KEY"; return 3; }
  local dir out="" chunk code body t
  dir=$(mktemp -d)
  ffmpeg -y -v error -i "$wav" -f segment -segment_time "${SARVAM_MAX_CHUNK_SECS}" \
    -c:a copy "$dir/c_%03d.wav" 2>/dev/null || { rm -rf "$dir"; echo "SEGMENT_FAIL"; return 1; }
  for chunk in "$dir"/c*.wav; do
    [ -f "$chunk" ] || continue
    code=$(curl -s --max-time 40 -o "$dir/b.json" -w "%{http_code}" \
      -X POST "https://api.sarvam.ai/speech-to-text" \
      -H "api-subscription-key: ${SARVAM_API_KEY}" \
      -F "file=@${chunk};type=audio/wav" \
      -F "model=${SARVAM_MODEL}" -F "mode=${SARVAM_MODE}" \
      -F "language_code=${SARVAM_LANGUAGE_CODE}" 2>/dev/null || echo "000")
    if [ "$code" = "200" ]; then
      t=$(jq -r '.transcript // ""' "$dir/b.json" 2>/dev/null || true)
      out="$out $t"
    else
      log "sarvam error http=$code body=$(head -c 160 "$dir/b.json" 2>/dev/null)"
    fi
  done
  rm -rf "$dir"
  printf '%s' "$out" | sed 's/^ *//; s/  */ /g'
}

# ---- Summary LLM (free API or local) -----------------------------------------
ai_summary() { # text -> summary
  local text="$1" body
  case "${SUMMARY_PROVIDER:-}" in
    free)
      [ -n "${SUMMARY_API_KEY:-}" ] || { echo "NO_FREE_KEY"; return 1; }
      body=$(jq -nc \
        --arg model "${SUMMARY_MODEL}" \
        --arg txt "$text" \
        '{model:$model,messages:[{role:"user",content:("Summarize this phone call in 3-5 bullets; list decisions/action-items. Write in the same language as the call.\n\n"+$txt)}],max_tokens:512,temperature:0.3}' 2>/dev/null || true)
      curl -s --max-time 60 -X POST "${SUMMARY_API_URL}" \
        -H "Authorization: Bearer ${SUMMARY_API_KEY}" -H "Content-Type: application/json" \
        -d "$body" | jq -r '.choices[0].message.content // "summary_failed"' 2>/dev/null || echo "summary_failed"
      ;;
    local)
      [ -n "${LLM_MODEL:-}" ] || { echo "NO_LOCAL_MODEL"; return 1; }
      curl -s --max-time 120 "${OLLAMA_URL}/api/generate" -d "{\"model\":\"${LLM_MODEL}\",\"stream\":false,\"prompt\":\"Summarize this phone call in 3-5 bullets; list action-items. Call: \\\"$text\\\"\"}" \
        | jq -r '.response // ""' 2>/dev/null || echo "summary_failed"
      ;;
    *) echo "Summary provider not configured (set SUMMARY_PROVIDER=free|local + keys). Run: bash /root/projects/call-transcriber/scripts/configure-sarvam.sh"; return 1 ;;
  esac
}

# ---- Approval UI -------------------------------------------------------------
contact_from_name() { # basename (without .m4a) -> caller name
  printf '%s' "$1" | sed 's/_.*//; s/^Call //; s/ *$//'
}

send_approval() { # path
  local path="$1" contact base token ts dur kb
  base=$(basename "$path" .m4a)
  contact=$(contact_from_name "$base")
  token=$(token_of "$path")
  ts=$(date "+%F %H:%M")
  dur=$(ffprobe -v quiet -show_entries format=duration -of csv=p=0 "$path" 2>/dev/null || echo 0)
  if ! grep -qF "$token|" "$PENDING_FILE" 2>/dev/null; then
    printf '%s|%s|%s|%s\n' "$token" "$path" "$contact" "$base" >> "$PENDING_FILE"
  fi
  kb=$(jq -nc --arg t "$token" '{inline_keyboard:[
    [{text:"🎤 Transcribe (Sarvam)",callback_data:($t+"|transcribe")}],
    [{text:"📝 Transcribe + Summary",callback_data:($t+"|both")}],
    [{text:"❌ Skip",callback_data:($t+"|skip")}]
  ]}'); [ "$kb" != "null" ] || kb=''
  tg_send "*New call recorded*\nCaller: $contact\nLength: ${dur%s.*}s · Seen: $ts\nTap to process (uses Sarvam credits only when transcribing):" "$kb"
  log "approval requested: $base (token=$token)"
}

# ---- Perform a requested action ---------------------------------------------
do_action() { # token action
  local token="$1" action="$2" line path contact base wav_tmp text verdict
  line=$(pending_lookup "$token")
  [ -n "$line" ] || { log "callback: unknown/expired token=$token"; return 0; }
  path=$(printf '%s' "$line" | cut -d'|' -f2)
  contact=$(printf '%s' "$line" | cut -d'|' -f3)
  base=$(printf '%s' "$line" | cut -d'|' -f4)
  [ -f "$path" ] || { tg_send "Caller: $contact — recording file no longer exists."; remove_pending "$token"; return 0; }
  wav_tmp="/tmp/transcribe_${token}.wav"
  ffmpeg -y -v error -i "$path" -ar 16000 -ac 1 -c:a pcm_s16le "$wav_tmp" || { log "ffmpeg FAILED: $path"; tg_send "Transcode failed for: $contact"; return 1; }

  tg_send "Transcribing \"$contact\" with Sarvam…"

  if [ "$action" = "skip" ]; then
    rm -f "$wav_tmp"; remove_pending "$token"
    tg_send "Skipped: $contact."; log "skip: $base"; return 0
  fi

  text=$(sarvam_transcribe "$wav_tmp")
  if [ "$text" = "NO_SARVAM_KEY" ]; then
    rm -f "$wav_tmp"; tg_send "No Sarvam key set. Run: bash /root/projects/call-transcriber/scripts/configure-sarvam.sh"; remove_pending "$token"; return 0
  fi
  [ -z "$text" ] && text="(no transcript returned)"
  printf '%s' "$text" > "$TRANSCRIPT_DIR/$base.txt" 2>/dev/null || true
  rm -f "$wav_tmp"

  verdict=$(classify_transcript "$text" "$contact")
  case "$action" in
    transcribe)
      if [ "$verdict" = "WORK" ]; then
        tg_send "🔨 *Work call — $contact*\n\n$text"
      else
        tg_send "📝 *Transcribed (non-work)* — $contact\n\n$text"
      fi
      ;;
    both)
      local summ
      summ=$(ai_summary "$text")
      if [ "$verdict" = "WORK" ]; then
        tg_send "🔨 *Work call — $contact*\n\n📌 *Transcript:*\n$text\n\n📋 *Summary:*\n$summ"
      else
        tg_send "📝 *$contact*\n\n📌 *Transcript:*\n$text\n\n📋 *Summary:*\n$summ"
      fi
      ;;
  esac
  mark_processed "$path"
  log "done($action): $base verdict=$verdict"
  remove_pending "$token"
}

# ---- Telegram polling loop --------------------------------------------------
handle_updates() {
  local upd offset new_off
  [ -n "${TELEGRAM_BOT_TOKEN:-}" ] || return 0
  offset=$(cat "$TG_OFFSET_FILE" 2>/dev/null || echo 0)
  upd=$(curl -s --max-time 8 "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getUpdates" \
        -d "offset=${offset:-0}" -d "limit=50" -d "timeout=5" 2>/dev/null) || return 0
  [ "$upd" = "null" ] && return 0
  new_off=$offset
  # iterate callback_query updates
  echo "$upd" | jq -r '.result[]? | select(.callback_query and .callback_query.data) |
        "\(.update_id)\t\(.callback_query.id)\t\(.callback_query.data)"' 2>/dev/null | \
  while IFS=$'\t' read -r uid cbid data; do
    [ -n "$data" ] || continue
    local token action
    token="${data%%|*}"
    action="${data#*|}"
    tg_answer "$cbid" "Processing \"$action\"…"
    log "callback: action=$action token=$token"
    do_action "$token" "$action" &
    new_off=$((uid + 1))
    echo "$new_off" > "$TG_OFFSET_FILE.tmp"
  done
  # persist highest offset seen
  local max_off
  max_off=$(cat "$TG_OFFSET_FILE.tmp" 2>/dev/null | sort -n | tail -1)
  [ -n "$max_off" ] && [ "$max_off" -gt "$offset" ] && { echo "$max_off" > "$TG_OFFSET_FILE"; rm -f "$TG_OFFSET_FILE.tmp"; } || rm -f "$TG_OFFSET_FILE.tmp"
}

# ---- main -------------------------------------------------------------------
: > "$TG_OFFSET_FILE.tmp" 2>/dev/null || true
_sarvam_state="off"; [ -n "${SARVAM_API_KEY:-}" ] && _sarvam_state="set"
log "transcriber watcher started (sarvam=${_sarvam_state} summary=${SUMMARY_PROVIDER:-none})"

while true; do
  handle_updates
  find "$RECORD_DIR" -maxdepth 2 -type f -iname "*.m4a" -newer "$STATE_FILE" 2>/dev/null | while read -r f; do
    was_processed "$f" && continue
    send_approval "$f"
    mark_processed "$f"          # mark seen so we don't re-offer while awaiting tap
  done
  sleep "${POLL_SECS:-60}"
done
