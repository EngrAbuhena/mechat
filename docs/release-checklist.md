# MeChat release checklist (95%)

## Automated (run in CI / Android Studio)

- [ ] `./gradlew testDebugUnitTest` — crypto/, lock, expiry, protocol coverage
- [ ] `./gradlew lintRelease`
- [ ] ktlint / detekt clean
- [ ] `./gradlew dependencyCheckAnalyze` (OWASP) — vulnerability scan (manual in sandbox)
- [ ] `scripts/check-no-trackers.sh app/build/outputs/apk/release/app-release.apk`
- [ ] Server: `cd server && npm test`

## Manual — instrumented tests (need an emulator; do NOT run in sandbox)

- [ ] `connectedDebugAndroidTest` — `PanicWipeTest`, `ChatUiTest`, `MailboxIntegrationTest`
      (the last one needs the real server in Docker + two emulated clients)

## Manual — smoke tests on real phones

- [ ] Onboarding + PIN setup + lock/unlock + panic PIN wipe
- [ ] Add contact via QR, safety number matches out of band
- [ ] Send/receive online (DataChannel) and offline (mailbox), exactly once
- [ ] Voice/video call, TURN relay option, permission denial handling
- [ ] Disappearing messages sweep; attachments; 1-hour idle battery test (docs/battery.md)
- [ ] Two-phone call test (docs/call-testing.md)

## Release hygiene

- [ ] Release APK minified (R8), `isDebuggable=false`, no tracker packages
- [ ] Certificate pins set for your deployment (primary + backup)
- [ ] Fonts bundled; README screenshots added; "Not independently audited" notice present
