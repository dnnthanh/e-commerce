# Engineering Conventions

## Java and Spring

- Java 25 LTS.
- Spring Boot 4.1.x baseline (4.1.0) with Java 25.
- Prefer Maven multi-module build for backend platform/services unless a feature spec justifies otherwise.
- Constructor injection only for required collaborators.
- MapStruct as Spring bean mapper.
- Lombok where it reduces boilerplate without hiding semantics.
- Bean Validation for transport/application input constraints; domain model still protects business invariants.
- Interface-driven Spring controllers.
- Application inbound boundaries depend on input-port interfaces (`application/port/in`); controllers, schedulers and consumers never inject concrete use-case implementations.
- Stateless static constant/helper holders use Lombok `@UtilityClass`; rich domain classes remain normal classes even when they expose static factories.
- Spring Data JPA for simple relational CRUD/aggregate persistence.
- Relational search/filter/pagination uses native SQL (`@Query(nativeQuery = true)` / `@NativeQuery` first); do not use JPA Specification/Criteria for business search.
- `JdbcClient`/`NamedParameterJdbcTemplate` is reserved for complex or performance-sensitive SQL, atomic conditional updates/locks, bulk pipelines, ledger/reporting SQL, and vendor-specific outbox/inbox operations.
- Use deterministic ordering for every paginated search; `Pageable` may carry pagination inputs but must not replace the native SQL search policy.
- WebFlux only at reactive/I/O fan-out boundaries where the dependency chain is non-blocking enough to benefit; do not wrap blocking JPA calls and claim reactive scalability.

## Package shape per service

```text
<context>/
  domain/
    model/
    service/                  # true domain services only
    exception/
  application/
    command/
    query/
    dto/
    port/
      in/                     # XUseCase / XQuery interfaces
      out/                    # XPort interfaces
    service/                  # XServiceImplement classes
  adapter/
    in/
      web/                    # public/private HTTP controllers
      internal/
        rest/
        grpc/
      webhook/
      messaging/
        kafka/
      scheduler/
    out/
      persistence/            # XPersistenceAdapter
      internal/
        <target-context>/
          rest/               # XRestAdapter
          grpc/               # XGrpcAdapter
      external/
        <provider>/
          rest/
          grpc/
      messaging/
        kafka/
  config/
```

Naming is contractual: `CartUseCase` is an interface, `CartServiceImplement` is its application implementation; `CartPersistencePort` is an interface, `CartPersistenceAdapter` implements it. A concrete class must not be named `*UseCase`.

Non-persistence infrastructure boundary adapters use `@Adapter`, inbound or outbound: internal/external REST or gRPC adapters, webhook/messaging consumers, schedulers/workers, provider/cache/storage/search adapters, and outbox publishers. Persistence implementations use `@Persistence`; application input-port implementations use `@UseCase`. Avoid generic `@Component` for classes whose architectural role is already known.

Transport DTOs are inbound concerns only. `application/**` and `adapter/out/**` must never import `api.request` or `api.response`. Use MapStruct at boundaries. Internal REST adapters use typed DTOs instead of raw maps.

Internal protocol is selected by architecture/code, not by an `application.yml transport` switch. Service endpoint values come from environment variables mapped in the owning `ci-cdconfigs/<be-name>/` folder.

## Controller interface convention

Public example:

```java
@RequestMapping("/products")
public interface ProductPublicApi {
    // Add JavaDoc only when the contract has non-obvious semantics, constraints, units, ownership, or errors.
}
```

Private example:

```java
@RequestMapping("/private/products")
public interface ProductPrivateApi {
    // Add JavaDoc only when the contract has non-obvious semantics, constraints, units, ownership, or errors.
}
```

Implementation contains behavior delegation only and should not duplicate mapping annotations.

## Authorization expression convention

Use text blocks for multi-line SpEL:

```java
@PreAuthorize("""
    @authorizationService.hasSellerPermission(
        'PRODUCT_UPDATE',
        #request.sellerId()
    )
    """)
```

Do not concatenate Java strings for long authorization expressions.

## Search criteria convention

Prefer records/parameter objects:

```java
/**
 * Product search criteria.
 *
 * @param keyword free-text keyword
 * @param shopId optional shop identifier
 * @param categoryId optional category identifier
 * @param status optional lifecycle status
 */
public record ProductSearchCriteria(
    String keyword,
    Long shopId,
    Long categoryId,
    ProductStatus status
) {}
```

