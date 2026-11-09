# changelog

headings match the tag: `v0.<major>.<minor>[.<rc>]` for release candidates,
`v<major>.<minor>[.<patch>]` for the real thing. the display name lives in `VERSION`.

## v1.1.0

the first release with hachigram's own face: telegram 13.2.1 underneath, a
hundred more inugram bugfixes, and no cherries anywhere.

### telegram

- telegram 13.2.1, up from 12.10.1. upstream rebuilt theming, the action bar,
  and the media player (now media3, built from source) in this stretch

### changed

- the app icon is hachigram's shiba, drawn by
  [karuzone](https://www.fiverr.com/karuzone), in eight variants: default,
  hachi, hachi dark, hachi outline, shiba, shiba dark, sketch and tile. the
  cherrygram launcher icons, splash, session logo and settings entry icon are
  gone with it
- the status bar notification icon is a silhouette of that face instead of a
  cherry. the switch to fall back to telegram's paper plane is still there
- the hachigram channel and support group no longer get cherrygram's cherry
  emoji as a title badge and profile background

### fixed

- 101 more bugfixes from [inugram](https://github.com/teidesu/inugram):
  notification dedup on android 16, stale
  notifications after reading elsewhere, ipv6 proxies, forum cache races,
  memory leaks in the image loader and notification center, share sheet
  dead zones, video quality selection, and a long tail of visual fixes.


## v0.1.0.1

the interface reads in lowercase, 129 upstream bugfixes land, and two pieces of
inherited breakage are gone.

### changed

- the whole interface reads in lowercase now, not just the strings this fork
  ships. telegram serves most of its text from a language pack, so this happens
  where strings are resolved rather than in the bundled xml, which means it holds
  after the language pack syncs. there's a switch for it in appearance settings
- hiding archived stories was locked to two hardcoded accounts in a premium or dev
  build, and its labels were left in russian. it's available to everyone and reads
  in english. that was the last hardcoded account check in the app

### fixed

- archiving a chat shows the undo button again. the toast without one is the
  first-run hint, and the flag saying the hint had been shown was written
  asynchronously, so it could be lost if the app was killed straight after and
  every archive looked like the first
- 129 bugfixes from [inugram](https://github.com/teidesu/inugram), MIT, keeping
  their original authors. memory leaks, notification handling, media and camera
  behaviour, scrolling and gestures. the patches that need inugram's own runtime,
  and its features and debloat, are not here

### build

- the gradle wrapper is the official 8.13 one. the jar this repo inherited matched
  no published gradle release, and github refused to run a build with it
- every commit builds an installable arm64 apk you can grab from the run
- the native build is cached between runs, so a release that doesn't move a
  submodule doesn't recompile ffmpeg and friends

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
