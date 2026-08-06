from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
errors = []


def require(condition, message):
    if not condition:
        errors.append(message)


def text(path):
    target = root / path
    if not target.exists():
        errors.append(f'missing file: {path}')
        return ''
    return target.read_text(errors='ignore')


api = text('frontend/shared/marketplace-api.service.ts')
for snippet in [
    'productOffers(productId: number)',
    'offer(skuId: number)',
    '/products/${productId}/offers',
    '/products/offers/${skuId}',
]:
    require(snippet in api, f'missing storefront offer facade contract: {snippet}')

routes = text('frontend/projects/storefront/src/app/app.routes.ts')
require("path: 'category/:id'" in routes, 'missing category storefront route')

product_detail = text('frontend/projects/storefront/src/app/features/product-detail.component.ts')
for snippet in ['ProductOfferView', 'selectedOffer']:
    require(snippet in product_detail, f'product detail missing real offer selection: {snippet}')
require(
    'SKU dùng cho pricing/cart' not in product_detail,
    'product detail still exposes diagnostic SKU input',
)

checkout = text('frontend/projects/storefront/src/app/features/checkout.component.ts')
for snippet in [
    'readonly validation = signal<CartValidation | undefined>(undefined)',
    'const validation = await this.marketplace.validateCart()',
    'if (!validation.valid) return',
]:
    require(snippet in checkout, f'checkout missing final cart-validation guard: {snippet}')

order_detail = text('frontend/projects/storefront/src/app/features/order-detail.component.ts')
require(
    "return ['CREATED', 'PAYMENT_PENDING'].includes(order.status)" in order_detail,
    'customer cancellation is not limited to domain-supported unpaid states',
)
require(
    "cancelOrder(this.orderNo, 'CUSTOMER_REQUEST')" in order_detail,
    'customer cancellation must use the supported CUSTOMER_REQUEST reason',
)
for invalid_reason in ['ADDRESS_ISSUE', 'PAYMENT_ISSUE']:
    require(
        invalid_reason not in order_detail,
        f'order detail exposes unsupported cancellation reason: {invalid_reason}',
    )

storefront_root = root / 'frontend/projects/storefront/src'
storefront_text = '\n'.join(
    path.read_text(errors='ignore')
    for path in storefront_root.rglob('*.ts')
    if path.is_file()
)
require("'/internal/" not in storefront_text, 'storefront TypeScript calls /internal/**')
require('"/internal/' not in storefront_text, 'storefront TypeScript calls /internal/**')

coverage = text('frontend/COVERAGE-MATRIX.md')
require(
    'Product detail therefore requires a SKU identifier' not in coverage,
    'frontend coverage matrix still documents the diagnostic SKU gap',
)

if errors:
    print('STOREFRONT_CORE_FLOW_V14=FAIL')
    for error in errors:
        print('-', error)
    sys.exit(1)

print('STOREFRONT_CORE_FLOW_V14=PASS')
