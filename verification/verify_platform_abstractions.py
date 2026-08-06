from pathlib import Path
import re, sys
root=Path(__file__).resolve().parents[1]
fail=[]

def text(rel):
    p=root/rel
    if not p.exists():
        fail.append(f'missing:{rel}')
        return ''
    return p.read_text(errors='ignore')

all_java='\n'.join(p.read_text(errors='ignore') for p in (root/'backend').rglob('*.java'))
if 'LoggerFactory.getLogger' in all_java or re.search(r'private\s+static\s+final\s+Logger\b', all_java):
    fail.append('manual SLF4J logger declaration remains; use Lombok @Slf4j')

cache_manager=text('backend/platform/be-platform-cache-starter/src/main/java/com/dnnthanh/marketplace/be/platform/cache/MarketplaceCacheManager.java')
for token in ['<T> Optional<T> get(', '<T> void put(', 'void evict(']:
    if token not in cache_manager:
        fail.append(f'generic cache helper missing:{token}')
cart=text('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/cache/RedisCartCacheAdapter.java')
if 'MarketplaceCacheManager' not in cart:
    fail.append('cart adapter does not use generic MarketplaceCacheManager')
if 'org.springframework.cache.Cache' in cart or 'org.springframework.cache.CacheManager' in cart:
    fail.append('cart adapter still handles Spring Cache plumbing directly')

base_consumer=text('backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/kafka/BaseDomainEventConsumer.java')
if 'abstract class BaseDomainEventConsumer' not in base_consumer or 'protected final void consume(' not in base_consumer:
    fail.append('BaseDomainEventConsumer template missing')
base_producer=text('backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/kafka/BaseKafkaProducer.java')
if 'abstract class BaseKafkaProducer' not in base_producer or 'KafkaTemplate<String, Object>' not in base_producer:
    fail.append('BaseKafkaProducer missing')
producer=text('backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/kafka/DomainEventProducer.java')
if 'extends BaseKafkaProducer<DomainEvent>' not in producer:
    fail.append('DomainEventProducer does not extend BaseKafkaProducer')

listener_signature=re.compile(r'@KafkaListener\([^)]*\)\s*(?:@Transactional\s*)?public\s+[^\s]+\s+\w+\s*\(([^)]*)\)', re.S)
for p in (root/'backend/services').rglob('*.java'):
    t=p.read_text(errors='ignore')
    if '@KafkaListener' not in t:
        continue
    if 'extends BaseDomainEventConsumer' not in t:
        fail.append(f'kafka consumer does not extend base:{p.relative_to(root)}')
    for match in listener_signature.finditer(t):
        args=match.group(1).strip()
        first=args.split(',')[0].strip() if args else ''
        if re.match(r'(?:final\s+)?String\b', first):
            fail.append(f'raw-string kafka listener:{p.relative_to(root)}')
    if 'ObjectMapper' in t or 'readTree(' in t or 'readValue(' in t:
        fail.append(f'kafka listener contains manual JSON mapping:{p.relative_to(root)}')
    if re.search(r'@Transactional\s+(?:\n\s*)?(?:private|protected)\s', t):
        fail.append(f'kafka listener has non-proxied transactional helper:{p.relative_to(root)}')
    if 'RestClient' in t and re.search(r'@KafkaListener[\s\S]{0,300}@Transactional|@Transactional[\s\S]{0,300}@KafkaListener', t):
        fail.append(f'kafka listener may hold transaction across internal HTTP:{p.relative_to(root)}')

for p in (root/'backend/services').rglob('*.java'):
    if 'KafkaTemplate' in p.read_text(errors='ignore'):
        fail.append(f'service bypasses BaseKafkaProducer/DomainEventProducer:{p.relative_to(root)}')

job=text('backend/services/be-checkout-job/src/main/java/com/dnnthanh/marketplace/be/checkout/job/scheduler/CheckoutSagaRecoveryJob.java')
if 'CheckoutRecoveryRestAdapter' not in job:
    fail.append('checkout recovery job missing CheckoutRecoveryRestAdapter')
if 'KafkaTemplate' in job:
    fail.append('checkout recovery job still publishes ad-hoc Kafka command')

review=text('backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/api/ReviewApi.java')
if 'ReviewSummary' not in review or '/reviews/summary' not in review:
    fail.append('review summary API/read model missing')

if fail:
    print('PLATFORM_ABSTRACTIONS_FAIL')
    for item in fail:
        print('-', item)
    sys.exit(1)
print('PLATFORM_ABSTRACTIONS_PASS')
