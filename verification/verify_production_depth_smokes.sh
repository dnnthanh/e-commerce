#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="${BUILD_DIR:-/tmp/ecommerce-production-depth-classes}"
cd "$ROOT_DIR"

SOURCE_PATH="$(find backend -type d -path '*/src/main/java' -print | paste -sd: -)"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

javac \
  -sourcepath "verification/src:$SOURCE_PATH" \
  -d "$BUILD_DIR" \
  verification/src/ProductionDepthDomainSmoke.java \
  verification/src/CrossContextWorkflowSmoke.java

java -cp "$BUILD_DIR" ProductionDepthDomainSmoke
java -cp "$BUILD_DIR" CrossContextWorkflowSmoke
