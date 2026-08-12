# Call Transcriber — Termux Working Branch

This branch contains the working local Termux/proot setup for call transcription with optional Sarvam + free/summary providers.

## What’s included
- `scripts/transcribe-watcher.sh` — polling watcher + Telegram approval + Sarvam/Whisper/LLM providers
- `scripts/start-transcriber.sh` — start/stop/status helper
- `config/env.example` — settings to copy to `~/.config/call-transcriber.conf`

## Quick start
```bash
cp config/env.example ~/.config/call-transcriber.conf
bash scripts/start-transcriber.sh start
bash scripts/start-transcriber.sh status
```

## Notes for this environment
- Uses existing Termux Telegram skill config if present.
- Works on aarch64 Termux/proot Ubuntu.
- New recording detection uses polling; no inotify on `/sdcard`.
- Local Whisper and Ollama remain supported alongside free APIs.
