# Build the Call Transcriber Android app on a MacBook Pro

## Prerequisites
- macOS with admin access
- Android Studio Ladybug or newer
- Java 17 from Android Studio or Temurin
- Git
- GitHub CLI optional

## 1. Clone the repo
```bash
git clone https://github.com/kapilthakare-cyberpunk/call-transcriber-sarvam.git
cd call-transcriber-sarvam
git checkout termux-working
```

## 2. Open in Android Studio
- File > Open > `android-app`
- Let Gradle sync complete
- If prompted, accept Android SDK install/update
- Build > Clean Project
- Build > Make Project

## 3. Configure API keys in `local.properties`
```properties
sarvam.api.key=YOUR_SARVAM_KEY
telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN
telegram.chat.id=YOUR_TELEGRAM_CHAT_ID
```

## 4. Run on device
- Connect Android phone via USB
- Enable USB debugging
- Select device in Android Studio
- Run app
- Grant permissions when prompted

## 5. Test call flow
- Make a call, end it, allow recording save
- App should detect new recording
- Choose Transcribe or Skip in approval UI
- Telegram notification appears with result

## 6. Optional providers
- Edit `config/env.example` or app Settings screen
- Set summary provider URL/key for OpenRouter/Groq/Cerebras/Mistral/OpenCode Zen
- Or use local Ollama/Whisper paths if available
