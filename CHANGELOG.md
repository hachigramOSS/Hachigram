# changelog

headings match the tag: `v0.<major>.<minor>[.<rc>]` for release candidates,
`v<major>.<minor>[.<patch>]` for the real thing. the display name lives in `VERSION`.

## v0.1.0

first build of cherrygram next. picks up where cherrygram left off when its source
went dark.

### telegram

- now on telegram 12.10.1, up from 12.5.1
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

### changed

- firebase does push and nothing else. analytics is off by default, the crashlytics
  plugin isn't applied, and remote config never starts, so nothing can phone home or
  change how the app behaves from a server
- everything that used to be locked behind donating is just available
- update checks and the repo link point here now
- renamed to `com.the306bobby.cherrygramnext`, so it installs next to cherrygram
  instead of over it
- the updater knows what a release candidate is: rc builds follow later rcs and take
  the stable release they were building up to, stable builds never get pulled onto an
  rc

### build

- a fresh clone actually builds. the sources the old repo held back are here, api
  keys come from `local.properties`, and signing no longer needs a keystore that was
  never published
- ndk 27.2.12479018 for 16 kb pages, targetsdk 36
- ci builds both abis on a forgejo runner

### privacy

- `PRIVACY.md` spells out what these builds actually do. short version: nothing
  phones home
