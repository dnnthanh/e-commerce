# Use case: keeping media metadata and object storage consistent

## Problem

Catalog needs deterministic image variants for `HOME_CARD`, `SEARCH_CARD`, and `PRODUCT_DETAIL`. Relational DB should not store image binary, while MinIO/S3 must contain the object referenced by `object_key`.

## Options considered

1. Store image bytes in PostgreSQL: simple consistency, poor object-delivery/CDN characteristics and bloats DB backups.
2. Insert DB metadata first and upload files manually: easy to drift; a product can point to a missing object.
3. Deterministic two-phase bootstrap: Liquibase writes stable object keys and a dedicated idempotent media-seed job uploads the exact object tree.

## Selected solution

Use option 3.

- Catalog Liquibase SQL stores metadata/object keys only.
- `infrastructure/media-seed/assets` contains legally safe generated local assets.
- `media-seed` container creates the MinIO bucket and mirrors the deterministic object tree.
- `verification/verify_media_seed.py` validates formats/dimensions/object tree before packaging.
- Full Docker smoke testing later verifies HTTP access from MinIO and product-card rendering from Angular.

## Failure/recovery

- MinIO unavailable: seed job fails and Docker health/dependency orchestration retries/restarts it; DB metadata remains deterministic.
- DB unavailable: Liquibase waits/fails independently; object upload is idempotent and may safely run again.
- Partial upload: `mc mirror --overwrite` repairs missing/outdated objects on rerun.
- Missing local asset: static media verification fails before delivery.

## Production note

Real production media is uploaded through `be-media-*`, virus/content checks, variant generation and object storage/CDN. The seed job exists only to bootstrap a useful local demo.
