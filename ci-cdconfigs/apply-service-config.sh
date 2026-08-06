#!/usr/bin/env sh
set -eu

if [ "$#" -lt 2 ] || [ "$#" -gt 3 ]; then
  echo "usage: $0 <service-name> <non-secret-env-file> [namespace]" >&2
  exit 2
fi

service="$1"
env_file="$2"
namespace="${3:-default}"
config_name="${service}-config"

if [ ! -f "$env_file" ]; then
  echo "env file not found: $env_file" >&2
  exit 2
fi

kubectl -n "$namespace" create configmap "$config_name" \
  --from-env-file="$env_file" \
  --dry-run=client \
  -o yaml \
  | kubectl apply -f -
