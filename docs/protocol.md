# MeChat message protocol

Read [RULES.md](RULES.md). All cryptography uses **libsodium primitives only** (RULE 3);
we invent no new primitives.

## Session setup (from the invite's exchanged public keys)

X3DH-style, X25519 only:
`shared = HKDF( DH(idPriv_A, SPK_B) ‖ DH(ratchetPriv_A, SPK_B) ‖ DH(idPriv_A, KX_B) )`
→ root key → initial chain keys. (libsignal was considered; the libsodium Double
Ratchet below is used for a smaller, auditable dependency surface.)

## Double Ratchet

- DH ratchet on X25519 (`crypto_scalarmult`); KDF = HKDF-SHA256 (RFC 5869 over HMAC-SHA256)
- AEAD per message: **XChaCha20-Poly1305** (`crypto_aead_xchacha20poly1305_ietf`),
  unique random 24-byte nonce per message — never reused
- Sender Ed25519 signature over the envelope; sequence number per ratchet key
- Replay protection: sequence numbers, reject `seq < recvSeq`; bounded skipped-key
  store (max 64) gives out-of-order delivery
- Forward secrecy / post-compromise: each ratchet step derives fresh root+chain keys,
  so a later key compromise does not decrypt older messages

## Envelope format (versioned)

```
[0]      version = 0x01
[1..33)  sender ratchet public key (X25519, 32B)
[33..41) sequence number (uint64 BE)
[41..65) nonce (24B)
[65..]   XChaCha20-Poly1305 ciphertext || 16B tag   (AAD = header bytes 0..65)
signature: Ed25519 over the full envelope (appended separately in the wire message)
```

The version byte allows future upgrades (e.g. a new AEAD or hybrid PQ KEX).

## Optional future upgrade (NOT default, NOT claimed)

Hybrid post-quantum key exchange (X25519 + ML-KEM) may be added as a future version.
Until audited and shipped, the app is **not** "quantum secure" (RULE 5).

## Wire messages

Client → server: `Hello`, `Signal` (encrypted offer/answer/ICE), `MailboxPut`, `MailboxGet`.
All payloads are opaque ciphertext to the relay.

## Offline delivery (blind mailbox)

- When the contact is offline, the sender uploads the encrypted envelope via
  `MailboxPut` addressed to `mailboxId = base64url(HKDF(contactPubKey, conversationSecret))`
  — the server cannot link mailboxes to identities or to each other.
- On start/reconnect the receiver sends `MailboxGet`, downloads blobs, decrypts,
  stores locally, then ACKs; the server deletes each blob after delivery (once).
- Retry with exponential backoff; dedupe by message ID (a message never appears twice).
- Message sizes are padded to buckets of **256 B / 1 KiB / 4 KiB** to reduce size leakage.

## What the server can still see

The relay sees, per connection: the client **IP address**, **timing** of connections
and uploads/downloads, and the **padded size bucket** of each blob. It never sees
plaintext, peer identities, or which conversations a mailbox belongs to.
