#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$REPO_ROOT"

# The Android app module is not scaffolded yet. Until the Gradle wrapper exists,
# repository verification is a no-op so documentation-only work is not blocked.
if [[ ! -x "./gradlew" ]]; then
  echo "Gradle wrapper not found at $REPO_ROOT/gradlew."
  echo "Skipping build verification: the Android app is not scaffolded yet."
  echo "==> Repository verification skipped (pre-scaffold)"
  exit 0
fi

echo "==> Verifying Android app (lint + unit tests + debug assemble)"
./gradlew lintDebug testDebugUnitTest assembleDebug

echo "==> Repository verification completed"
