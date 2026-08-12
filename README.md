# Call Transcriber (S26 / Termux proot)

Auto-transcribes **new** Android call recordings on-device and forwards
work-related calls to Telegram — fully local, no cloud, no uploads.

## Stack (all local, aarch64 / proot Ubuntu on Termux)
- **Transcription**: `whisper.cpp` `large-v3-turbo` (~1.6 GB, 100 langs incl. Hindi/Marathi, NEON SIMD)
- **MLOps / transcode**: `ffmpeg` (m4a → 16 kHz mono wav)
- **Work classification**: fast keyword rules (filename + transcript) + local Ollama `qwen2.5:1.5b` as a tiebreaker only (Ollama LLM gen is slow under proot; deterministic rules carry the load)
- **Notify**: existing Telegram bot token (re-used from the telegram skill config)
- **Watcher**: bash + `find -newer` polling every 60s (FUSE /sdcard can't fire inotify)

## Files
```
/root/projects/call-transcriber/
├── scripts/transcribe-watcher.sh   # the watcher (poll, transcribe, classify, notify)
├── scripts/start-transcriber.sh    # start/stop/status + ollama bootstrap
└── config.env                      # editable tuning knobs (keywords, models, paths)
```
Live path aliases: `/root/scripts/transcribe-watcher.sh` and `/root/scripts/start-transcriber.sh`
are symlinks into this project for a single source of truth.

Runtime:
- state file: `/root/.transcriber-state` (one processed path per line — keeps the 5k+ existing
  recordings untouched; `find -newer` + this file = only NEW recordings)
- transcripts: `/sdcard/Recordings/Transcripts/<basename>.txt`
- log: `/tmp/transcriber-watcher.log`
- whisper binary + models: `/opt/transcriber/whisper.cpp-master`

## Boot autostart
`~/.termux/boot/start-transcriber.sh` (requires the **Termux:Boot** app) calls
`proot-distro login ubuntu -- bash /root/scripts/start-transcriber.sh start`.
APK pre-staged at `/sdcard/Download/termux-boot-0.8.1.apk` — open & install on the phone once.

## Manage
```bash
bash /root/scripts/start-transcriber.sh start
bash /root/scripts/start-transcriber.sh status
bash /root/scripts/start-transcriber.sh stop
```
Tune without touching code: edit `/root/.config/call-transcriber.conf`.
