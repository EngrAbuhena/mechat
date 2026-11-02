# Contributing to MeChat

1. Read [docs/RULES.md](docs/RULES.md) — it is binding for every change.
2. Keep changes minimal; every step must compile and run.
3. Never add analytics/ads/crash SDKs, trackers, or new permissions without discussion.
4. Crypto only via well-known libraries (libsodium / Android Keystore) — never hand-rolled.
5. Run `./gradlew testDebugUnitTest`, Android lint, and `scripts/check-no-trackers.sh`
   before opening a PR.
6. Do not commit secrets, keystores or `.env` files.
