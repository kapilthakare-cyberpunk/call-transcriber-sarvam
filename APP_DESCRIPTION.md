# Sarvam Call Transcriber – Android App Description

## 📱 App Overview
**Sarvam Call Transcriber** is an Android application that automatically detects new call recordings and prompts you to decide whether to transcribe each call using the Sarvam AI Speech‑to‑Text API. Transcription credits are only spent when you explicitly tap **Transcribe**, conserving your limited Sarvam credits.

The app also offers an optional AI‑generated summary/extraction of the call's action items, powered by a free local LLM or a free‑tier cloud model.

---

## 🎯 Core Features

| Feature | Description |
|---|---|
| **New‑call detection** | Scans `/sdcard/Recordings` (via MediaStore polling) for `.m4a` files that arrived after the last check. |
| **Per‑call approval gate** | On every new recording, a toast‑style inline card appears with three buttons:<br>• 🎤 **Transcribe** (Sarvam AI STT only)<br>• 📝 **Transcribe + Summary** (Sarvam + free/local LLM)<br>• ❌ **Skip** (no cost) |
| **Sarvam AI transcription** | Uses the `saaras:v3` model on `https://api.sarvam.ai/speech-to-text`. Audio is split into 29s chunks (the REST API caps at 30s) and stitched back together. Works excellently with Marathi, Hindi, and English code‑switching. |
| **WORK / NOTWORK classification** | Keyword‑rule based (invoice, payment, client, etc.) with an optional Ollama/LLM tiebreaker when no keywords match. Calls classified as **WORK** are forwarded to Telegram. |
| **AI summary / task extraction** (optional) | After transcription, a separate LLM (configured as `free` – OpenRouter/Groq, or `local` – ollama/llama.cpp) produces 3‑5 bullet points highlighting decisions, action‑items, and spoken numbers. |
| **Telegram integration** | Re‑uses your existing bot token and chat ID. On transcription completion, the app sends a markdown‑formatted message to your Telegram channel/chat: <br>• Transcript (if WORK, prefixed with 🔨)<br>• Optional summary<br>• Caller name and duration |
| **Privacy‑first** | All audio stays on the device. Sarvam is only called after explicit user tap. No recordings are uploaded without your consent. |
| **Configurable** | Edit `/root/.config/call-transcriber.conf` (or the app's settings screen) to:<br>• Set/change your Sarvam API key<br>• Choose summary provider (`free` / `local` / off)<br>• Adjust keyword lists, poll interval, language code<br>• Toggle classification overrides |

---

## 📸 Screens (mock‑up)

| Screen | Content |
|---|---|
| **New‑call notification** | “New call recorded – tap to review” <br>[_Button 1: Transcribe_] [_Button 2: Transcribe + Summary_] [_Button 3: Skip_] |
| **Transcription result** | *Marathi Devanagari transcript* with a toggle to copy/forward<br>• “सर एकदा चेक करा ना अमाउंट मी तुम्हाला WhatsApp पर स्क्रीनशॉट पाठवत आहे…” |
| **Summary screen** (if enabled) | 3‑5 bullet points:<br>• “Caller asked to send invoice for ₹ 2,500.”<br>• “Agreed to share screenshot on WhatsApp.”<br>• “Payment to be generated today.” |
| **Telegram forward** | Markdown message appearing in your Telegram chat: <br>```\n🔨 *Work call – Amit Gujar*\n\nसर एकदा चेक करा ना अमाउंट मी तुम्हाला WhatsApp पर स्क्रीनशॉट पाठवत आहे…\n\n📋 *Summary:*\n• Invoice of ₹ 2,500 requested<br>• Screenshot to be shared on WhatsApp\n``` |

---

## ⚙️ Technical Stack (Android‑native version)

| Layer | Technology |
|---|---|
| **Detection** | `BroadcastReceiver` + `MediaStore` observer (new `.m4a` files in `/sdcard/Recordings/Call`) |
| **Transcode** | `ffmpeg` (m4a → 16 kHz mono PCM WAV) |
| **STT** | `OkHttp` + `multipart/form‑data` POST to `https://api.sarvam.ai/speech-to-text` <br>• Header: `api-subscription-key: <YOUR_KEY>`<br>• Fields: `file`, `model=saaras:v3`, `mode=transcribe`, `language_code=unknown` |
| **Chunking** | 29s segments (safe under the 30s REST cap) |
| **Classification** | Keyword‑rule engine (built‑in lists + user‑editable regexes)<br>Optional LLM tiebreaker via Ollama (`http://10.0.2.2:11434` or direct) |
| **Summary** | Two paths:<br>• **Free**: POST to `https://openrouter.ai/api/v1/chat/completions` (or Groq) with `meta‑llama/llama‑4‑scout`<br>• **Local**: `curl http://127.0.0.1:11434/api/generate` with a local ggml/llama.cpp model |
| **Telegram** | `curl –X POST https://api.telegram.org/bot<TOKEN>/sendMessage` with markdown & inline‑keyboard (for the per‑call gate) or simple `sendMessage` for results |
| **Permissions** | `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` (or `MANAGE_EXTERNAL_STORAGE` on Android 11+), `FOREGROUND_SERVICE` (if you want ongoing notification), `INTERNET` |

---

## 🔐 Privacy & Security

- **No background recording** – the app only reacts to recordings that already exist on the phone.
- **Sarvam key** is stored locally (SharedPreferences or encrypted SharedPreferences) and never transmitted.
- **Telegram messages** contain only the transcript and optional summary – no raw audio.
- **Optional local LLM** runs entirely on‑device (if you deploy a Qt/llama.cpp‑based model), keeping everything offline.

---

## 📋 Suggested Play Store Listing (short)

> **Sarvam Call Transcriber** – Your personal AI‑powered call auditor for Android.  
> Automatically detects new recordings and asks: *Transcribe this call?*  
> • Powered by Sarvam AI – state‑of‑the‑art Marathi/Hindi/English speech‑to‑text.<br>• Only spend credits when you say yes.<br>• optional AI summary of action items.<br>• Classify calls as WORK or personal, forward WORK transcripts to Telegram.<br>• Fully private – audio never leaves your phone unless you tap “Transcribe”.<br>• Configurable keywords, poll interval, and summary model.<br>• Free open‑source build, no subscription, no ads.

---

## 🛠️ How to Get It

| Option | What you need |
|---|---|
| **A. Telegram‑only mode** (already functional) | No APK needed – the watcher runs in Termux/proot on your phone. The inline‑keyboard cards appear in Telegram. |
| **B. Native Android APK** | I can scaffold a full Kotlin + Jetpack Compose source tree for you to build in Android Studio (no SDK in this environment, but complete source & instructions provided). |
| **C. Local web dashboard** | A tiny Flask/Express server hosted on the proot box, accessible from your phone’s browser – full UI, same Sarvam logic. |

**Which would you like to pursue?** I can:
- Guide you through testing the existing Telegram gate right now.
- Scaffold the native Android Compose source tree.
- Set up the local web dashboard.
- Help you configure the AI summary provider (free LLM key or local model).

Just say the word!