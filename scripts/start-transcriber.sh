#!/usr/bin/env bash
# Start/stop the call transcription watcher + Ollama.
set -euo pipefail

WATCHER=/root/scripts/transcribe-watcher.sh
export OLLAMA_HOST=127.0.0.1:11434

ensure_ollama() {
  # Ollama is only an optional WORK-classification tiebreaker; Sarvam does the
  # real transcription. Make it best-effort so it never blocks startup.
  if ! command -v ollama >/dev/null 2>&1; then
    echo "  ollama not installed - skipping (Sarvam STT only)."
    return 0
  fi
  if ! curl -s --max-time 3 "$OLLAMA_HOST/api/version" >/dev/null 2>&1; then
    echo "  starting ollama..."
    setsid nohup ollama serve >/tmp/ollama.log 2>&1 </dev/null &
    for i in $(seq 1 10); do
      curl -s --max-time 3 "$OLLAMA_HOST/api/version" >/dev/null 2>&1 && break
      sleep 2
    done
  fi
}

case "${1:-start}" in
  start)
    echo "==> Ensuring Ollama is up"
    ensure_ollama
    echo "==> Starting transcription watcher"
    if pgrep -f "/root/scripts/transcribe-watcher.sh" >/dev/null; then
      echo "  watcher already running"
    else
      setsid nohup "$WATCHER" >/dev/null 2>&1 </dev/null &
      sleep 3
      pgrep -f "$WATCHER" >/dev/null && echo "  watcher started (pid $(pgrep -f "$WATCHER" | head -1))" || echo "  FAILED - check /tmp/transcriber-watcher.log"
    fi
    echo "==> Done. New recordings in /sdcard/Recordings/Call and Voice Recorder will be transcribed automatically."
    ;;
  stop)
    pkill -f "/root/scripts/transcribe-watcher.sh" 2>/dev/null && echo "  watcher stopped" || echo "  watcher not running"
    ;;
  status)
    echo "Ollama: $(curl -s --max-time 3 "$OLLAMA_HOST/api/version" 2>/dev/null || echo 'down')"
    pgrep -f "$WATCHER" >/dev/null && echo "Watcher: running" || echo "Watcher: not running"
    echo "Processed so far: $(wc -l < /root/.transcriber-state 2>/dev/null || echo 0)"
    echo "Log: /tmp/transcriber-watcher.log"
    ;;
  *)
    echo "usage: $0 {start|stop|status}"
    exit 1
    ;;
esac