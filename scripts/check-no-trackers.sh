#!/usr/bin/env bash
# Scans the built APK (dex) for known tracker/analytics packages. RULE 2: no analytics/ads/crash reporting/Firebase.
set -u
APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
TRACKERS=(com/google/firebase com/google/android/gms com/google/tagmanager analytics crashlytics appsflyer adjust/io com/facebook amplitude mixpanel sentry bugsnag segment branch onesignal urbanairship)
fail=0
[ -f "$APK" ] || { echo "APK not found"; exit 1; }
for t in "${TRACKERS[@]}"; do
  if unzip -p "$APK" "classes*.dex" 2>/dev/null | strings | grep -qi "$t"; then echo "TRACKER FOUND in APK dex: $t"; fail=1; fi
done
[ "$fail" -eq 0 ] && echo "OK: no known tracker packages found." || { echo "FAILED" >&2; exit 1; }
exit "$fail"
