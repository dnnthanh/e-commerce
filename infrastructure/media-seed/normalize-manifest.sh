#!/usr/bin/env sh
set -eu

SOURCE_MANIFEST="${1:?source manifest is required}"
TARGET_MANIFEST="${2:?target manifest is required}"
: "${MEDIA_SEED_PRODUCT_ID_OFFSET:=1000}"

: > "$TARGET_MANIFEST"

while IFS= read -r relative_path; do
  [ -n "$relative_path" ] || continue

  case "$relative_path" in
    products/[0-9][0-9][0-9][0-9]/*)
      product_path="${relative_path#products/}"
      manifest_id="${product_path%%/*}"
      variant_path="${product_path#*/}"
      numeric_id="$(printf '%s' "$manifest_id" | sed 's/^0*//')"
      [ -n "$numeric_id" ] || numeric_id=0
      product_id=$((numeric_id + MEDIA_SEED_PRODUCT_ID_OFFSET))
      printf 'products/%s/%s\n' "$product_id" "$variant_path" >> "$TARGET_MANIFEST"
      ;;
    *)
      printf '%s\n' "$relative_path" >> "$TARGET_MANIFEST"
      ;;
  esac
done < "$SOURCE_MANIFEST"

printf 'MEDIA_SEED_MANIFEST_NORMALIZED count=%s offset=%s\n' \
  "$(wc -l < "$TARGET_MANIFEST" | tr -d ' ')" \
  "$MEDIA_SEED_PRODUCT_ID_OFFSET"