Do not create `ShopId`/`CategoryId` wrappers merely for search filtering unless a real invariant requires them.

## Exception and i18n convention

- Stable machine-readable error code is the API contract.
- Human-readable message is resolved by MessageSource/i18n.
- Standard response includes code, localized message, traceId, timestamp, path, and optional field errors/details.
- Separate business/domain errors from validation, authorization, infrastructure, external dependency, and retryable/non-retryable concerns.
- Never expose raw stack traces to clients.

## HTTP normalization/logging

- Centralized String deserialization/binding trimming.
- Explicit `@NoTrim` or equivalent opt-out for passwords/signatures/raw values.
- Structured request/response logs with masking and payload-size/truncation controls.
- Never log Authorization headers, cookies, secrets, CVV, raw payment credentials, refresh tokens, or full access tokens.

## Kafka object convention

- Producer/consumer APIs accept typed event objects.
- Serializer/deserializer configuration is centralized.
- Event headers/envelope carry versioning and correlation information.
- Deserialization failures follow an explicit recoverability/DLT policy.

## Data ownership

- Each service owns its schema/database logically.
- Local Compose may consolidate multiple logical PostgreSQL databases into one PostgreSQL container to reduce operational duplication, while users/DBs remain isolated.
- No cross-service SQL joins.

## Internal communication decision

Use the semantic need, not preference, to select a protocol:

| Mechanism | Use when | Avoid when |
| --- | --- | --- |
| REST | synchronous resource/command API, broad interoperability | very high-volume strongly typed RPC is dominant |
| gRPC | low-latency internal RPC/streaming with protobuf contract | browser/public interoperability is primary |
| GraphQL | client-driven aggregation/read composition | internal command workflow or event propagation |
| Kafka event | asynchronous domain/integration event, replayable fan-out | caller requires immediate success/failure response |
| Work queue | one-of-N workers must execute a command/job | multiple consumers independently need the same event |
| Webhook | callback pushed into this system by a provider/service | the caller is under our control and RPC/event is clearer |
| SOAP | required legacy/enterprise contract | greenfield internal service communication |

A service may use several mechanisms for different semantics, but there is no runtime `transport` enum/property that swaps them.

## CI/CD config convention

```text
ci-cdconfigs/
  be-cart-api/
    env/
      local.env.example
      dev.env.example
      staging.env.example
      prod.env.example
      secrets.env.example
    kubernetes/
      base/
      overlays/dev/
      overlays/staging/
      overlays/prod/
  be-order-api/
  ...
```

One BE owns one folder. Root-level files may explain/template the convention but must not become a global service-value map. Secrets are injected; they are not stored in Git.

## Documentation quality

Comments must explain business meaning, unit, ownership, constraints, or non-obvious behavior. Avoid comments that merely repeat the identifier name when a more meaningful description can be given.
Remove stale `@param` blocks left after DTO extraction, boilerplate comments such as “HTTP adapter”, “creates client”, and comments that merely restate a field/class name.

Dependency hygiene: Boot 4 `RestClient.Builder` consumers declare `spring-boot-starter-restclient`; `spring-boot-starter-test` is test-scoped; non-HTTP workers/jobs/outbox modules do not declare MVC/WebFlux starters. Lombok is inherited from the backend parent as `provided`; do not duplicate it in child POMs.

## API and search request convention

```text
api/request/search/ProductSearchRequest
        -> MapStruct
application/query/ProductSearchCriteria
        -> input port / output port
adapter/out/persistence/repository/*JpaRepository
        -> @Query(nativeQuery = true) + :#{#criteria.field}
```

- Never declare request/response records inside a `*Api` interface. API contracts contain methods only.
- Search/filter/list endpoints use one `*SearchRequest`/`*QueryRequest`; do not pass each filter as a separate `@RequestParam`.
- Relational searches use native SQL with a single grouped criteria parameter and SpEL binding.
- Do not apply the search-object rule mechanically to command/path parameters such as `expectedVersion`, resource ids, or atomic mutation parameters.
- OpenSearch remains the engine for the Search/Discovery service; it still receives one grouped HTTP search request.
