# API Search Contract + Native SQL/SpEL Refactor Verification

## Why this checkpoint exists

The user identified two architectural defects:

1. transport records were nested inside `*Api` interfaces;
2. search/filter endpoints passed many loose variables instead of one search request/criteria object.

A real Maven build also exposed the next dependency-model defect: the project requested the wrong Testcontainers 2.x JUnit artifact coordinate.

## Enforced contract

```text
api/request/search/*SearchRequest
        -> MapStruct
application/query/*SearchCriteria
        -> input port
        -> output port
adapter/out/persistence/repository/*JpaRepository
        -> native SQL + SpEL :#{#criteria.field}
```

Exceptions:

- Search/Discovery is OpenSearch-owned; only its HTTP search request is grouped.
- Mongo-owned Comment/Notification use grouped search requests/criteria but stay MongoDB-backed.
- command/path/atomic-lock parameters are not artificially converted into search objects.

## Updated search/list paths

- Audit
- Catalog
- Search/Discovery
- Inventory
- Order
- Payment
- Pricing
- Review
- Settlement
- Seller
- Comment
- Notification

Ordinary Inventory and Payment read/search code was split out of broad `JdbcClient` adapters into Spring Data JPA native-query adapters. JDBC remains for inventory mutation/locking and payment workflow/refund/ledger-style atomic operations.

## Testcontainers Maven model fix

Parent uses Testcontainers `2.0.5` and manages:

- `org.testcontainers:testcontainers`
- `org.testcontainers:testcontainers-junit-jupiter`

Testcontainers is dependency-managed rather than inherited as a test dependency by every reactor module.

## Fresh verification

`bash verification/run_feature013_final_audit.sh`

Result: `OVERALL=0`.

`python3 verification/verify_api_search_contract_v7.py`

Result: `43 passed, 0 failed`.

`python3 verification/verify_maven_model_v5.py`

Result: `MAVEN_MODEL_V5=PASS poms=56 modules=55`.

The verification also checks that every project-local Java import resolves after moving API DTOs to top-level request/response packages.

## Full Maven status in this execution runtime

Attempted:

`./mvnw -f backend/pom.xml -U -DskipTests validate`

This sandbox cannot resolve `repo.maven.apache.org`, therefore Maven Wrapper bootstrap exits `6` before Maven starts. Full Java-25 compilation still needs the user's local Java-25 Maven environment.
