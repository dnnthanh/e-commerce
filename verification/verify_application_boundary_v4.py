from pathlib import Path
import re, sys
ROOT=Path(__file__).resolve().parents[1]
BACKEND=ROOT/'backend'
errors=[]

def require(cond,msg):
    if not cond: errors.append(msg)

# Input ports are application contracts, not aliases for HTTP request/response DTOs.
for p in BACKEND.glob('services/*/src/main/java/**/application/port/in/*.java'):
    s=p.read_text(errors='ignore')
    require('.api.api.' not in s, f'input port leaks HTTP/API transport type: {p.relative_to(BACKEND)}')

# Every inbound adapter (HTTP, scheduler, Kafka consumer/listener) depends on input ports, never concrete use cases.
for p in BACKEND.glob('services/*/src/main/java/**/*.java'):
    rel = p.relative_to(BACKEND).as_posix()
    if '/adapter/in/' not in rel:
        continue
    s=p.read_text(errors='ignore')
    require('.application.usecase.' not in s, f'inbound adapter imports concrete use case: {p.relative_to(BACKEND)}')
    require(re.search(r'\.application\.[A-Z]\w*(UseCase|Orchestrator);', s) is None,
            f'inbound adapter imports concrete application class: {p.relative_to(BACKEND)}')


# Application layer may not depend on HTTP/API transport packages.
for p in BACKEND.glob('services/*/src/main/java/**/application/**/*.java'):
    s=p.read_text(errors='ignore')
    require('.api.api.' not in s, f'application layer leaks HTTP/API transport type: {p.relative_to(BACKEND)}')

# Promotion candidate lookup is ordinary relational read/search: Spring Data native SQL, not JdbcClient.
promo_jdbc=BACKEND/'services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/adapter/out/persistence/JdbcPromotionRepositoryAdapter.java'
require(not promo_jdbc.exists(), 'promotion repository still uses raw JDBC for ordinary candidate search')
promo_repo=BACKEND/'services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/adapter/out/persistence/repository/PromotionJpaRepository.java'
require(promo_repo.exists() and 'nativeQuery = true' in promo_repo.read_text(errors='ignore'),
        'promotion candidate search must use Spring Data native SQL')

# New/refactored HTTP boundaries must have MapStruct adapter mappers.
for rel in (
    'services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/adapter/in/web/mapper/PromotionApiMapper.java',
    'services/be-audit-api/src/main/java/com/dnnthanh/marketplace/be/audit/api/adapter/in/web/mapper/AuditApiMapper.java',
    'services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/adapter/in/web/mapper/AuthorizationApiMapper.java',
    'services/be-media-api/src/main/java/com/dnnthanh/marketplace/be/media/api/adapter/in/web/mapper/MediaApiMapper.java',
    'services/be-operations-api/src/main/java/com/dnnthanh/marketplace/be/operations/api/adapter/in/web/mapper/OperationsApiMapper.java',
    'services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/adapter/in/web/mapper/ReviewApiMapper.java',
    'services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/adapter/in/web/mapper/SellerApiMapper.java',
    'services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/adapter/in/web/mapper/SettlementApiMapper.java',
    'services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/in/web/mapper/CartApiMapper.java',
    'services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/mapper/ProductApiMapper.java',
    'services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/in/web/mapper/CheckoutApiMapper.java',
    'services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/adapter/in/web/mapper/InventoryApiMapper.java',
    'services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/adapter/in/web/mapper/NotificationApiMapper.java',
    'services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/adapter/in/web/mapper/SearchApiMapper.java',
):
    p=BACKEND/rel
    require(p.exists() and '@Mapper' in p.read_text(errors='ignore'), f'MapStruct web mapper missing: {rel}')

if errors:
    print('APPLICATION_BOUNDARY_V4=FAIL')
    for e in errors: print(' -',e)
    sys.exit(1)
print('APPLICATION_BOUNDARY_V4=PASS')
