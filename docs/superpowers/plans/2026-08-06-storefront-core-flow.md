# Storefront Core Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the Angular storefront from a diagnostic client into a coherent customer journey from discovery through Product Detail, Cart, Checkout, Orders and Tracking, with only the narrow Catalog backend additions needed to expose safe sellable SKU/offer data.

**Architecture:** Keep the existing `ApiService` + `MarketplaceApiService` frontend boundary and the existing Catalog hexagonal structure. Add one Catalog read projection for public sellable offers, then compose customer-facing Angular pages around typed shared primitives and a storefront cart store that enriches cart identifiers with public Catalog offer metadata. Pricing, Inventory, Cart, Checkout and Order remain authoritative for their existing business rules.

**Tech Stack:** Java 25, Spring Boot 4.1.0, Spring Data/JdbcClient, MapStruct, JUnit 5/Mockito, Angular 22 standalone components, TypeScript 6, Keycloak JS 26, GitHub Actions.

## Global Constraints

- Start from `.agent/specs/014-storefront-core-flow.md` on `develop` commit `836247e7415a05255a2d501f7e7fec5a396a2bd6`.
- Implementation branch: `feature/storefront-core-flow-implementation`.
- Browser code must never call `/internal/**`.
- Public Catalog offer data must not duplicate Pricing or Inventory business ownership.
- Preserve Cart optimistic-version semantics and Checkout idempotency/Saga semantics.
- Frontend unit tests are optional by repository policy; storefront/admin production builds and real-flow verification are mandatory.
- Backend changed behavior must be covered by tests before/alongside implementation.
- Java package/port/adapter naming follows `AGENTS.MD` and `.agent/CONVENTIONS.MD`.
- No new database migration is required for this feature: existing `sku` fields are sufficient.
- Keep Admin behavior/build green after shared frontend changes.
- Final AI-assisted branch must be squashed to one logical commit on top of `develop`, then CI rerun against the exact rewritten HEAD.

---

## File Structure

### Backend Catalog

- Create `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/dto/ProductOffer.java` — application read projection for a customer-safe sellable SKU.
- Create `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/port/out/ProductOfferPort.java` — output boundary for product-offer projection queries.
- Create `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/response/ProductOfferResponse.java` — public transport response.
- Create `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/JdbcProductOfferAdapter.java` — read-only JDBC projection over `product + sku`.
- Modify `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/port/in/ProductUseCase.java` — expose product-offer queries through the existing cohesive Catalog input port.
- Modify `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/service/ProductServiceImplement.java` — enforce published-product visibility before returning offers.
- Modify `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/ProductPublicApi.java` — add public offer endpoints.
- Modify `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/ProductPublicController.java` — delegate offer endpoints.
- Modify `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/mapper/ProductApiMapper.java` — map application offer projection to HTTP response.
- Create `backend/services/be-catalog-api/src/test/java/com/dnnthanh/marketplace/be/catalog/api/application/service/ProductServiceImplementTest.java` — application tests for visibility and offer delegation.

### Frontend contracts and storefront state

- Modify `frontend/shared/marketplace-types.ts` — add `ProductOfferView` and typed customer-facing cart-line projection.
- Modify `frontend/shared/marketplace-api.service.ts` — add `productOffers(productId)`, `offer(skuId)`, and grouped Catalog product search parameters.
- Modify `frontend/shared/ui-state.ts` — retain trace/error details while classifying 401/403/404/general failures.
- Create `frontend/projects/storefront/src/app/shared/storefront-cart.store.ts` — shared cart state/enrichment used by header, Product Detail, Cart and Checkout.
- Create `verification/verify_storefront_core_flow_v14.py` — static feature contract gate.

### Storefront shell and reusable UI

- Create `frontend/projects/storefront/src/app/app.routes.ts` — route table including `/category/:id`.
- Modify `frontend/projects/storefront/src/app/app.component.ts` — shell/navigation only.
- Modify `frontend/projects/storefront/src/main.ts` — import routes from `app.routes.ts`.
- Create `frontend/projects/storefront/src/app/shared/product-card.component.ts` — reusable product result card.
- Create `frontend/projects/storefront/src/app/shared/price-display.component.ts` — reusable price presentation.
- Create `frontend/projects/storefront/src/app/shared/state-panel.component.ts` — loading/empty/error/forbidden/not-found presentation.
- Modify `frontend/projects/storefront/src/styles.css` — responsive shell, grids, product detail, cart, checkout, order/account states.

