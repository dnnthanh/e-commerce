# Internal naming + CI/CD configuration verification

Date: 2026-08-04

## Accepted architecture rules

- Input port: `XUseCase` / `XQuery` interface in `application/port/in`.
- Input-port implementation: `XServiceImplement` in `application/service`.
- Output port: `XPort` interface in `application/port/out`.
- Output implementation: `XAdapter`; persistence uses `XPersistenceAdapter`.
- Internal outbound adapters live under `adapter/out/internal/<target-service>/<protocol>`.
- Internal inbound adapters live under `adapter/in/internal/<protocol>`.
- No runtime `transport` configuration switch.
- Java contains no internal environment URL; `application.yml` binds environment variables.
- `ci-cdconfigs` is inside the repository and owns one direct folder per `be-*` deployable.
- No global endpoint/secrets values file; each BE owns its config independently.
- Secret values have no committed defaults and are injected by CI/Secret Manager/Kubernetes Secret/External Secrets.

## Implemented changes

- Renamed/moved input-port implementations to `*ServiceImplement`.
- Consolidated output ports under `application/port/out` and adapter implementations under `adapter/out/...`.
- Split Cart internal Catalog/Pricing/Inventory dependencies into separate output ports + REST adapters.
- Split Checkout cross-service communication into target-specific REST adapters.
- Replaced utility-class bean holders for payment/notification providers with concrete adapters.
- Removed hard-coded internal service URLs from Java, including platform authorization/token clients.
- Removed committed Keycloak/MinIO client-secret defaults.
- Payment simulator redirect URL is environment-backed.
- Added 53 direct per-deployable folders under `ci-cdconfigs` with local/dev/staging/prod examples, secret examples, Kubernetes base and overlays.
- Added CI configuration coverage verification: every `${ENV_VAR}` in a service `application.yml` must be mapped by that same BE's CI/CD folder.
- Updated `AGENTS.MD`, `.agent/CONVENTIONS.MD`, and Feature 013 package/naming/config sections.

## Fresh verification

- `verification/run_feature013_final_audit.sh`: PASS, `OVERALL=0`.
- `verification/verify-naming-internal-config.sh`: PASS, `FAILURES=0`.
- `verification/verify_ci_config_coverage.py`: PASS, 53 services.
- `verification/verify_maven_model_v5.py`: PASS, 56 POMs / 55 modules.
- YAML parse: PASS, 623 YAML documents/files scanned.
- Service-depth gate: 41 passed, 0 failed.

## Maven/JDK runtime gate

The current execution environment still has Java 21 and cannot resolve Maven Central. A fresh wrapper attempt fails before Maven starts:

- Java: OpenJDK 21.0.10
- `./mvnw -f backend/pom.xml -DskipTests validate`: exit 6
- blocker: `Could not resolve host: repo.maven.apache.org`

Therefore Maven compile/JUnit on Java 25 is not claimed as PASS in this environment. The Maven model error reported from the user's machine was addressed structurally in the parent POM and is covered by `verify_maven_model_v5.py`; the next real Maven run on the user's Java-25 environment remains the authoritative compile check.
