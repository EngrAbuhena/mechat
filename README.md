# MeChat

An identity-free, end-to-end encrypted private messenger for Android.

**Not independently audited.** No claims of being "unhackable", "military-grade",
"quantum-secure" or "zero metadata" — see [docs/threat-model.md](docs/threat-model.md)
for exactly what is and is not protected.

## Features

- No account: identity = keys generated on your device (Ed25519 + X25519, libsodium)
- Double Ratchet messaging (X25519 + HKDF + XChaCha20-Poly1305 + Ed25519 signatures)
- One-time invite QR codes (24h expiry, single use), safety numbers for verification
- WebRTC 1:1 voice/video calls (SRTP/DTLS), optional TURN relay to hide your IP
- Offline "blind mailbox" delivery (server cannot link mailboxes to identities)
- Disappearing messages, encrypted attachments (EXIF stripped), voice notes
- App lock with 6–12 digit PIN (Argon2id), panic PIN wipe, auto-wipe after 10 failures
- Background connection via user-controlled foreground service — no Firebase/Google push
- Privacy-safe notifications (default: "New message" only)

Screenshots: _(placeholders — add before release)_

## Build the app

```
./gradlew assembleDebug          # debug
./gradlew assembleRelease        # R8-minified release
./gradlew testDebugUnitTest      # unit tests
```

Requires Android Studio, JDK 17, Android SDK 36. Fonts are bundled in `app/src/main/res/font`.

## Self-host the server

See [docs/server.md](docs/server.md): Docker + docker-compose with Caddy (Let's Encrypt,
wss://) and coturn (TURN over TLS). Configure via `.env` — nothing hard-coded.

## Security model

See [docs/protocol.md](docs/protocol.md) and [docs/crypto.md](docs/crypto.md).

## Known limitations

- 1:1 chats and calls only (no groups yet); no multi-device sync
- Server sees IP/timing/size bucket (use VPN/Tor); not anonymous
- Disappearing messages can't stop screenshots; panic wipe can't stop memory forensics
- Not independently audited

## Contributing / reporting

See [CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md).
