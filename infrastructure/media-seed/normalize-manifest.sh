#!/usr/bin/env sh
set -eu

SOURCE_MANIFEST="${1:?source manifest is required}"
TARGET_MANIFEST="${2:?target manifest is required}"
: "${MEDIA_SEED_PRODUCT_ID_OFFSET:=1000}"

: > "$TARGET_MANIFEST"
seen=''
count=0

append_unique() {
  normalized_path="$1"
  case "
$seen
" in
    *"
$normalized_path
"*)
      return 0
      ;;
  esac

  printf '%s\n' "$normalized_path" >> "$TARGET_MANIFEST"
  if [ -n "$seen" ]; then
    seen="$seen
$normalized_path"
  else
    seen="$normalized_path"
  fi
  count=$((count + 1))
}

while IFS= read -r relative_path; do
  [ -n "$relative_path" ] || continue

  case "$relative_path" in
    products/[0-9][0-9][0-9][0-9]/*)
      product_path="${relative_path#products/}"
      manifest_id="${product_path%%/*}"
      variant_path="${product_path#*/}"

      case "$manifest_id" in
        0*)
          numeric_id="$manifest_id"
          while [ "${numeric_id#0}" != "$numeric_id" ]; do
            numeric_id="${numeric_id#0}"
          done
          [ -n "$numeric_id" ] || numeric_id=0
          product_id=$((numeric_id + MEDIA_SEED_PRODUCT_ID_OFFSET))
          ;;
        *)
          product_id="$manifest_id"
          ;;
      esac

      append_unique "products/$product_id/$variant_path"
      ;;
    *)
      append_unique "$relative_path"
      ;;
  esac
done < "$SOURCE_MANIFEST"

printf 'MEDIA_SEED_MANIFEST_NORMALIZED count=%s offset=%s\n' \
  "$count" \
  "$MEDIA_SEED_PRODUCT_ID_OFFSET"
