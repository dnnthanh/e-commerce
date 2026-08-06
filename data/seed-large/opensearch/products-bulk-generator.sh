#!/usr/bin/env sh
set -eu
COUNT="${COUNT:-500000}"
i=1
while [ "$i" -le "$COUNT" ]; do
  printf '{"index":{"_index":"marketplace-products-v1","_id":"%s"}}\n' "$i"
  printf '{"productId":%s,"sellerId":%s,"categoryId":%s,"name":"Product %s","description":"Synthetic search document %s","price":%s,"rating":4.5,"reviewCount":%s,"status":"PUBLISHED","updatedAt":"2026-08-03T00:00:00Z"}\n' "$i" "$((10000+i%500))" "$((1+i%50))" "$i" "$i" "$((100000+i%50000000))" "$((i%500))"
  i=$((i+1))
done
