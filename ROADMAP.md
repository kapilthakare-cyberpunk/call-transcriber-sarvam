# Call Transcriber (Sarvam) — Roadmap

## Current State
- Bash watcher polls `/sdcard/Recordings` for new `.m4a` files.
- Telegram approval UI with inline keyboard.
- Transcription via Sarvam SaaS (`saaras:v3`) with chunking.
- Optional summary via local/API LLM.
- Keyword + optional contact-name classification.
- State tracking via flat file `/root/.transcriber-state`.
- Android Compose scaffold started but not complete.

## Immediate (0–2 weeks)
- [ ] Replace flat state file with SQLite to avoid duplicates and race conditions.
- [ ] Scope `find` to known recording subdirectories and add `-print0` safety.
- [ ] Decouple Telegram config from hardcoded factory-skill path; support env vars.
- [ ] Improve watcher error handling: retry/backoff for Sarvam/Telegram/network failures.
- [ ] Add structured JSON logging and simple metrics: processed/skipped/failed counts.

## Short Term (2–6 weeks)
- [ ] Build Android companion UI:
  - Foreground service for reliable background operation.
  - Material3 app for approvals, history, and settings.
  - Boot autostart without Termux:Boot dependency.
- [ ] Add offline Whisper fallback when Sarvam is unavailable or credits are exhausted.
- [ ] Add call metadata extraction: contact name/number, duration, timestamp from filename/system.

## Medium Term (6–12 weeks)
- [ ] Improve classification:
  - per-contact rules
  - learn from user feedback (approve/skip/summary choices)
  - multi-language keyword sets
- [ ] Export options:
  - Google Sheets sync with pincode-aware entry/exit timestamps
  - CSV backup
  - optional encrypted cloud backup
- [ ] Battery/performance tuning:
  - adaptive polling based on device idle/battery state
  - audio preprocessing to reduce transcription cost/time

## Long Term (3+ months)
- [ ] Full-featured Android app:
  - searchable transcript history
  - reminders and follow-ups from calls
  - multi-language support beyond Hindi/Marathi
- [ ] Integrations:
  - calendar events from work calls
  - CRM/task tool hooks
  - end-to-end encrypted backup/restore
