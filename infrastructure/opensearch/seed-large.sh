#!/usr/bin/env sh
set -eu
URL="${OPENSEARCH_URL:-http://opensearch:9200}"
INDEX="${OPENSEARCH_PRODUCT_INDEX:-marketplace-products-v1}"
HOT_PRODUCT_RATIO=8
TOTAL="${OPENSEARCH_LARGE_PRODUCT_COUNT:-500000}"
BATCH=10000
start=1
while [ "$start" -le "$TOTAL" ]; do
  end=$((start+BATCH-1)); [ "$end" -gt "$TOTAL" ] && end="$TOTAL"
  awk -v index="$INDEX" -v start="$start" -v end="$end" -v hotRatio="$HOT_PRODUCT_RATIO" 'BEGIN {
    split("Nova X|WorkMate Pro|Orbit Tab|Pulse Buds|Zenix GaN|Kite Keys|Kite Mouse|Vista Pro|Nimbus Hub|Vista Cam|Linka Mesh|Vault SSD",family,"|");
    for (i=start;i<=end;i++) {
      x=i%100;
      category=x<18?1:x<32?2:x<45?3:x<55?4:x<65?5:x<73?6:x<80?7:x<87?8:x<91?9:x<95?10:x<98?11:12;
      seller=x<55?10001+(i%20):x<85?10021+(i%80):10101+(i%400);
      product=1000000+i; hot=x<hotRatio;
      name=family[category] " " (1+(i%24)) (hot?" Pro":"");
      base=category==1?7990000:category==2?15990000:category==3?8490000:category==8?3990000:category==12?1590000:990000;
      price=base+(i%23)*50000; rating=3.4+((i*17)%17)/10; if(rating>5)rating=5;
      reviews=hot?250+((i*37)%4500):((i*37)%180);
      status=i%1000<930?"PUBLISHED":i%1000<960?"DRAFT":i%1000<985?"SUSPENDED":"ARCHIVED";
      print "{\"index\":{\"_index\":\"" index "\",\"_id\":\"" product "\"}}";
      printf "{\"productId\":%d,\"sellerId\":%d,\"categoryId\":%d,\"name\":\"%s\",\"description\":\"Marketplace product with category-specific warranty and delivery SLA.\",\"price\":%d,\"rating\":%.1f,\"reviewCount\":%d,\"status\":\"%s\",\"updatedAt\":\"2026-08-01T12:00:00Z\"}\n",product,seller,category,name,price,rating,reviews,status;
    }
  }' | curl -fsS -H 'Content-Type: application/x-ndjson' -XPOST "$URL/_bulk?refresh=false" --data-binary @- >/dev/null
  start=$((end+1))
done
curl -fsS -XPOST "$URL/$INDEX/_refresh" >/dev/null
echo "seeded $TOTAL realistic documents into $INDEX (HOT_PRODUCT_RATIO=$HOT_PRODUCT_RATIO%)"
