from pathlib import Path
import re, sys
root=Path(__file__).resolve().parents[1]
fail=[]

def read(rel):
    p=root/rel
    if not p.exists():
        fail.append(f'missing:{rel}')
        return ''
    return p.read_text(errors='ignore')

search=read('backend/services/be-search-worker/src/main/java/com/dnnthanh/marketplace/be/search/worker/SearchProjectionConsumer.java')
if re.search(r'@KafkaListener[\s\S]{0,300}\bonCatalog\s*\(\s*String\b', search):
    fail.append('search-worker consumes raw String instead of DomainEvent')
if 'DomainEvent' not in search:
    fail.append('search-worker missing typed DomainEvent')

order=read('backend/services/be-order-worker/src/main/java/com/dnnthanh/marketplace/be/order/worker/PaymentStatusConsumer.java')
if re.search(r'@KafkaListener[\s\S]{0,400}\bhandle\s*\(\s*String\b', order):
    fail.append('order-worker consumes raw String instead of DomainEvent')
if 'ObjectMapper' in order or 'readTree' in order:
    fail.append('order-worker manually deserializes Kafka payload')

cart=read('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/cache/RedisCartCacheAdapter.java')
if 'readValue(value,Cart.class)' in cart.replace(' ', '') or 'readValue(value, Cart.class)' in cart:
    fail.append('cart cache deserializes rich aggregate directly instead of cache DTO')
if 'CartCacheDocument' not in cart:
    fail.append('cart cache missing explicit cache DTO/document')

job=read('backend/services/be-checkout-job/src/main/java/com/dnnthanh/marketplace/be/checkout/job/scheduler/CheckoutSagaRecoveryJob.java')
if 'KafkaTemplate' in job:
    fail.append('checkout recovery job publishes command without a matching typed recovery worker')
if 'CheckoutRecoveryRestAdapter' not in job:
    fail.append('checkout job does not call authenticated recovery boundary')

review_api=read('backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/api/ReviewApi.java')
if 'ReviewSummary' not in review_api:
    fail.append('review API missing summary read model for search projection')

if fail:
    print('RUNTIME_FLOW_DEPTH_FAIL')
    for x in fail: print('-',x)
    sys.exit(1)
print('RUNTIME_FLOW_DEPTH_PASS')
