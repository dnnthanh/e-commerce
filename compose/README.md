# Docker Compose topology

`docker-compose.yml` at repository root is the canonical local entrypoint. The default topology is intentionally smaller than the complete production-like topology.

```bash
docker compose up -d --build
```

A wrapper is also provided to neutralize an accidentally exported `COMPOSE_FILE` and force the repository root as project directory:

```bash
./compose-up.sh up -d --build
```

## Why build contexts are scoped

The local backend build uses `backend/Dockerfile.runtime` with `context: ./backend`; storefront/admin use `context: ./frontend`. The root compose never sends the whole repository (`.`) as an application build context and never uses `..` as a build context.

- `backend/Dockerfile.runtime`: local developer image. Maven packages the reactor once and the image stores every service JAR under `/opt/marketplace/services/<module>/app.jar`.
- `backend-runtime-build`: the only root Compose service with a backend Maven `build:` block.
- Every `be-*` container uses `e-commerce-backend-runtime:local` and selects its JAR with `SERVICE_MODULE`.
- `backend/Dockerfile.service`: retained for CI/production-style per-service images; canonical local Compose does not invoke Maven once per service.
- `frontend/Dockerfile.storefront`: storefront production build.
- `frontend/Dockerfile.admin`: admin production build.
- root `Dockerfile.backend` and `Dockerfile.frontend`: retained aggregate/backward-compatible images, not used by canonical per-service builds.

## Split topology

All split commands are run from repository root and explicitly set the project directory so every fragment uses root-relative paths consistently:

```bash
docker compose --project-directory . \
  -f compose/infrastructure.yml \
  -f compose/backend/all.yml \
  up -d --build
```

Merged split topology (default developer profile):

```bash
docker compose --project-directory . \
  -f compose/infrastructure.yml \
  -f compose/backend/all.yml \
  -f compose/seeds.yml \
  -f compose/frontend.yml \
  up -d --build
```

Add `--profile full`, `--profile heavy`, or `--profile observability` before `up` when that optional topology is required.

One backend service, assuming infrastructure/network peers already exist:

```bash
docker compose --project-directory . \
  -f compose/backend/be-catalog-api.yml \
  up -d --build
```

Per-service fragments intentionally omit broad startup ordering; use `backend/all.yml` or root compose for dependency orchestration.

## macOS `operation not permitted`

If the canonical root compose still fails while the project is under `~/Downloads`, distinguish a Compose path bug from macOS privacy permissions:

1. Run `./compose-up.sh config` first. If config resolves, paths are valid.
2. Check `echo "$COMPOSE_FILE"`; the wrapper ignores it, plain `docker compose` does not.
3. Docker Desktop may need access to Downloads/Desktop/Documents under macOS Privacy & Security / Files and Folders. Grant Docker Desktop access or move the repository to a developer directory such as `~/workspace`.
4. Re-run `./compose-up.sh up -d --build`.

The repository verifier `ci-cdconfigs/verify_compose_runtime_paths_v11.py` rejects parent build contexts and missing Dockerfiles before runtime.

## Build-context hygiene

`backend/.dockerignore` excludes Maven targets/logs and `frontend/.dockerignore` excludes Angular dependencies/build output. This keeps BuildKit from re-sending generated artifacts on every service image build.

All split fragments use the same `marketplace-network` name and do not mark it external. This allows the infrastructure + backend + frontend files to be merged safely in one Compose project without an "external network not found" race.

## Runtime network contract

Local Compose uses the Compose **service name** as the DNS contract. Do not use `container_name` as an application endpoint and do not use the host-published port for service-to-service calls.

```text
Host/browser -> gateway:       http://localhost:8080
Host/browser -> catalog:       http://localhost:8083

be-gateway -> catalog:         http://be-catalog-api:8080
checkout -> inventory:         http://be-inventory-api:8080
checkout -> pricing:           http://be-pricing-api:8080
payment -> simulator:          http://be-payment-simulator:8080
service -> Keycloak backchannel http://keycloak:8080
service -> Postgres:           jdbc:postgresql://postgres:5432/<db>
service -> MySQL:              jdbc:mysql://mysql:3306/<db>
service -> SQL Server:         jdbc:sqlserver://sqlserver:1433;databaseName=<db>;...
service -> Oracle:             jdbc:oracle:thin:@oracle:1521/FREEPDB1
service -> MongoDB:            mongodb://mongo:27017/<db>?replicaSet=rs0
service -> Redis:              redis:6379
service -> Kafka:              kafka:9092
service -> MinIO SDK:          http://minio:9000
service -> OpenSearch:         http://opensearch:9200
```

All Spring HTTP deployables listen on **container port 8080**. Their left-hand published port remains unique for developer access, for example `8083:8080` for Catalog and `8092:8080` for Payment. Workers/outbox/jobs are not published to the host.

`localhost` is valid in three local-only situations:

1. a healthcheck calling the same container (`localhost:8080`);
2. a browser/public URL such as storefront, Keycloak public issuer, or MinIO public media URL;
3. Kafka's EXTERNAL listener used from the developer host (`localhost:29092`).

Keycloak intentionally has split identity/backchannel addresses. Tokens are issued with `iss=http://localhost:8180/realms/marketplace`, so resource servers validate that issuer while fetching JWKs through `http://keycloak:8080/.../certs`. Changing the issuer to `keycloak:8080` would make browser-issued local tokens fail issuer validation.

The v13 verifier checks this contract:

```bash
python verification/verify_compose_runtime_network_v13.py
```

## Workstation-sized local topology (v16)

A cold local build no longer launches Maven independently for every backend service. `backend-runtime-build` executes the Maven reactor once with a shared BuildKit `/root/.m2` cache. Runtime containers share the resulting image and choose a service through `SERVICE_MODULE`.

The default stack keeps Oracle, Mongo-only community/notification services, async jobs/outbox/workers, and observability tooling disabled. Enable additional groups only when the test case needs them:

```bash
# default dev stack
docker compose up -d --build

# all optional bounded contexts/data stores
docker compose --profile full up -d --build

# Oracle + settlement path
docker compose --profile heavy up -d

# LGTM / dashboards / exporters
docker compose --profile observability up -d

# large data seeds (dependencies needed by the large seeds are profile-compatible)
docker compose --profile seed-large up -d
```

Local JVMs have bounded heap defaults. Kafka, OpenSearch, Keycloak and SQL Server also have developer memory budgets. `.env` keeps `COMPOSE_PARALLEL_LIMIT=2` so frontend/media/JMX/backend-runtime image builds do not stampede Docker Desktop.

`backend-runtime-build` is a one-shot build-owner service. After a successful `up`, seeing it as `Exited (0)` is expected; the backend application containers use the image it produced.

Tracing is sampled at `0.0` by default because LGTM is profile-gated. When running observability locally, enable tracing explicitly if needed:

```bash
TRACING_SAMPLING=1.0 docker compose --profile observability up -d
```

The v16 verifier checks the shared-image contract and profile layout:

```bash
python verification/verify_local_compose_optimization_v16.py
```
