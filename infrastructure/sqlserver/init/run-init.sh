#!/usr/bin/env bash
set -euo pipefail
/opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "IF DB_ID('order_db') IS NULL CREATE DATABASE order_db; IF DB_ID('fulfillment_db') IS NULL CREATE DATABASE fulfillment_db;"
