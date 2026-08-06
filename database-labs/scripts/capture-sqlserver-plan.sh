#!/usr/bin/env sh
set -eu
mkdir -p database-labs/sqlserver/04-plan-analysis/runtime
docker compose exec -T sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'Marketplace!2026' -C -d order_db -i /dev/stdin < database-labs/sqlserver/01-baseline/order-customer-history.sql > database-labs/sqlserver/04-plan-analysis/runtime/order-baseline.txt
