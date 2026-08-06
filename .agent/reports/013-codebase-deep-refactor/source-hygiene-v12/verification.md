# Feature 013 — Source hygiene v12 verification

## Scope

This pass addresses source consistency issues discovered after the Java 25 Maven build progressed into real module compilation/tests:

- Spring Boot 4 `RestClient.Builder` dependency ownership per consuming module;
- transport/application model boundary and stale imports after DTO extraction;
- architectural stereotypes (`@UseCase`, `@Persistence`, `@Adapter`) by role;
- MapStruct use for named non-trivial mappers;
- persistence exception package placement and duplicate exception removal;
- Maven dependency hygiene (`starter-test` test scope, inherited Lombok, no redundant worker server stack);
- redundant generated JavaDoc/comment cleanup;
- explicit-import hygiene;
- Catalog `ProductTest` assertion updated to the concrete domain exception actually raised by the invariant.

## Architecture conventions enforced

- `XUseCase` / `XQuery`: application input-port interface.
- `XServiceImplement`: application implementation annotated `@UseCase`.
- `XPort`: output-port interface.
- `XAdapter` / `XPersistenceAdapter`: infrastructure implementations using `@Adapter` / `@Persistence`.
- `@Adapter` covers non-persistence infrastructure boundaries, inbound and outbound.
- Transport DTOs do not leak into `application/**` or `adapter/out/**`.
- `*Mapper` classes/interfaces use MapStruct.
- Comments document semantics/invariants/constraints, not obvious fields/plumbing.
- Relational simple CRUD uses Spring Data JPA; relational search uses native SQL + SpEL criteria; JDBC is reserved for atomic/locking/bulk/ledger/reporting/vendor-specific cases.

## Fresh executable verification

`verification/run_feature013_final_audit.sh` was executed after the final edits in this pass.

Result:

```text
OVERALL=0
FINAL_AUDIT_EXIT=0
```

The final audit now includes source-hygiene gates v8-v12. Key results:

```text
SOURCE_HYGIENE_V8=PASS
java_files=867 restclient_modules=17
DEPENDENCY_HYGIENE_V9=PASS
JAVA_SOURCE_QUALITY_V10=PASS files=835
ANNOTATION_MAPPER_IMPORT_HYGIENE_V11=PASS
MODEL_IMPORT_ANNOTATION_HYGIENE_V12=PASS
java_files=867 internal_types=867
MAVEN_MODEL_V5=PASS poms=56 modules=55
JAVA_TEXT_BLOCK_SYNTAX=PASS
API_SEARCH_CONTRACT_V7: 43 passed, 0 failed
Service depth: 41 passed, 0 failed
PRODUCTION_DEPTH_DOMAIN_SMOKE=PASS
CROSS_CONTEXT_WORKFLOW_SMOKE=PASS
```

See `final-audit.log` for the complete command-by-command output.

## RED evidence

The new gates were observed failing before the fixes. Evidence is retained as:

- `red-v11.log`
- `red-v12-initial.log`
- `red-v12-platform.log`
- `red-v12-comments.log`

## Runtime/toolchain limitation of this environment

This execution environment still contains JDK 21 rather than JDK 25, cannot resolve Maven Central through the Maven Wrapper, and has no Docker executable. Therefore this report does **not** claim a full Java 25 Maven reactor build or Docker startup in this environment.

The user's real Java 25 Maven run has already demonstrated that the prior reactor/POM/Testcontainers/text-block/Spotless blockers were passed and later reached the Catalog unit-test failure. This source snapshot contains the Catalog test correction as well as the source-hygiene fixes described above. A fresh user-side `mvn clean install` is still the authoritative compiler/Spring/MapStruct/JUnit verification for the entire 56-project reactor.
