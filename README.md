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


## Versioning and releases

`APP_VERSION_NAME_CHERRY` in `gradle.properties` is this fork's own version and is
unrelated to Telegram's, which is tracked separately in `APP_VERSION_NAME` and shown
in the APK filename as the `-TG-` component.

Release-candidate status lives in the Forgejo release, not the version string: mark
the release as a pre-release. `/releases/latest`, which the in-app updater reads,
skips pre-releases, so stable users are not offered them.

Do not put a suffix such as `-RC1` in the version. The updater compares versions with
`Utilities.parseInt` on each dot-separated segment, which stops at the first
non-digit, so `1.0.0-RC1`, `1.0.0-RC2` and `1.0.0` all compare equal and no update
would ever be offered between them.

Pre-1.0 builds use `0.0.x` so that `1.0.0` remains free for the first stable release.

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
5. Fill out values in `TMessagesProj/src/main/java/com/the306bobby/cherrygramnext/Extra.kt` – each variable
   contains a link explaining where to get the required data. Register your own `api_id`/`api_hash`
   at https://my.telegram.org; do not reuse another client's.
6. You are ready to compile.


## Thanks to:
- [Catogram](https://github.com/Catogram/Catogram) and [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [exteraGram](https://github.com/exteraSquad/exteraGram) and [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegraher](https://github.com/nikitasius/Telegraher) and [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
