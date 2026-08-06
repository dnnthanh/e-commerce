# Platform API Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Harden the shared `be-platform-starter` and affected API/search boundaries for production: shared success/error envelopes, Java-configured NON_NULL JSON, trace-id propagation, correct pageable/cursor metadata, MapStruct compilation, broader exception mapping, and deployment-safe configuration ownership.

**Architecture:** `be-platform-starter` remains the shared technical core; no new `be-core` module is introduced. Servlet controllers keep their domain-specific signatures where practical and a shared `ResponseBodyAdvice` wraps normal responses, while pagination endpoints can return `ApiResponse` explicitly when they need metadata. Micrometer/OpenTelemetry remains the canonical trace source; the current trace id is additionally exposed/propagated in a `trace-id` HTTP header and is removed from `UserContext` because identity context and observability context are separate concerns.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring MVC, Spring Security, Spring Data, Micrometer Tracing/OpenTelemetry, Jackson 3, MapStruct 1.6.3, Maven, Kustomize/Kubernetes.

## Global Constraints

- Do not create `be-core`; modify `backend/platform/be-platform-starter` as the shared core.
- Configure Jackson null omission in Java config, not `application.yml`.
- `UserContext` must not contain `traceId`.
- HTTP responses and outbound `RestClient` calls must propagate current trace id through header `trace-id`; standard W3C tracing remains enabled.
- Relational Catalog pagination uses Spring `Pageable`/`Page`; Search/Discovery keeps cursor/search-after pagination.
- All successful JSON API responses use the shared `ApiResponse` shape; `null` fields are omitted.
- Error handling returns the same top-level envelope and never exposes stack traces.
- Real environment values/secrets must not be embedded in reusable Kubernetes base manifests.

---

### Task 1: Shared API envelope and Jackson NON_NULL

**Files:**
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/ApiResponse.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/ApiMetadata.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/PageMetadata.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/CursorMetadata.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/ApiResponseBodyAdvice.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/PlatformJacksonConfiguration.java`
- Modify: `backend/platform/be-platform-starter/pom.xml`
- Test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/marketplace/be/platform/PlatformApiResponseTest.java`

**Interfaces:**
- Produces: `ApiResponse<T>`, `PageMetadata`, `CursorMetadata`, automatic normal-response wrapping.

- [ ] Write tests proving null envelope fields are omitted, existing `ApiResponse` is not double-wrapped, and Spring `Page` becomes `data + metadata`.
- [ ] Run targeted platform tests under the available JDK with the repository Java version temporarily overridden only for local verification; confirm RED.
- [ ] Add the envelope types, Java `JsonMapperBuilderCustomizer` NON_NULL setting, and response advice.
- [ ] Run targeted platform tests and confirm GREEN.

### Task 2: Trace header propagation and UserContext separation

**Files:**
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/TraceHeaders.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/TraceIdRestClientCustomizer.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/StructuredHttpLoggingFilter.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/UserContext.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/UserContextConfiguration.java`
- Test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/marketplace/be/platform/TracePropagationTest.java`
- Test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/marketplace/be/platform/UserContextTest.java`

**Interfaces:**
- Produces: HTTP header constant `trace-id`; response header propagation; outbound RestClient interceptor using `Tracer.currentSpan()`.

- [ ] Write tests proving `UserContext` has no trace component and that servlet response/outbound REST requests receive current `trace-id`.
- [ ] Run tests and confirm RED.
- [ ] Remove trace from `UserContext`, update its factories/callers, add trace header response + RestClient customization.
- [ ] Run tests and confirm GREEN.

### Task 3: Generic errors and production-grade exception translation

**Files:**
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/GlobalExceptionHandler.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/SecurityConfiguration.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/ApiAuthenticationEntryPoint.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/ApiAccessDeniedHandler.java`
- Modify: `backend/platform/be-platform-starter/src/main/resources/messages.properties`
- Modify: `backend/platform/be-platform-starter/src/main/resources/messages_vi.properties`
- Test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/marketplace/be/platform/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Produces: consistent `ApiResponse.error(ApiError)` for business, validation/binding, malformed input, auth, 404/405/415, data conflict/locking, timeout/unavailable, and unknown failures.

