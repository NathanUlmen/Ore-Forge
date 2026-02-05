#!/usr/bin/env bash
set -euo pipefail

mkdir -p "$(pwd)/captures"

# Adjust JAVA_BIN if you want a specific JDK
JAVA_BIN="${JAVA_BIN:-java}"

# Your classpath / main class / args:
# Replace these with what your IntelliJ config uses.
CP="Ore-Forge.lwjgl3.main"
MAIN="ore.forge.lwjgl3.Lwjgl3Launcher"
APP_ARGS=("$@")

renderdoccmd capture \
  --wait-for-exit \
  --working-dir "$(pwd)" \
  --capture-file "$(pwd)/captures/cap.rdc" \
  -- \
  "$JAVA_BIN" -cp "$CP" "$MAIN" "${APP_ARGS[@]}"
