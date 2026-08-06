# Use Case — Media processing idempotency and reference-safe deletion

## Problem
Uploads and image transformations retry. Without checksum/idempotency, workers create duplicate variants; deleting referenced media can break published products.

## Constraints
- Binary objects stay in object storage, DB stores metadata/object keys.
- Variant generation is deterministic.
- Referenced asset cannot be physically deleted immediately.

## Candidate solutions
- Store image bytes in relational DB — rejected.
- Delete object immediately on detach — rejected because other references may exist.

## Selected solution
MediaUseCase owns upload metadata and completion, MediaProcessingUseCase owns scanning/processing state, checksum identifies duplicate binaries, worker emits deterministic variants and deletion is gated by reference count/delayed cleanup.

## Important failure modes
- Transformation retry -> existing deterministic variant is reused/ignored.
- Scanner failure -> FAILED state.
- Delete while referenced -> rejected/deferred.

## Main implementation references
- `backend/services/be-media-api/.../MediaUseCase.java`
- `backend/services/be-media-api/.../MediaProcessingUseCase.java`
- `backend/services/be-media-worker/.../MediaVariantWorker.java`

## Verification
- `MediaAssetTest`
- `verification/verify_media_seed.py`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
