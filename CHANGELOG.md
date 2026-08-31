# Changelog

Headings use the tag scheme: `v0.<major>.<minor>[.<rc>]` for release candidates,
`v<major>.<minor>[.<patch>]` for final releases. The display name for each release
comes from `VERSION`.

## v0.1.0

First release of Cherrygram Next, forked from Cherrygram at the point its source
stopped being published.

### Telegram

- Updated from Telegram 12.5.1 to 12.10.1.
- Upstream is now tracked through a real merge base, so future updates are an
  ordinary merge rather than a source drop.

### Removed

- Talsec freeRASP anti-tamper, and the native and Kotlin obfuscation layers
  alongside it.
- Huawei/HMS support, including the AppGallery build target.
- The donation subsystem, the previous maintainer's payment details, and the
  SafeStars affiliate integration.
- Hardcoded privileges for the previous maintainer's accounts: a restart command
  triggered by sending a link, delete protection on two dialogs, and a lockscreen
  prompt bypass.
- The remotely fetched sticker blocklist, which let a third party change what
  users saw.

### Changed

- Firebase is opt-in. No configuration ships, so analytics, crash reporting,
  remote config and push are inert; supplying a `google-services.json` restores
  them with no code changes.
- Features that were gated behind donating are available to everyone.
- Update checks and the repository link point at this project.
- Renamed to `com.the306bobby.cherrygramnext`, installing alongside Cherrygram
  rather than replacing it.

### Build

- A clean clone builds: the sources the upstream repository withheld are present,
  API credentials come from `local.properties`, and signing no longer requires a
  keystore that was never published.
- NDK 27.2.12479018 for 16 KB page support, targetSdk 36.
- Continuous integration builds both ABIs on a Forgejo runner.

### Privacy

- A policy describing what these builds actually do: `PRIVACY.md`.
