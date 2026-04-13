#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

APP_ID="de.thomba.andropicsort"
MAIN_ACTIVITY=".MainActivity"
APK_GLOB="app/build/outputs/apk/debug/*.apk"

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb not found. Set ANDROID_HOME or install Android SDK platform-tools."
  exit 1
fi

# This script intentionally does not call Gradle.
APK_PATH="$(ls -t $APK_GLOB 2>/dev/null | head -n 1 || true)"
if [[ -z "$APK_PATH" ]]; then
  echo "ERROR: No existing debug APK found under app/build/outputs/apk/debug/."
  echo "Build once first (for example with ./scripts/dev-run.sh or ./gradlew :app:assembleDebug)."
  exit 1
fi

DEVICE="${1:-}"
if [[ -z "$DEVICE" ]]; then
  DEVICE="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
fi

if [[ -z "$DEVICE" ]]; then
  echo "ERROR: No running device/emulator found."
  echo "Start an emulator from Android Studio Device Manager, then rerun this script."
  exit 1
fi

echo "==> Installing existing APK on $DEVICE"
echo "    $APK_PATH"
adb -s "$DEVICE" install -r "$APK_PATH" >/dev/null

echo "==> Launching app"
adb -s "$DEVICE" shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/dev/null

echo "Done. App is running on $DEVICE (without rebuild)."

