#!/usr/bin/env sh
set -eu

: "${MINIO_ENDPOINT:=http://minio:9000}"
: "${MINIO_ROOT_USER:=marketplace}"
: "${MINIO_ROOT_PASSWORD:=marketplace-secret}"
: "${MINIO_BUCKET:=marketplace-media}"

mc alias set marketplace "${MINIO_ENDPOINT}" "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}"
mc mb --ignore-existing "marketplace/${MINIO_BUCKET}"
mc anonymous set download "marketplace/${MINIO_BUCKET}"
mc mirror --overwrite /seed-assets/seed "marketplace/${MINIO_BUCKET}/seed"
printf 'MEDIA_SEED_UPLOAD_COMPLETE bucket=%s\n' "${MINIO_BUCKET}"
