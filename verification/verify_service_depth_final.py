from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
checks = []

def text(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

def expect(name, cond):
    checks.append((name, bool(cond)))

# Return: one canonical aggregate/workflow.
ret = text('backend/services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/domain/model/ReturnRequest.java')
expect('return canonical aggregate supports inspect', 'inspect(' in ret)
expect('return canonical aggregate supports reject', 'reject(' in ret)
expect('return canonical aggregate supports dispute', 'openDispute(' in ret and 'resolveDispute(' in ret)
expect('return canonical aggregate supports disposition', 'InventoryDisposition' in ret)
expect('legacy ReturnCase removed', not (ROOT / 'backend/services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/domain/model/ReturnCase.java').exists())
expect('legacy ReturnCasePort removed', not (ROOT / 'backend/services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/domain/port/ReturnCasePort.java').exists())

# Order operations.
order_uc = text('backend/services/be-order-api/src/main/java/com/dnnthanh/marketplace/be/order/api/application/service/OrderCommandServiceImplement.java')
order = text('backend/services/be-order-api/src/main/java/com/dnnthanh/marketplace/be/order/api/domain/model/MarketplaceOrder.java')
expect('order auto expires unpaid', 'expireUnpaid' in order_uc or 'expireUnpaid' in order)
expect('order seller cancellation', 'cancelSellerOrder' in order_uc or 'cancelSellerOrder' in order)
expect('order guarded ops override', 'manualOverrideCancel' in order_uc or 'manualOverrideCancel' in order)
expect('order domain has no duplicate requireText', order.count('private static String requireText(') == 1)

# Fulfillment multi-package/reconciliation/SLA.
expect('fulfillment allocation use case exists', (ROOT / 'backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/application/service/ShipmentAllocationService.java').exists())
expect('fulfillment reconciliation use case exists', (ROOT / 'backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/application/service/FulfillmentReconciliationService.java').exists())
expect('fulfillment SLA use case exists', (ROOT / 'backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/application/service/FulfillmentSlaService.java').exists())

# Payment: eliminate dual aggregate/persistence path.
payment = text('backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/domain/model/Payment.java')
expect('payment handles provider outcomes canonically', 'applyProviderEvent' in payment)
expect('payment tracks cumulative refunded amount', 'refundedAmount' in payment)
expect('legacy PaymentAggregate removed', not (ROOT / 'backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/domain/model/PaymentAggregate.java').exists())
expect('legacy PaymentAggregatePort removed', not (ROOT / 'backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/domain/port/PaymentAggregatePort.java').exists())

# Settlement lifecycle/ledger.
settle = text('backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/domain/model/SellerSettlementLedger.java')
expect('settlement has closing lifecycle', 'closePeriod' in settle)
expect('settlement has hold/dispute', 'placeHold' in settle and 'releaseHold' in settle)
expect('settlement rebuilds balance from ledger', 'rebuildBalance' in settle)

# Authorization: durable mutation/recovery.
auth = text('backend/services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/application/service/AuthorizationServiceImplement.java')
expect('authorization mutation records durable intent before provider', 'prepareChange' in auth)
expect('authorization reconciles incomplete mutation', 'reconcilePending' in auth)

# Promotion targeting dimensions.
promo = text('backend/services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/domain/model/Promotion.java')
promo_service = text('backend/services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/application/service/PromotionServiceImplement.java')
expect('promotion seller targeting', 'sellerId' in promo or 'sellerIds' in promo)
expect('promotion sku/category targeting', ('skuIds' in promo or 'skuId' in promo) and ('categoryIds' in promo or 'categoryId' in promo))
expect('promotion channel/segment targeting', 'channel' in promo and ('segment' in promo or 'customerSegment' in promo))
expect('promotion evaluation consumes targeting context', 'PromotionContext' in promo_service or 'PromotionEvaluationContext' in promo_service)

# Notification delivery/retry/DLQ/replay ownership.
notify_input = ROOT / 'backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/application/port/in/NotificationDeliveryUseCase.java'
notify_handler = text('backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/application/service/NotificationDeliveryServiceImplement.java')
expect('notification delivery input port exists', notify_input.exists())
expect('notification delivery handler implements input port', 'implements NotificationDeliveryUseCase' in notify_handler)
notify = text('backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/domain/model/NotificationDelivery.java')
expect('notification delivery has retry/dlq states', 'FAILED' in notify and ('DEAD_LETTER' in notify or 'DLQ' in notify))
expect('notification replay use case exists', (ROOT / 'backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/application/service/NotificationReplayService.java').exists())

# Cart canonical aggregate + authoritative checkout validation.
cart = text('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/domain/model/ShoppingCart.java')
expect('cart save for later', 'saveForLater' in cart and 'moveToCart' in cart)
expect('cart item state enum', 'SAVED_FOR_LATER' in cart)
expect('cart checkout validation use case exists', (ROOT / 'backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/application/service/CartCheckoutValidationServiceImplement.java').exists())
expect('legacy Cart aggregate removed', not (ROOT / 'backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/domain/model/Cart.java').exists())

# Review/Seller/Pricing canonical paths.
expect('legacy Review aggregate removed', not (ROOT / 'backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/domain/model/Review.java').exists())
expect('legacy ReviewService removed', not (ROOT / 'backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/application/ReviewService.java').exists())
expect('seller canonical lifecycle used', 'SellerAccount' in text('backend/services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/application/service/SellerLifecycleService.java'))
expect('pricing only one PriceRuleRepositoryPort', len(list((ROOT / 'backend/services/be-pricing-api/src/main/java').rglob('PriceRuleRepositoryPort.java'))) == 1)

# Inventory operational flows.
expect('inventory adjustment use case', (ROOT / 'backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/application/service/InventoryAdjustmentService.java').exists())
expect('inventory transfer use case', (ROOT / 'backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/application/service/InventoryTransferService.java').exists())
expect('inventory reconciliation use case', (ROOT / 'backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/application/service/InventoryReconciliationService.java').exists())

passed = sum(ok for _, ok in checks)
failed = len(checks) - passed
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'\nSummary: {passed} passed, {failed} failed')
sys.exit(1 if failed else 0)
