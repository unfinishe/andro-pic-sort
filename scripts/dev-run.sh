#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

APP_ID="de.thomba.andropicsort"
MAIN_ACTIVITY=".MainActivity"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [[ -z "${JAVA_HOME:-}" ]] && [[ -d "$ROOT_DIR/.tools/jdk-21.0.10+7" ]]; then
  export JAVA_HOME="$ROOT_DIR/.tools/jdk-21.0.10+7"
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb not found. Set ANDROID_HOME or install Android SDK platform-tools."
  exit 1
fi

echo "==> Building debug APK"
./gradlew --no-daemon :app:assembleDebug

if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found at $APK_PATH"
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

echo "==> Installing APK on $DEVICE"
adb -s "$DEVICE" install -r "$APK_PATH" >/dev/null

echo "==> Launching app"
adb -s "$DEVICE" shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/dev/null

echo "Done. App is running on $DEVICE"

