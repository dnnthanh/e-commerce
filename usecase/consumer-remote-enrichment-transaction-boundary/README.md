# Use case: Remote enrichment outside the database transaction

## Problem

A Kafka consumer often needs data from another service before it can persist its own projection. Putting an HTTP call inside `@Transactional` keeps a database connection and transaction open while waiting for network I/O. Under dependency latency this can exhaust the pool, increase lock time, and amplify retries.

A second subtle problem is self-invocation: a listener method calling its own `@Transactional` helper may bypass the Spring proxy, so the transaction annotation does not apply at all.

## Chosen flow

```text
Kafka listener
    |
    | typed DomainEvent
    v
remote enrichment outside DB transaction
    |
    | resolved immutable input
    v
@Transactional materializer bean
    |-- Inbox claim
    |-- local DB mutation
    `-- local Outbox write when required
```

The listener/consumer adapter coordinates remote reads. A separate Spring bean owns the transactional materialization boundary.

## Examples in the project

- Review: delivered-order event -> Order/Catalog lookup -> `VerifiedPurchaseMaterializer` -> Inbox + verified-purchase rows in MySQL.
- Fulfillment: paid-order event -> Order/Inventory lookup -> `ShipmentMaterializer` -> Inbox + shipment rows in SQL Server.
- Settlement: fulfillment-completed event -> Order lookup -> `SettlementMaterializer` -> Inbox + settlement rows in Oracle.

## Why Inbox must be in the same transaction

If an Inbox marker commits first and the business mutation fails afterward, redelivery sees the marker and skips work, permanently losing the business effect. Claiming the event and applying the local state change in one transaction removes that crash window.

## Retry semantics

Remote lookup failure occurs before Inbox is claimed, therefore Kafka redelivery can safely retry the enrichment. Once the materializer transaction commits, duplicate delivery is ignored by the Inbox uniqueness rule.

## Anti-patterns rejected

- HTTP/REST call inside a long database transaction.
- Protected/private `@Transactional` helper called through self-invocation.
- Inbox commit before the local business mutation.
- Cross-service database access used to avoid an HTTP boundary.
