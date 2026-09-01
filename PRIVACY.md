# Privacy Policy — Hachigram

_Last updated: 2026-08-31_

Hachigram is an open-source Telegram client for Android. It is a fork of
Cherrygram, which is itself a fork of the official Telegram app.

## What we collect

Nothing. The project runs no servers that collect user data, no analytics, no
crash reporting and no telemetry of any kind. There are no ads and no trackers.

Published builds use Firebase for **push notifications only**. Firebase Analytics
is off by default, the Crashlytics plugin is not applied, and Firebase Remote
Config is never initialised, so none of them can report anything or change how the
app behaves. The anti-tamper SDK that upstream Hachigram bundled has been removed
entirely.

## Where your data does go

**Telegram.** This is a Telegram client, so your messages, contacts, media and
account details are handled by Telegram's own servers under
[Telegram's Privacy Policy](https://telegram.org/privacy). Nothing in this app
changes that, and none of it passes through us.

**Update checks.** The app asks `git.306bobbyandroid.download` for the latest
release. That server sees your IP address and the request, as any web server
would. By default this happens automatically, at most once an hour while you are
using the app, and you can turn it off in Hachigram settings. Nothing about your
account or your messages is sent; it is a plain request for the release list.

**Push notifications.** Published builds receive notifications through Firebase
Cloud Messaging, because it is far cheaper on battery than keeping our own
connection alive. Google issues your device a push token and delivers the
notifications, so Google can see that token, your IP address and when messages
arrive for you. **It does not see who messaged you or what they said**: Telegram
sends an encrypted payload the app decrypts locally. Telegram is given credentials
for our Firebase project so it can deliver to that token.

If you build without a `google-services.json`, or run a device with no Google
services and no microG, the app keeps its own connection instead and no push
provider is involved.

**Google Gemini (optional, off by default).** The AI features do nothing unless
you enter your own Gemini API key. If you do, the text you send to those features
goes to Google under your own API key and Google's terms. Remove the key and
nothing is sent.

**Links you tap.** Opening a link, a Telegram channel or a payment shortcut hands
off to your browser or the relevant app. We do not proxy or log any of it.

## What stays on your device

App settings, cached media and chat data stay on your device and are handled by
Android's normal app-data mechanisms. If the app crashes it writes a local
`last_crash.log`; it is never uploaded, and it is only shared if you choose to
share it.

## If you build it yourself

Push needs a `google-services.json` of your own; without one the app falls back to
its own connection. Whoever produces a build decides that, and this policy
describes the builds published by this project.

## Changes

This policy lives in the repository alongside the code, so its history is public.
Material changes will appear in the commit log.

## Contact

Source and issues: <https://git.306bobbyandroid.download/306bobby/Hachigram>
