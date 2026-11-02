# MeChat security review (90% hardening pass)

Read [RULES.md](RULES.md).

## 1. Permission audit (merged manifest)

| Permission | Feature that needs it | Runtime-requested? |
|---|---|---|
| INTERNET | wss:// relay + TURN | No (normal) |
| ACCESS_NETWORK_STATE | reconnect on network change | No (normal) |
| CAMERA | QR scan; video calls | Yes — on Scan tap / call start |
| RECORD_AUDIO | voice notes; calls | Yes — on record / call start |
| POST_NOTIFICATIONS | message notifications | Yes (Android 13+) |
| FOREGROUND_SERVICE + _DATA_SYNC / _PHONE_CALL / _MICROPHONE / _CAMERA | background connection; calls | No (service types) |
| RECEIVE_BOOT_COMPLETED | restart connection after reboot | No (system broadcast) |
| REQUEST_IGNORE_BATTERY_OPTIMIZATIONS | only if user taps the button in Settings | Yes — user-initiated |

Removed/not present: **no location, no READ_PHONE_STATE, no contacts, no storage** permissions.

## 2. Dependency audit

No analytics, ads, crash-reporting, Firebase or tracker SDKs. Crypto is libsodium only
(no second crypto framework). All deps listed in `gradle/libs.versions.toml`.
**Vulnerability scan (OWASP dependency-check / `./gradlew dependencyCheckAnalyze`) could
not run in the dev sandbox (no Android SDK/Gradle) — MANUAL STEP before release.**

## 3. Release hardening

- R8 minification + resource shrinking enabled; `isDebuggable=false`; minimal ProGuard rules.

## 4. Logging

`util/Logger` is a no-op in release (BuildConfig.DEBUG gate). No message content, keys,
PINs or IDs are ever logged.

## 5. Exported components

Only the launcher `MainActivity` is exported. `ConnectionService`, `CallForegroundService`,
`BootReceiver` and the `FileProvider` are all `exported=false`. No custom intent filters
beyond the launcher and the system BOOT_COMPLETED.

## 6. Clipboard

Invite codes copied via `SensitiveClipboard`: flagged `IS_SENSITIVE` and auto-cleared after 60s.

## 7. Device integrity

`DeviceIntegrity` shows an informational root/emulator warning only; the app does not block.

## 8. Network

`network_security_config.xml`: cleartext blocked, system CAs only. Certificate pinning
(primary + backup pins, per-deployment) in `ConnectionManager`. No third-party hosts are
contacted; no connectivity probes (no generate_204/gstatic). Fonts are bundled, not
downloadable.
