# MeChat call testing — manual two-phone checklist

Group calls are **out of scope**. Do NOT test on sandbox devices; use two real phones.

## Setup

1. Both phones install MeChat, complete onboarding, add each other via invite QR.
2. Grant CAMERA + RECORD_AUDIO **only when the call starts** (requested at call time).
3. Server reachable via wss:// with coturn TURN (TLS) configured.

## Checklist

- [ ] Voice call: A calls B → B sees full-screen ringing UI with safety number → accept → audio both ways → end.
- [ ] Video call: enable video → camera preview, switch camera works, mute/speaker toggles.
- [ ] Decline: B declines → A sees "Call ended", no crash.
- [ ] Permission denial: deny CAMERA/RECORD_AUDIO → call continues gracefully (voice-only / error toast), no crash.
- [ ] Safety number shown on call screen matches the one in chat (compare out of band).
- [ ] "Always relay calls (TURN)" ON → call still connects; verify via TURN logs that traffic is relayed (contact cannot see A's IP).
- [ ] Network switch (Wi-Fi → mobile) mid-call → ICE restart / reconnect or clean drop.
- [ ] App backgrounded during call → foreground service keeps call alive (Android 14+ phoneCall type).
- [ ] Offline contact → call shows "unavailable" and offers mailbox message instead.

## Known limitations

- 1:1 calls only; no group calls.
- If both peers are offline, no call is possible (mailbox is for messages, not realtime media).
- TURN relay adds latency; direct P2P is preferred when both are online.
- Media is SRTP/DTLS end-to-end, but metadata (IP, timing) is visible to the relay unless "always relay" is used or a VPN/Tor is active.
