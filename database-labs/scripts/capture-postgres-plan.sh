#!/usr/bin/env sh
set -eu
mkdir -p database-labs/postgresql/04-plan-analysis/runtime
docker compose exec -T postgres sh -c "PGPASSWORD=catalog_password psql -U catalog -d catalog_db" < database-labs/postgresql/01-baseline/outbox-pending.sql > database-labs/postgresql/04-plan-analysis/runtime/outbox-baseline.txt
