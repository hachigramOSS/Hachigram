# changelog

headings match the tag: `v0.<major>.<minor>[.<rc>]` for release candidates,
`v<major>.<minor>[.<patch>]` for the real thing. the display name lives in `VERSION`.

## v0.1.0

hachigram's first build. an opinionated, fully open source fork of hachigram,
picked up where hachigram's source went dark, on telegram 12.10.1.

### telegram

- telegram 12.10.1, up from 12.5.1
- upstream is properly linked, so future updates are a merge instead of dumping a
  whole new source tree on top

### gone

- talsec freerasp anti-tamper, plus the native and kotlin obfuscation it came with
- huawei/hms and the appgallery build
- the donation system, the old maintainer's payment details, and the safestars
  affiliate stuff
- hardcoded perks for the old maintainer's accounts: a restart anyone on that list
  could trigger by sending you a link, delete protection on two chats, and a
  lockscreen prompt bypass
- the remote sticker blocklist, which let someone else change what you saw
- firebase analytics, remote config, crashlytics and app indexing. none of them were
  initialised, but remote config alone accounted for 546 references in the shipped
  apk, and shipping code that can't run is just weight
- telegram's google assistant action reporting, which told google when an
  assistant-launched action finished

### changed

- firebase does push and nothing else. nothing can phone home or change how the app
  behaves from a server
- everything that used to be locked behind donating is just available
- named hachigram, package `com.the306bobby.hachigram`, so it installs next to
  hachigram instead of over it
- the ui talks like a person now, not a changelog
- the updater knows what a release candidate is: rc builds follow later rcs and take
  the stable release they were building up to, stable builds never get pulled onto an
  rc

### build

- a fresh clone actually builds. the sources the old repo held back are here, api
  keys come from `local.properties`, and signing no longer needs a keystore that was
  never published
- ndk 27.2.12479018 for 16 kb pages, targetsdk 36
- releases are built and hosted on github

### privacy

- `PRIVACY.md` spells out what these builds actually do. short version: nothing
  phones home