### Storefront feature pages

- Modify `frontend/projects/storefront/src/app/features/home.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/search.component.ts`.
- Create `frontend/projects/storefront/src/app/features/category.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/product-detail.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/cart.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/checkout.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/account.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/orders.component.ts`.
- Modify `frontend/projects/storefront/src/app/features/order-detail.component.ts`.

### Documentation and evidence

- Modify `frontend/COVERAGE-MATRIX.md` — remove the deliberate public SKU/offer gap after verification.
- Modify `frontend/README.md` — document the routed customer flow and offer contract.
- Create/update `.agent/reports/014-storefront-core-flow/README.md` — verification index with commands, CI SHA and environment-blocked runtime/screenshot items if any.

---

### Task 1: Add the public Catalog sellable-offer projection with TDD

**Files:**
- Create: `backend/services/be-catalog-api/src/test/java/com/dnnthanh/marketplace/be/catalog/api/application/service/ProductServiceImplementTest.java`
- Create: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/dto/ProductOffer.java`
- Create: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/port/out/ProductOfferPort.java`
- Create: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/response/ProductOfferResponse.java`
- Create: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/JdbcProductOfferAdapter.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/port/in/ProductUseCase.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/service/ProductServiceImplement.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/ProductPublicApi.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/ProductPublicController.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/mapper/ProductApiMapper.java`

**Interfaces:**
- Produces application record:

```java
public record ProductOffer(
    Long skuId,
    Long productId,
    Long sellerId,
    String productName,
    String sellerSku,
    String variantName,
    int purchaseLimit
) {}
```

- Produces output port:

```java
public interface ProductOfferPort {
    List<ProductOffer> findSellableByProductId(Long productId);
    Optional<ProductOffer> findSellableBySkuId(Long skuId);
}
```

- Extends `ProductUseCase` with:

```java
List<ProductOffer> offers(Long productId);
ProductOffer offer(Long skuId);
```

- Public endpoints:

```http
GET /products/{productId}/offers
GET /products/offers/{skuId}
```

- Both endpoints return only SKU rows where `sku.active = true` and owning `product.status = 'PUBLISHED'`.

- [ ] **Step 1: Write failing application tests**

Add tests that construct `ProductServiceImplement` with mocked `ProductRepositoryPort`, `MediaReadinessPort`, and `ProductOfferPort`.

```java
@Test
void offersReturnsOnlyProjectionAfterPublishedProductVisibilityCheck() {
    Product published = Product.rehydrate(
        10L, 20L, 30L, "Phone", "desc", ProductStatus.PUBLISHED,
        0L, LocalDateTime.now(), LocalDateTime.now());
    ProductOffer offer = new ProductOffer(100L, 10L, 20L, "Phone", "PHONE-BLACK", "Black", 5);

    when(repository.findById(10L)).thenReturn(Optional.of(published));
    when(offerPort.findSellableByProductId(10L)).thenReturn(List.of(offer));

    assertThat(service.offers(10L)).containsExactly(offer);
    verify(offerPort).findSellableByProductId(10L);
}

@Test
void offersDoesNotLeakDraftProduct() {
    Product draft = Product.rehydrate(
        10L, 20L, 30L, "Phone", "desc", ProductStatus.DRAFT,
        0L, LocalDateTime.now(), LocalDateTime.now());
    when(repository.findById(10L)).thenReturn(Optional.of(draft));

    assertThatThrownBy(() -> service.offers(10L))
        .isInstanceOf(ProductNotFoundException.class);
    verifyNoInteractions(offerPort);
}
```

Also test `offer(skuId)` throws `ProductNotFoundException` when the safe projection port returns empty.

- [ ] **Step 2: Run the focused test and verify RED**

```bash
./mvnw -f backend/pom.xml -pl services/be-catalog-api -am -Dtest=ProductServiceImplementTest test
```

Expected before implementation: compile/test failure because `ProductOffer`, `ProductOfferPort`, and `ProductUseCase.offers/offer` do not exist.

- [ ] **Step 3: Implement the minimal application/API contract**

