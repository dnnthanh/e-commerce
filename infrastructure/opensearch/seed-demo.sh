#!/usr/bin/env sh
set -eu
URL="${OPENSEARCH_URL:-http://opensearch:9200}"
INDEX="${OPENSEARCH_PRODUCT_INDEX:-marketplace-products-v1}"

# Small visible search dataset aligned conceptually with relational demo IDs.
awk -v index="$INDEX" 'BEGIN {
  split("Nova X|WorkMate Pro|Orbit Tab|Pulse Buds|Zenix GaN|Kite Keys|Kite Mouse|Vista Pro|Nimbus Hub|Vista Cam|Linka Mesh|Vault SSD",family,"|");
  for (i=1;i<=120;i++) {
    category=1+((i-1)%12); product=1000+i; seller=10000+category;
    name=family[category] " " (1+int((i-1)/12));
    price=(category==1?7990000:category==2?15990000:category==3?8490000:category==8?3990000:990000)+(i%10)*50000;
    rating=3.6+((i*7)%14)/10; if (rating>5) rating=5;
    reviews=8+((i*37)%480);
    status=(i==120?"SUSPENDED":"PUBLISHED");
    print "{\"index\":{\"_index\":\"" index "\",\"_id\":\"" product "\"}}";
    printf "{\"productId\":%d,\"sellerId\":%d,\"categoryId\":%d,\"name\":\"%s\",\"description\":\"Sản phẩm demo với thông số và bảo hành theo từng danh mục.\",\"price\":%d,\"rating\":%.1f,\"reviewCount\":%d,\"status\":\"%s\",\"updatedAt\":\"2026-08-01T12:00:00Z\"}\n",product,seller,category,name,price,rating,reviews,status;
  }
}' | curl -fsS -H 'Content-Type: application/x-ndjson' -XPOST "$URL/_bulk?refresh=true" --data-binary @- >/dev/null

echo "seeded 120 documents into $INDEX"
