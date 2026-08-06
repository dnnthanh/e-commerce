#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Ignore an accidentally exported COMPOSE_FILE from another project.
unset COMPOSE_FILE || true
# Keep the remaining independent image builds (frontend/media/exporters/shared backend runtime) conservative on Docker Desktop.
export COMPOSE_PARALLEL_LIMIT="${COMPOSE_PARALLEL_LIMIT:-2}"
exec docker compose --project-directory "$ROOT" -f "$ROOT/docker-compose.yml" "$@"