`ProductServiceImplement.offers` must reuse published-product visibility semantics:

```java
@Transactional(readOnly = true)
public List<ProductOffer> offers(Long productId) {
    getPublished(productId);
    return offerPort.findSellableByProductId(productId);
}

@Transactional(readOnly = true)
public ProductOffer offer(Long skuId) {
    return offerPort.findSellableBySkuId(skuId)
        .orElseThrow(ProductNotFoundException::new);
}
```

`JdbcProductOfferAdapter` uses one projection query; do not create JPA aggregate entities for SKU merely for this read model:

```sql
SELECT s.id AS sku_id,
       p.id AS product_id,
       p.seller_id,
       p.name AS product_name,
       s.seller_sku,
       s.variant_name,
       s.purchase_limit
FROM sku s
JOIN product p ON p.id = s.product_id
WHERE p.id = :productId
  AND p.status = 'PUBLISHED'
  AND s.active = TRUE
ORDER BY s.id
```

The single-SKU query uses the same projected columns and visibility predicates with `s.id = :skuId`.

- [ ] **Step 4: Map and expose the public transport response**

`ProductOfferResponse` mirrors the seven application fields exactly. Add `ProductApiMapper.toResponse(ProductOffer)` and map lists in `ProductPublicController`.

```java
@GetMapping("/{productId}/offers")
List<ProductOfferResponse> offers(@PathVariable Long productId);

@GetMapping("/offers/{skuId}")
ProductOfferResponse offer(@PathVariable Long skuId);
```

- [ ] **Step 5: Run focused + Catalog module tests and verify GREEN**

```bash
./mvnw -f backend/pom.xml -pl services/be-catalog-api -am test
```

Expected: Catalog tests pass; no Liquibase change is introduced.

---

### Task 2: Add typed frontend offer contracts and a static storefront feature gate

**Files:**
- Modify: `frontend/shared/marketplace-types.ts`
- Modify: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/shared/ui-state.ts`
- Create: `verification/verify_storefront_core_flow_v14.py`

**Interfaces:**

```ts
export interface ProductOfferView {
  skuId: number;
  productId: number;
  sellerId: number;
  productName: string;
  sellerSku: string;
  variantName: string;
  purchaseLimit: number;
}

export interface UiErrorView {
  kind: 'unauthorized' | 'forbidden' | 'not-found' | 'error';
  message: string;
  traceId?: string;
}
```

Facade additions:

```ts
productOffers(productId: number): Promise<ProductOfferView[]>
offer(skuId: number): Promise<ProductOfferView>
products(page: number, size: number, criteria?: { keyword?: string; sellerId?: number; categoryId?: number }): Promise<PageResult<ProductView>>
```

- [ ] **Step 1: Add the failing static verification script**

The script must inspect repository text and fail unless all of these are true:

```python
required_snippets = {
    "frontend/shared/marketplace-api.service.ts": [
        "productOffers(productId: number)",
        "offer(skuId: number)",
        "/products/${productId}/offers",
        "/products/offers/${skuId}",
    ],
    "frontend/projects/storefront/src/app/app.routes.ts": ["path: 'category/:id'"],
    "frontend/projects/storefront/src/app/features/product-detail.component.ts": [
        "ProductOfferView",
        "selectedOffer",
    ],
}
```

Also fail if storefront TypeScript contains `'/internal/` or the product-detail template contains `SKU dùng cho pricing/cart`.

- [ ] **Step 2: Run static verification and verify RED**

```bash
python verification/verify_storefront_core_flow_v14.py
```

Expected: FAIL because offer facade/routes/UI do not exist yet.

- [ ] **Step 3: Add typed contracts/facade methods**

Build Catalog query strings via `URLSearchParams`; use backend field name `keyword`, not the old ad-hoc `query` parameter.

```ts
products(page = 0, size = 24, criteria: { keyword?: string; sellerId?: number; categoryId?: number } = {}) {
  const p = new URLSearchParams({ page: String(page), size: String(size) });
  if (criteria.keyword) p.set('keyword', criteria.keyword);
  if (criteria.sellerId) p.set('sellerId', String(criteria.sellerId));
  if (criteria.categoryId) p.set('categoryId', String(criteria.categoryId));
  return this.api.getPage<ProductView>(`/products?${p}`);
}
```

