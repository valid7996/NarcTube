#!/usr/bin/env sh

DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
cd "$DIR"

if [ -x "$DIR/gradlew" ] && [ -f "$DIR/gradlew" ]; then
  exec "$DIR/gradlew" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle is not installed or the wrapper is missing. Please install Gradle or open this project in Android Studio." >&2
  exit 1
fi
