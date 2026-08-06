#!/usr/bin/env sh
set -eu
: "${MODULE:?MODULE environment variable is required}"
JAR="/app/services/${MODULE}.jar"
if [ ! -f "$JAR" ]; then
  echo "Missing built service artifact: $JAR" >&2
  exit 64
fi
exec java ${JAVA_OPTS:-} -jar "$JAR"
