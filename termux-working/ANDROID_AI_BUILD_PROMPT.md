# AI Assistant Prompt — Android Call Transcriber Build

Copy everything below and send it to an AI assistant (Cursor, Claude, Copilot, etc.) on your MacBook Pro to build the Android app from this repo.

---

You are an Android build assistant. Build a working Android app from the repo `https://github.com/kapilthakare-cyberpunk/call-transcriber-sarvam.git`, branch `termux-working`, folder `android-app`.

Requirements:
1. Open `android-app` in Android Studio and fix any sync/build issues.
2. Implement the event-driven call detection flow:
   - Foreground service listening for call-end events
   - Enqueue `TranscriptionWorker` with the latest recording file path
3. Implement `TranscriptionWorker` to:
   - Read `inputData.getString("file_path")`
   - Transcode with `ffmpeg` to 16kHz mono WAV if needed
   - Call Sarvam STT if `sarvamApiKey` is set
   - Optionally summarize/classify using configured provider
   - Send result to Telegram if bot token/chat ID are set
4. Implement Settings screen to store:
   - Sarvam API key
   - Telegram bot token and chat ID
   - Summary provider + API URL/key/model
   - Work/personal keywords and contacts
5. Keep dependencies minimal and compatible with compileSdk 35 / minSdk 26 / targetSdk 35.
6. Build a debug APK and report the output path.

Do not add unnecessary abstractions. Prefer boring, working code over complex architecture.
