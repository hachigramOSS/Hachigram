# Hachigram

an opinionated, fully open-source Telegram client for Android.

Hachigram is a fork of [Cherrygram](https://github.com/arsLan4k1390/Cherrygram),
picked up from the last source it published, brought up to current upstream
[Telegram](https://github.com/DrKLO/Telegram), and stripped of everything that
phoned home or answered to someone else.

named after a shiba who is not Hachiko but would also wait.

## what's different

- no anti-tamper sdk, no huawei services, no donation system
- no hardcoded privileges for anyone's account
- firebase does push and nothing else. no analytics, no crash reporting, no remote
  config, so nothing changes the app's behaviour from a server
- everything that used to be locked behind donating is just available
- builds from a clean clone, which the upstream project did not

## install

Grab an APK from
[releases](https://git.306bobbyandroid.download/306bobby/Hachigram/releases).
`arm64-v8a` for anything modern, `armeabi-v7a` for older 32-bit devices, or the
universal build if you are unsure.

It installs alongside other Telegram clients rather than replacing them.

## privacy

[what these builds do and do not send](https://the306bobby.com/hachigram/privacy).
short version: nothing phones home. push notifications go through firebase because
it costs far less battery than holding our own connection, and the payload is
encrypted so google cannot read it.

## licence

GPLv2, inherited from Telegram. Cherrygram's copyright notices are kept in the
files that came from it, as the licence requires.

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
   git clone --recursive --shallow-submodules <your-fork-url> Hachigram
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
5. Fill out values in `TMessagesProj/src/main/java/com/the306bobby/hachigram/Extra.kt` – each variable
   contains a link explaining where to get the required data. Register your own `api_id`/`api_hash`
   at https://my.telegram.org; do not reuse another client's.
6. You are ready to compile.


## thanks to
- [Cherrygram](https://github.com/arsLan4k1390/Cherrygram), which this is a fork of
- [inugram](https://github.com/teidesu/inugram), whose patches this carries
- [Catogram](https://github.com/Catogram/Catogram) and [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [exteraGram](https://github.com/exteraSquad/exteraGram) and [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegraher](https://github.com/nikitasius/Telegraher) and [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
