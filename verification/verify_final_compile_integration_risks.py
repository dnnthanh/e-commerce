#!/usr/bin/env python3
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
checks=[]
def read(rel):
    p=ROOT/rel
    return p.read_text(errors='ignore') if p.exists() else ''
def exists(rel): return (ROOT/rel).exists()
def check(name, ok): checks.append((name, bool(ok)))

fulfillment_service=read('backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/application/service/FulfillmentServiceImplement.java')
fulfillment_sla=read('backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/application/service/FulfillmentSlaService.java')
fulfillment_adapter=read('backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/adapter/out/persistence/JdbcShipmentRepositoryAdapter.java')
check('fulfillment no deleted transition port dependency', 'FulfillmentTransitionPort' not in fulfillment_service)
check('fulfillment no generic business IllegalArgumentException', 'IllegalArgumentException' not in fulfillment_sla)
check('fulfillment creation event does not call transition mapper for ALLOCATED', 'created ? null : FulfillmentEventType.forShipmentStatus' not in fulfillment_adapter)

return_app=read('backend/services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/application/service/ReturnServiceImplement.java')
create_prefix=return_app.split('public ReturnView create',1)[0][-200:] if 'public ReturnView create' in return_app else ''
check('return remote create is outside DB transaction', '@Transactional' not in create_prefix)

review_adapter=read('backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/adapter/out/persistence/JdbcProductReviewPersistenceAdapter.java')
check('review MySQL insert avoids unsupported RETURNING', 'RETURNING id' not in review_adapter)

catalog_api=read('backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/ProductInternalApi.java')
catalog_view=read('backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/response/SkuOwnerView.java')
catalog_controller=read('backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/ProductInternalController.java')
catalog_mapper=read('backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/mapper/ProductApiMapper.java')
catalog_snapshot_adapter=read('backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/JdbcSkuSnapshotAdapter.java')
check('catalog sku contract exposes active flag', 'boolean active' in catalog_view)
check('catalog sku contract exposes purchase limit', 'purchaseLimit' in catalog_view)
check('catalog controller delegates snapshot transport mapping to MapStruct', 'mapper.toView(snapshotUseCase.get(skuId))' in catalog_controller and 'SkuOwnerView toView(SkuCheckoutSnapshot snapshot)' in catalog_mapper)
check('catalog snapshot adapter reads active/purchase limit', 'purchase_limit' in catalog_snapshot_adapter and 's.active' in catalog_snapshot_adapter)

check('canonical cart cache port exists', exists('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/application/port/out/ShoppingCartCachePort.java'))
check('canonical cart cache document exists', exists('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/cache/CartCacheDocument.java'))
cache_adapter=read('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/cache/RedisCartCacheAdapter.java')
check('cart redis adapter uses MarketplaceCacheManager', 'MarketplaceCacheManager' in cache_adapter)
check('cart mutation use case integrates cache aside', 'ShoppingCartCachePort' in read('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/application/service/CartMutationService.java'))

inventory_master=read('backend/services/be-inventory-api/src/main/resources/db/changelog/db.changelog-master.yaml')
inv_idempotency=read('backend/services/be-inventory-api/src/main/resources/db/changelog/002-ledger-idempotency.sql')
check('inventory ledger idempotency migration wired', '002-ledger-idempotency' in inventory_master)
check('inventory ledger has unique reference+reason guard', 'UNIQUE' in inv_idempotency.upper() and 'reference_key' in inv_idempotency and 'reason' in inv_idempotency)

check('obsolete PaymentStatus enum removed', not exists('backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/domain/enumtype/PaymentStatus.java'))

fail=[n for n,ok in checks if not ok]
for n,ok in checks: print(('PASS' if ok else 'FAIL')+': '+n)
print(f'Summary: {len(checks)-len(fail)} passed, {len(fail)} failed')
sys.exit(1 if fail else 0)
