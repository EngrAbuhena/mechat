# MeChat server deployment guide

Read [RULES.md](RULES.md). The server is a **relay/signalling server only** — it is
end-to-end encrypted at the clients; the server never sees plaintext (RULE 5).

## What the server does

- `wss://…/ws` WebSocket endpoint (Node.js + TypeScript + `ws`, in `/server`)
- Relays WebRTC signalling (offer/answer/ICE) between online peers
- "Blind mailbox": opaque encrypted blobs for offline peers, TTL default 24h (max 7d), delivered once then deleted
- No accounts, no user database, no logging of IPs/peer IDs/payloads — aggregate counters only
- Per-connection rate limit and max blob size (env-configured)

## Deploy

1. `cp .env.example .env` and fill in `SERVER_DOMAIN`, `TURN_REALM`, cert paths. Never commit `.env`.
2. TLS via **Let's Encrypt + Caddy**: Caddyfile uses `{$SERVER_DOMAIN}` and issues certificates automatically; it proxies `wss://…/ws` to the relay container.
3. `docker compose up -d --build`
4. **coturn** runs with TLS on 5349 (`--tls-listening-port`), certs from the same Let's Encrypt directory; clients use `turns:` URLs.

## TURN credentials (short-lived, time-limited HMAC)

coturn supports time-limited credentials: `username = "<expiry-timestamp>:<user>"` with
`password = base64(HMAC-SHA256(secret, username))`, where `secret` is the coturn
`static-auth-secret` (env var, never in source). Clients receive a fresh username/credential
pair per session (valid e.g. 1h); the app loads them from BuildConfig fields fed by
`local.properties`/CI secrets (`MECHTURN_URL`, `MECHTURN_USER`, `MECHTURN_CRED`).
No IP addresses or long-lived passwords are hard-coded anywhere.

## Tests

`cd server && npm ci && npm test` — relay delivery, mailbox TTL expiry/one-time
delivery, and blob size limits. The Dockerfile is written but intentionally not
built in the dev sandbox.
