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
for snippet in ['selectedMedia', '[src]="selected.url"', '.media-thumb img']:
    require(snippet in product_detail, f'product detail missing real media gallery: {snippet}')

product_card = text('frontend/projects/storefront/src/app/shared/product-card.component.ts')
for snippet in ['productMedia(productId)', 'img', '[src]="imageUrl"']:
    require(snippet in product_card, f'product card missing Media-backed image rendering: {snippet}')

media_response = text(
    'backend/services/be-media-api/src/main/java/com/dnnthanh/marketplace/be/media/api/api/response/VariantView.java'
)
require('String url' in media_response, 'Media public response does not expose browser delivery URL')
media_config = text('backend/services/be-media-api/src/main/resources/application.yml')
require(
    'MINIO_PUBLIC_BASE_URL' in media_config,
    'Media service does not bind the public/browser MinIO endpoint',
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

visual_spec = text('frontend/playwright/storefront.visual.spec.ts')
for forbidden in ['page.route(', 'installBusinessApiMocks']:
    require(forbidden not in visual_spec, f'visual test still mocks business APIs: {forbidden}')
for snippet in ['localhost:9000', 'product-gallery-main img', 'home-real-data-desktop']:
    require(snippet in visual_spec, f'real-data visual evidence missing: {snippet}')

visual_workflow = text('.github/workflows/storefront-visual.yml')
for snippet in ['media-seed', 'seed-postgres-demo', 'Verify Media metadata resolves to a real MinIO object']:
    require(snippet in visual_workflow, f'real-stack visual workflow missing: {snippet}')

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
