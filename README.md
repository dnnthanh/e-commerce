# Marketplace E-commerce Platform

Production-oriented multi-seller marketplace reference implementation with Java 25/Spring Boot services, Angular storefront/admin applications, Kafka/outbox workflows, Keycloak authorization, OpenSearch discovery, multi-database persistence and observability.

## Run local developer stack

The default Compose topology is intentionally workstation-sized. It builds the backend Maven reactor once, then starts the core marketplace APIs and infrastructure:

```bash
docker compose up -d --build
```

Optional topology is profile-gated:

```bash
# every optional bounded context/data store (without observability tooling)
docker compose --profile full up -d --build

# Oracle/settlement path only
docker compose --profile heavy up -d

# LGTM, Alloy, dashboards and exporters
docker compose --profile observability up -d
```

If your shell has unrelated Compose environment variables, use the pinned wrapper:

```bash
./compose-up.sh up -d --build
```

See [`compose/README.md`](compose/README.md) for split/service-specific commands and macOS path-permission troubleshooting.

## Main areas

- `backend/`: platform starter plus API/worker/outbox/job deployables.
- `frontend/`: Angular storefront and admin with shared envelope/trace/error contract.
- `infrastructure/`: PostgreSQL, MySQL, SQL Server, Oracle, MongoDB, Redis, Kafka, OpenSearch, MinIO, Keycloak and observability assets.
- `database-labs/`: multi-engine performance/DBA labs including 32 deep production incidents.
- `docs/architecture/`: bounded-context and cross-cutting architecture decisions.
- `docs/superpowers/specs|plans/`: design and implementation planning artifacts.
- `ci-cdconfigs/`: architecture/contract/config verification gates.

## Frontend coverage

Storefront covers discovery/product/cart/checkout/order/shipment/return/community/notification/account flows. Admin covers seller/catalog/media/pricing/promotion/inventory/order/fulfillment/payment/return/moderation/settlement/security/audit/operations.

Browser code does not call `/internal/**`. See [`docs/architecture/015-frontend-runtime-and-dba-lab-depth.md`](docs/architecture/015-frontend-runtime-and-dba-lab-depth.md) for intentional API gaps and boundaries.

## Database labs

All four relational engines have deep incident packs:

- PostgreSQL 17
- MySQL 8.4 / InnoDB
- SQL Server 2022
- Oracle 23

Start at [`database-labs/README.md`](database-labs/README.md). Deep labs require actual-plan evidence, hot/long-tail parameters, concurrency, trade-off analysis and regression acceptance—not only a rewritten query/index.

### Docker local-network convention

For the normal local developer stack, use `./compose-up.sh up -d --build`. Use `--profile full` only when the complete topology is required. Spring services use container port `8080`; Compose service names provide DNS between containers. For example, Checkout calls Inventory at `http://be-inventory-api:8080`, while a developer calls Checkout from macOS at `http://localhost:8090`.

If a backend container is unhealthy, inspect it with:

```bash
docker compose ps
docker compose logs --tail=200 <service>
docker inspect --format '{{json .State.Health}}' <container>
```

Run `python verification/verify_compose_runtime_network_v13.py` before changing local ports or service URLs; it detects missing required application environment variables and stale host-port routing.
