#!/bin/sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

exec "$SCRIPT_DIR/backend/mvnw" -f "$SCRIPT_DIR/backend/pom.xml" "$@"
