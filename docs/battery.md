# MeChat 1-hour idle battery test (manual)

Do NOT run this in the sandbox — run on a real phone.

## Procedure

1. Charge to 100%, unplug, enable airplane mode OFF (normal connectivity), disable Wi-Fi scanning.
2. Install MeChat, complete onboarding, leave on ChatList. Do not touch the screen.
3. Record battery % at start (Settings → Battery) and via `adb shell dumpsys battery`.
4. Wait exactly 1 hour. Keep the screen off.
5. Record battery % at end. Also collect: `adb shell dumpsys batterystats` (Doze history),
   and count of WebSocket reconnects in logcat (`ConnectionManager`).

## Expected

- The foreground service keeps one WebSocket; Doze may pause it — the WorkManager
  mailbox fallback (15 min) should deliver messages within ~15 min worst case.
- Drain should be a few % per hour on a modern phone with the screen off.

## If drain is high

- Check reconnect loops (exponential backoff should prevent tight loops).
- Verify the fallback worker is not running more often than every 15 min.
- Consider reducing WebSocket ping interval.

## Privacy note

The persistent notification exists because Android requires it for a foreground
service; it shows no message content. UnifiedPush is a documented future option
and is intentionally NOT implemented now.