- [ ] Write failing handler tests for representative 400/404/409/500 paths.
- [ ] Implement explicit mappings with safe stable error codes and localized message keys.
- [ ] Configure servlet security entry point/denied handler to use the same envelope.
- [ ] Run platform tests and confirm GREEN.

### Task 4: Checkout MapStruct iterable-to-request bug

**Files:**
- Modify: `backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal/common/rest/mapper/CheckoutInternalRestMapper.java`
- Test: `backend/services/be-checkout-api/src/test/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal/common/rest/mapper/CheckoutInternalRestMapperTest.java`

**Interfaces:**
- Produces: `InventoryReleaseRequest toInventoryReleaseRequest(List<String>)` and `PromotionReservationMutationRequest toPromotionReservationMutationRequest(List<String>)` implemented as explicit default wrappers rather than generated iterable-to-bean mappings.

- [ ] Reproduce current MapStruct compilation failure.
- [ ] Add tests for the two wrapper mappings.
- [ ] Replace only the unsupported generated mappings with default methods.
- [ ] Run checkout targeted tests/compile and confirm GREEN.

### Task 5: Catalog Pageable pagination and Search cursor metadata

**Files:**
- Modify Catalog API/use-case/port/persistence repository types to use `Pageable` and `Page`.
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/ProductPublicApi.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/ProductPublicController.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/port/in/ProductUseCase.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/port/out/ProductRepositoryPort.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/application/service/ProductServiceImplement.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/ProductPersistenceAdapter.java`
- Modify: `backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/repository/ProductJpaRepository.java`
- Modify Search API response/controller to return explicit cursor metadata through `ApiResponse`.
- Test Catalog and Search pagination adapters/controllers.

**Interfaces:**
- Catalog: Spring `Pageable` input and `Page<Product>` output, deterministic `updated_at DESC, id DESC` native paging query + count query.
- Search: existing search-after cursor remains; HTTP response is `ApiResponse<SearchResponse>` with `CursorMetadata`.

- [ ] Add failing tests proving Catalog exposes page metadata and Search exposes cursor metadata.
- [ ] Convert Catalog native repository query to pageable native query with count query.
- [ ] Remove manual `page -> offset/limit` mapping from Catalog search criteria.
- [ ] Wrap Search result with cursor metadata without replacing search-after.
- [ ] Run Catalog/Search targeted tests and confirm GREEN.

### Task 6: CI/CD config hardening

**Files:**
- Modify all `ci-cdconfigs/be-*/kubernetes/base/configmap.yaml` ownership so reusable bases contain no environment-specific DB/host/user values.
- Modify per-service Kubernetes overlay/config documentation and verification scripts as needed.
- Add verification script: `verification/verify_cicd_no_embedded_runtime_values.py`.

**Interfaces:**
- Deployment still consumes `be-<service>-config` and `be-<service>-secret` via environment variables.
- Actual ConfigMaps are supplied/rendered per environment by CI/CD/Kustomize; real secrets remain external.

- [ ] Add a failing repository check that flags JDBC URLs, concrete internal host URLs, database usernames, passwords, tokens, and client secrets in reusable base ConfigMaps.
- [ ] Replace embedded base runtime values with deploy-time placeholders/examples that cannot be mistaken for production values and document CI injection.
- [ ] Keep secret example manifests placeholder-only and excluded from base resources.
- [ ] Run the new config verification and existing CI config coverage checks.

### Task 7: Project verification and evidence

**Files:**
- Update: `.agent/specs/000-platform-foundation.md`
- Create/update evidence under `.agent/reports/013-codebase-deep-refactor/`.

- [ ] Run Spotless on modified Java files.
- [ ] Run targeted Maven tests using JDK 21 override only when Java-25 runtime is unavailable; record this limitation explicitly.
- [ ] Run repository Python/static verification scripts relevant to API search, mapper hygiene, exceptions, CI config, and platform abstractions.
- [ ] Run full `mvn clean verify` only if JDK 25 is available; otherwise do not claim it passed.
- [ ] Repackage the corrected repository ZIP.
