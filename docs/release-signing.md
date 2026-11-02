# Release signing & publishing

## Signing config

`app/build.gradle.kts` reads from environment variables **or** `~/.gradle/gradle.properties`:

```
MECHAT_KEYSTORE=/path/to/mechat-release.jks
MECHAT_KEYSTORE_PASSWORD=...
MECHAT_KEY_ALIAS=mechat
MECHAT_KEY_PASSWORD=...
```

Never commit the keystore or passwords (RULE 4); `.gitignore` excludes `*.jks`, `*.keystore`, `local.properties`.

Release enables **APK Signature Scheme v2 and v3** (v3 allows planned key rotation).

## Back up the keystore safely in TWO places

1. An **offline, encrypted medium** (e.g. VeraCrypt volume on a USB drive kept in a safe).
2. A **password manager's secure note** (or split Shamir shares among trusted people).

Store the keystore file + its passwords separately from each other. Losing the keystore
means you can never update the app under the same identity.

## Build release artifacts

```
./gradlew bundleRelease      # AAB for Play
./gradlew assembleRelease    # APK
```

## Print SHA-256 and signing certificate fingerprint

```
sha256sum app/build/outputs/apk/release/app-release.apk
# Signing cert SHA-256 fingerprint:
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk | grep SHA256
# or from the keystore:
keytool -list -v -keystore $MECHAT_KEYSTORE -alias $MECHAT_KEY_ALIAS | grep SHA256
```

## Publish to a PUBLIC GitHub repository

1. Create a public repo, add it as remote: `git remote add origin git@github.com:<org>/mechat.git`
2. Before pushing, run `scripts/check-no-trackers.sh` and confirm no secrets are staged
   (`git status` must not show `*.jks`, `.env`, `local.properties`).
3. Tag the release: `git tag -a v0.1.0 -m "v0.1.0" && git push origin v0.1.0`
4. Attach the AAB/APK + SHA-256 + cert fingerprint to the GitHub Release.

Note: publishing source publicly is fine; the signing key stays private.
