# MeChat — GLOBAL RULES

Read this file at the start of every step.

1. No phone number, email, username or account. Identity = keys generated on the device.
2. No analytics, ads, crash-reporting, Firebase or any tracker SDK. No location permission. No contacts permission. Ask for the minimum permissions only, at the moment they are needed.
3. Never write our own cryptography. Use well-known libraries (libsodium via lazysodium-android, or Google Tink, or libsignal) and Android Keystore.
4. No secrets, passwords or keys in source code. Never log message content, keys, PINs or IDs.
5. Be honest in docs and UI: the app is end-to-end encrypted and uses relay/signalling servers. Do not claim "no server", "unhackable" or "quantum secure".
6. Every step must compile and run. Keep previous work; only change what the step needs.
7. All colors, fonts, corner radii and spacing come ONLY from the theme in ui/theme/. No hard-coded colors, font names or magic numbers inside screens. Fonts are bundled in res/font; never use downloadable fonts (they contact Google).
