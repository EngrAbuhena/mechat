#!/usr/bin/env bash
# Scans the built APK and dependency tree for known tracker/analytics packages.
# Fails (exit 1) if any are found. RULE 2: no analytics, ads, crash-reporting, Firebase.
set -u
APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
DEPS_DIR="${2:-app/build/intermediates}"

TRACKERS=(
  "com.google.firebase"
  "com.google.android.gms"
  "com.google.android.libraries.places"
  "com.google.tagmanager"
  "analytics"
  "crashlytics"
  "appsflyer"
  "adjust"
  "facebook"
  "amplitude"
  "mixpanel"
  "sentry"
  "bugsnag"
  "segment"
  "branch.io"
  "onesignal"
  "urbanairship"
)

fail=0
if [ -f "$APK" ]; then
  echo "Scanning APK: $APK"
  for t in "${TRACKERS[@]}"; do
    if unzip -l "$APK" 2>/dev/null | grep -qi "$t"; then
      echo "TRACKER FOUND in APK: $t"; fail=1
    fi
  done
else
  echo "APK not found ($APK) — scanning dependency tree only."
fi

echo "Scanning dependency tree: $DEPS_DIR"
for t in "${TRACKERS[@]}"; do
  if [ -d "$DEPS_DIR" ] && grep -rqi "$t" "$DEPS_DIR" 2>/dev/null; then
    echo "TRACKER FOUND in dependencies: $t"; fail=1
  fi
done

if [ "$fail" -eq 0 ]; then
  echo "OK: no known tracker packages found."
else
  echo "FAILED: tracker packages detected." >&2
fi
exit $fail
