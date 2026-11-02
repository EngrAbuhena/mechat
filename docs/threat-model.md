# MeChat threat model

Read [RULES.md](RULES.md). Honest limits only (RULE 5) — no "unhackable", no "zero metadata".

## What MeChat protects

- **Message content**: end-to-end encrypted (Double Ratchet over libsodium: X25519 +
  HKDF + XChaCha20-Poly1305, Ed25519 signatures). The relay/signalling server and TURN
  see only ciphertext.
- **No identity**: no phone number, email, username or account. Identity = device-generated
  keys; the public ID is a hash of the public key. No device identifiers are read.
- **Local data**: SQLCipher-encrypted database (raw random key in Android Keystore),
  private keys wrapped by Keystore AES-256-GCM, `allowBackup=false`, everything excluded
  from backup/transfer. Panic PIN and 10-failure auto-wipe erase local data.
- **App lock**: Argon2id-hashed PIN (6–12 digits, PIN-only, no biometrics), lockout with
  increasing delays, FLAG_SECURE against Recents/screenshots (toggle, default ON).

## What MeChat does NOT protect against

- **Server-seen metadata**: the relay sees your **IP address**, **timing**, and the
  **padded message size bucket** (256 B / 1 KiB / 4 KiB). TURN sees relayed call traffic.
  Use a VPN/Tor for metadata protection. MeChat is not anonymous.
- **Screenshots/copying by the other person**: disappearing messages cannot stop that.
- **A compromised phone**: malware/root can read the screen or memory; the app shows an
  informational warning but does not block.
- **Malicious contacts**: a contact can share what you sent them; safety numbers help you
  verify keys out of band.
- **Device forensics before a wipe**: a panic wipe removes local files and Keystore aliases,
  but cannot defeat memory acquisition (cold boot, chip-off) or coerced disclosure by a
  trained attacker.
- **Supply chain / implementation bugs**: the app is **not independently audited**.

## Future (documented, not shipped)

- UnifiedPush as an alternative push channel; hybrid X25519+ML-KEM key exchange.
  Neither is claimed today — the app is not "quantum-secure".
