#!/bin/sh
# Repository-local Maven bootstrap. It intentionally keeps the project build reproducible even on
# machines that do not have Maven installed. Maven itself is downloaded only when needed.
set -eu
MAVEN_VERSION="3.9.14"
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_DIR="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="$DIST_DIR/apache-maven-${MAVEN_VERSION}"
ARCHIVE="$DIST_DIR/apache-maven-${MAVEN_VERSION}-bin.zip"
URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$DIST_DIR"
  if [ ! -f "$ARCHIVE" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl --fail --location --retry 3 --output "$ARCHIVE" "$URL"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ARCHIVE" "$URL"
    else
      echo "Neither curl nor wget is available to bootstrap Maven ${MAVEN_VERSION}." >&2
      exit 1
    fi
  fi
  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$ARCHIVE" -d "$DIST_DIR"
  else
    echo "unzip is required to bootstrap Maven ${MAVEN_VERSION}." >&2
    exit 1
  fi
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