- [ ] **Step 4: Add structured UI error classification**

Keep `errorMessage` for existing pages, and add:

```ts
export function uiError(error: unknown): UiErrorView {
  if (error instanceof ApiClientError) {
    const kind = error.status === 401 ? 'unauthorized'
      : error.status === 403 ? 'forbidden'
      : error.status === 404 ? 'not-found'
      : 'error';
    return { kind, message: error.message, traceId: error.traceId };
  }
  return { kind: 'error', message: errorMessage(error) };
}
```

---

### Task 3: Extract the storefront shell/routes and add reusable UI primitives

**Files:**
- Create: `frontend/projects/storefront/src/app/app.routes.ts`
- Modify: `frontend/projects/storefront/src/app/app.component.ts`
- Modify: `frontend/projects/storefront/src/main.ts`
- Create: `frontend/projects/storefront/src/app/shared/product-card.component.ts`
- Create: `frontend/projects/storefront/src/app/shared/price-display.component.ts`
- Create: `frontend/projects/storefront/src/app/shared/state-panel.component.ts`
- Modify: `frontend/projects/storefront/src/styles.css`

**Interfaces:**

`ProductCardComponent` inputs:

```ts
productId = input.required<number>();
name = input.required<string>();
sellerId = input<number | undefined>();
price = input<number | undefined>();
rating = input<number | undefined>();
```

`PriceDisplayComponent` inputs:

```ts
amount = input.required<number>();
currency = input('VND');
```

`StatePanelComponent` inputs:

```ts
kind = input.required<'loading' | 'empty' | 'unauthorized' | 'forbidden' | 'not-found' | 'error'>();
message = input.required<string>();
traceId = input<string | undefined>();
```

- [ ] **Step 1: Move route declarations out of `AppComponent`**

`app.routes.ts` owns `Routes` and imports feature components/guards. Preserve all current routes and add:

```ts
{ path: 'category/:id', component: CategoryComponent }
```

- [ ] **Step 2: Reduce `AppComponent` to a responsive shell**

Header must provide home brand, search entry, cart with current item count, account/login state, desktop navigation and compact mobile navigation. Do not print authorization role arrays in the header.

- [ ] **Step 3: Update `main.ts`**

```ts
import { routes } from './app/app.routes';
```

Keep one `AuthService.ensureInitialized()` call after bootstrap.

- [ ] **Step 4: Add shared presentation primitives and responsive CSS**

Do not put Pricing/Cart business rules inside components. Keep all shared components standalone.

- [ ] **Step 5: Build storefront**

```bash
cd frontend
npm install --no-audit --no-fund
npm run build:storefront
```

Expected: production build passes after Tasks 2-3 compile together.

---

### Task 4: Build Home, Search and Category discovery around real APIs

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/home.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/search.component.ts`
- Create: `frontend/projects/storefront/src/app/features/category.component.ts`

**Interfaces:**
- Home consumes `MarketplaceApiService.products(0, 12)`.
- Category consumes `MarketplaceApiService.products(page, 24, { categoryId })`.
- Search continues to consume OpenSearch cursor API; route query state stores `q` and `size` only.

- [ ] **Step 1: Replace Home's local `Product` interface and direct `ApiService` call**

Use `ProductView`, `MarketplaceApiService`, `ProductCardComponent`, `StatePanelComponent`.

Derive category shortcuts from the loaded product data without inventing fake categories:

```ts
readonly categoryIds = computed(() => [...new Set(this.products().map(p => p.categoryId))]);
```

Render labels as `Danh mục #{{id}}` until a future dedicated category-name contract exists.

- [ ] **Step 2: Make Search route-stateful**

On init read `q` and `size` from `ActivatedRoute.snapshot.queryParamMap`. On a new search, update query params through `Router.navigate` and reset cursor history before calling the existing OpenSearch API.

Render results with `ProductCardComponent`; preserve cursor history/`nextCursor` semantics.

- [ ] **Step 3: Implement Category page**

Read `category/:id`, call Catalog pageable products with `{ categoryId }`, render pageable result cards, and show loading/empty/error states.

- [ ] **Step 4: Run the feature static verifier**

```bash
python verification/verify_storefront_core_flow_v14.py
```

