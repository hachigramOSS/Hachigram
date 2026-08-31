# 🍒 Cherrygram

Cherrygram is a third-party Telegram client with not many but useful modifications.

This is an unofficial fork of the original [Telegram App for Android](https://github.com/DrKLO/Telegram).

This repo contains the official source code for [Telegram App for Android](https://play.google.com/store/apps/details?id=org.telegram.messenger).

## Current Maintainers

- [arsLan4k1390](https://github.com/arsLan4k1390)
- You? :)

## Contributors

- [arsLan4k1390](https://github.com/arsLan4k1390)


## Discussion

Join the [Cherrygram official channel](https://t.me/cherrygram)

Join the [Cherrygram official group](https://t.me/CherrygramSupport)


## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto


## Compilation Guide

You will require Android Studio 2025.1.4, Android NDK 27.2.12479018 and Android SDK 36.

1. Clone the source code **with its submodules** — upstream Telegram now vendors ffmpeg,
   libvpx, dav1d, opus, openh264, libyuv, tlottie and jlatexmath as git submodules:
   ```bash
   git clone --recursive --shallow-submodules <your-fork-url> Cherrygram-Next
   ```
   If you already cloned without `--recursive`:
   ```bash
   git submodule update --init --recursive --depth=1
   ```
2. Fill out storeFile, storePassword, keyAlias, keyPassword in all module build.gradle files
   (TMessagesProj_App, TMessagesProj_AppStandalone) to sign your app.
3. Go to https://console.firebase.google.com/, create android apps matching your application ID,
   enable Firebase Messaging and download `google-services.json` into the `TMessagesProj` folder.
4. Open the project in Android Studio (note that it should be opened, NOT imported).
5. Fill out values in `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/Extra.kt` – each variable
   contains a link explaining where to get the required data. Register your own `api_id`/`api_hash`
   at https://my.telegram.org; do not reuse another client's.
6. You are ready to compile.


## Thanks to:
- [Catogram](https://github.com/Catogram/Catogram) and [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [exteraGram](https://github.com/exteraSquad/exteraGram) and [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegraher](https://github.com/nikitasius/Telegraher) and [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
