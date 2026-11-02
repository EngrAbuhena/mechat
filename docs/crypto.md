# MeChat crypto design

Read [RULES.md](RULES.md) first. We never write our own cryptography (RULE 3).

## Library

- **libsodium** via `com.goterl.lazysodium:lazysodium-android:5.1.0` (native libsodium bundled in the AAR; no network, no Play services). JVM unit tests use `lazysodium-java`.

## Algorithms

| Purpose | Algorithm | API |
|---|---|---|
| Identity signing | **Ed25519** | `Sodium.cryptoSignKeyPair()` |
| Key agreement | **X25519** (Curve25519) | `Sodium.cryptoBoxKeyPair()` |
| Public identity ID | `base64url(SHA-256(signing public key))`, no padding | `Sodium.cryptoHashSha256()` |
| At-rest private key protection | **AES-256-GCM** (128-bit tag, random 96-bit IV prepended) with the key held only in the **Android Keystore** | `KeystoreKeyProvider` (StrongBox preferred on API 28+, graceful fallback to TEE) |

## Rules honoured

- Private keys are **never stored unencrypted**: `IdentityStore` persists only the Keystore-wrapped blob (DataStore). Raw key bytes exist only in memory.
- No device identifiers: the public ID is derived solely from the public signing key. We do NOT read ANDROID_ID, Build.FINGERPRINT, IMEI, MAC, etc.
- No secrets in source; nothing about keys is logged (RULE 4).
- Honest claims only (RULE 5): the app is end-to-end encrypted over relay/signalling servers; nothing here claims "unhackable" or "quantum secure".

## KeyInfo

`KeystoreKeyProvider.keyInfo()` returns the Android Keystore `KeyInfo`; `isInsideSecureHardware` is shown in the debug section of Settings.

## Tests

`app/src/test/.../CryptoTest.kt`: key generation, AES-GCM wrap/unwrap round trip, tampered ciphertext rejected (`AEADBadTagException`), wrong key rejected.