Expected at this checkpoint: route/shell checks pass; Product Detail checks may remain RED until Task 5 if running task-by-task locally.

---

### Task 5: Replace Product Detail diagnostic SKU input with real offer selection

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/product-detail.component.ts`
- Consume: `ProductOfferView`, `PriceView`, `MarketplaceApiService`, `StorefrontCartStore`, shared UI primitives.

**Interfaces:**

```ts
readonly offers = signal<ProductOfferView[]>([]);
readonly selectedOffer = signal<ProductOfferView | undefined>(undefined);
readonly price = signal<PriceView | undefined>(undefined);
readonly adding = signal(false);
quantity = 1;
```

- [ ] **Step 1: Load product + offers + media/community data in the initial request group**

```ts
const [product, offers, media, comments, reviews, summary] = await Promise.all([
  this.marketplace.product(this.productId),
  this.marketplace.productOffers(this.productId),
  this.marketplace.productMedia(this.productId),
  this.marketplace.comments(this.productId),
  this.marketplace.reviews(this.productId),
  this.marketplace.reviewSummary(this.productId),
]);
```

Select the first offer when present, then resolve its price.

- [ ] **Step 2: Replace manual SKU/Resolve-price controls**

Render variant buttons/options from `variantName` and `sellerSku`. Selecting an offer triggers `marketplace.price(offer.skuId, offer.sellerId)`.

Quantity must be at least 1 and at most `selectedOffer.purchaseLimit`.

- [ ] **Step 3: Add to cart using the selected offer**

```ts
const offer = this.selectedOffer();
const price = this.price();
const cart = await this.cartStore.ensureLoaded();
const updated = await this.marketplace.putCartItem({
  sellerId: offer.sellerId,
  skuId: offer.skuId,
  quantity: this.quantity,
  priceSnapshot: price.amount,
  selected: true,
  expectedVersion: cart.version,
});
this.cartStore.accept(updated);
```

Disable duplicate submission while `adding()` is true.

- [ ] **Step 4: Verify no diagnostic SKU input remains**

```bash
python verification/verify_storefront_core_flow_v14.py
```

Expected: static Product Detail checks pass.

---

### Task 6: Add a storefront cart store and customer-readable Cart/Checkout

**Files:**
- Create: `frontend/projects/storefront/src/app/shared/storefront-cart.store.ts`
- Modify: `frontend/projects/storefront/src/app/features/cart.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/checkout.component.ts`
- Modify: `frontend/projects/storefront/src/app/app.component.ts`

**Interfaces:**

```ts
export interface StorefrontCartLine {
  item: CartItem;
  offer?: ProductOfferView;
}

@Injectable({ providedIn: 'root' })
export class StorefrontCartStore {
  readonly cart = signal<CartView | undefined>(undefined);
  readonly offers = signal<Record<number, ProductOfferView>>({});
  readonly count = computed(() => this.cart()?.items
    .filter(i => !i.savedForLater)
    .reduce((sum, i) => sum + i.quantity, 0) ?? 0);

  async refresh(): Promise<CartView>;
  async ensureLoaded(): Promise<CartView>;
  accept(cart: CartView): void;
  line(item: CartItem): StorefrontCartLine;
}
```

`refresh()` fetches `/private/cart`, then enriches unique `skuId`s through public `MarketplaceApiService.offer(skuId)` using `Promise.all`. Offer lookup failure must not make the entire cart unreadable; preserve the line with an undefined offer and surface a generic fallback label.

- [ ] **Step 1: Implement the shared cart store**

Keep Cart version as returned by backend. `accept(updatedCart)` must replace the current cart immediately and refresh offer metadata only for new SKU ids.

- [ ] **Step 2: Refactor Cart page**

Render `offer.productName`, `offer.variantName`, seller, quantity, price snapshot and line subtotal. Keep checkbox, quantity mutation, save-for-later, remove and validation actions.

On optimistic conflict, keep the existing behavior of showing the backend error then refreshing current Cart version; do not blind-retry the mutation.

Render validation violations as readable rows instead of raw JSON.

- [ ] **Step 3: Refactor Checkout page**

Use the same enriched lines. Keep warehouse id as a local-demo operational input only if the backend still requires it; visually demote it under an “Thông tin giao hàng demo” section rather than presenting it as a technical top-level diagnostic.

Do not expose/regenerate the idempotency key as a primary UI control. Generate it once when the component is created and preserve it for repeated submission attempts of the same checkout intent until success.

Keep provider selection and promotion codes because the existing contract supports them.

- [ ] **Step 4: Wire cart count into the shell**

Authenticated header loads cart once and displays `cartStore.count()`; anonymous header does not call `/private/cart`.

- [ ] **Step 5: Build storefront and admin**

```bash
cd frontend
npm run build:storefront
npm run build:admin
```

Expected: both production builds pass.

---

### Task 7: Polish Account, Order History and Order Detail/Tracking

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/account.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/orders.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/order-detail.component.ts`

