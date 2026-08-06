from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
checks=[]
def require(rel, tokens):
    p=root/rel
    if not p.exists():
        checks.append((False,f'missing {rel}')); return
    text=p.read_text(errors='ignore')
    for token in tokens:
        checks.append((token in text, f'{rel} contains {token}'))

require('backend/services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/api/PromotionInternalApi.java',
        ['ReservePromotionRequest','confirm','release','@PreAuthorize'])
require('backend/services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/application/service/PromotionCheckoutReservationServiceImplement.java',
        ['usagePort.reserve','usagePort.confirm','usagePort.release'])
require('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/application/port/out/PromotionClientPort.java',
        ['PromotionReservation','reserve','confirm','release'])
require('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/application/service/CheckoutServiceImplement.java',
        ['promotion.reserve','promotion.confirm','promotion.release'])
require('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal/promotion/rest/PromotionRestAdapter.java',
        ['/internal/promotions/reservations','promotionIds'])
failed=[msg for ok,msg in checks if not ok]
for ok,msg in checks:
    print(('PASS' if ok else 'FAIL')+': '+msg)
if failed:
    print(f'Summary: {len(checks)-len(failed)} passed, {len(failed)} failed')
    sys.exit(1)
print(f'Summary: {len(checks)} passed, 0 failed')
