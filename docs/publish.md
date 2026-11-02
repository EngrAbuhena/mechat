# Publishing MeChat to a public GitHub repository

## License choice (one paragraph)

The **server** is under **AGPL-3.0** (`server/LICENSE`): anyone who self-hosts or modifies
the relay must share their changes, which keeps the server ecosystem auditable and prevents
closed forks that could quietly weaken privacy. The **app** is under **Apache-2.0** (`LICENSE`):
it is maximally permissive for client software — users and OEMs can build and distribute
it without source-disclosure obligations, while the Apache-2.0 patent grant protects
contributors. If you prefer stronger copyleft on the app too, swap `LICENSE` to GPL-3.0 —
your call before publishing.

## Exact commands

```bash
# 0. Safety check first (must pass):
bash scripts/check-no-trackers.sh

# 1. Initialize and commit:
git init
git add .
git commit -m "Initial release"

# 2a. With the GitHub CLI:
gh repo create <my-username>/mechat --public --source=. --remote=origin --push

# 2b. Or manually: create an empty PUBLIC repo at https://github.com/new, then:
git remote add origin git@github.com:<my-username>/mechat.git
git branch -M main
git push -u origin main

# 3. Repo description and topics (GitHub UI or CLI):
gh repo edit <my-username>/mechat \
  --description "Identity-free, end-to-end encrypted messenger for Android (libsodium Double Ratchet, WebRTC calls, blind-mailbox offline delivery)" \
  --add-topic android,privacy,e2ee,webrtc,kotlin
```

## Create GitHub Release v1.0.0

1. Build signed artifacts on your machine: `./gradlew assembleRelease bundleRelease`
   (keystore via `MECHAT_*` env vars — see docs/release-signing.md).
2. Compute: `sha256sum app-release.apk` and
   `keytool -printcert -jarfile app-release.apk | grep SHA256`.
3. On GitHub: Releases → Draft new release → tag `v1.0.0`, title "v1.0.0".
4. Attach `app-release.apk` (and the AAB if publishing to Play).
5. Release notes must include:
   - `SHA-256: <hash>`
   - `Signing certificate SHA-256 fingerprint: <fingerprint>`
   so users can verify the download.

## Final checklist

Done: full app (crypto, lock, panic wipe, Room/SQLCipher, invites, WebRTC, mailbox,
disappearing messages, attachments, calls, notifications), server + tests, CI workflows,
tracker scan, docs (rules, crypto, protocol, threat-model, server, security-review,
release-signing, battery, call-testing, release-checklist), README/SECURITY/CONTRIBUTING,
licenses, .gitignore hardened, repo safety scan clean.

Still manual: git init/commit/push, `gh repo create` or manual remote, release build with
real keystore, SHA-256 + fingerprint into release notes, description/topics, emulator
instrumented tests, dependency vulnerability scan, font files (Space Grotesk), screenshots,
real security@ contact in SECURITY.md.