- [ ] **Step 1: Replace raw authorization JSON on Account**

Render compact sections for roles, permissions and seller scopes only when present, plus links to Orders and Notifications. Keep notification preferences form unchanged semantically.

- [ ] **Step 2: Make Order History customer-readable**

Keep pagination and monetary data, but render status chips, dates, payable amount and seller-order count with customer labels rather than diagnostic terminology.

Add explicit loading and empty states.

- [ ] **Step 3: Improve Order Detail/Tracking**

Render order monetary summary, seller-order cards and shipment timeline. Keep cancellation action bound to existing backend state; do not add unsupported transitions.

If no shipment exists, render a friendly pending-fulfillment state rather than a diagnostic hint.

- [ ] **Step 4: Run frontend builds**

```bash
cd frontend
npm run build:storefront
npm run build:admin
```

Expected: both pass.

---

### Task 8: Documentation, final verification, CI and evidence

**Files:**
- Modify: `frontend/COVERAGE-MATRIX.md`
- Modify: `frontend/README.md`
- Create/update: `.agent/reports/014-storefront-core-flow/README.md`

- [ ] **Step 1: Update frontend coverage documentation**

Remove the deliberate gap stating Product Detail requires a diagnostic SKU id. Record both public offer lookup forms and note that browser code still never calls `/internal/**`.

- [ ] **Step 2: Run repository static verification**

```bash
python verification/verify_storefront_core_flow_v14.py
python verification/verify_keycloak_authority_v17.py
for script in verification/verify_*.py; do python "$script"; done
```

Expected: all static gates pass.

- [ ] **Step 3: Run backend quality gate**

```bash
./mvnw -f backend/pom.xml -B -ntp clean verify
```

Expected: PASS on Java 25.

- [ ] **Step 4: Run frontend quality gate**

```bash
cd frontend
npm install --no-audit --no-fund
npm run build:storefront
npm run build:admin
```

Expected: both builds PASS.

- [ ] **Step 5: Validate Compose topology**

```bash
docker compose config --quiet
docker compose --profile full --profile heavy --profile observability config --quiet
```

- [ ] **Step 6: Exercise the real customer smoke flow when a Docker/browser-capable execution host is available**

Canonical startup:

```bash
./compose-up.sh up -d --build
```

Exercise and capture evidence for:

```text
anonymous Home -> Search/Category -> Product Detail
login -> Product Detail variant selection -> Add to Cart
Cart quantity/validation -> Checkout -> Order result
Orders -> Order Detail -> Shipment tracking
one empty/not-found/error state
one mobile-width storefront state
```

Store screenshots and an evidence index under `.agent/reports/014-storefront-core-flow/`. If the execution host cannot run Docker/browser automation, record the exact environment blocker and do not claim these runtime scenarios passed.

- [ ] **Step 7: Push branch and open PR to `develop`**

PR body must summarize frontend flow, Catalog public offer contract, backend boundary, verification commands and any runtime-evidence blocker.

- [ ] **Step 8: Read GitHub Actions for the pushed HEAD**

Required jobs from `.github/workflows/ci.yml`:

```text
static-verification
backend
frontend
compose-config
docker-build
core-infrastructure-smoke
```

If any job fails, inspect the job log, fix the root cause, rerun, and include the follow-up in the final logical commit.

- [ ] **Step 9: Squash to one logical feature commit and rerun CI**

Final history on top of `develop` must contain exactly one logical implementation commit. After rewriting history, verify the PR/head SHA changed as expected and all required CI jobs are green on that exact SHA before marking ready for review.
