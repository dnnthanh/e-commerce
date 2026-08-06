# Use Case — Multi-package fulfillment allocation and reconciliation

## Problem
One seller order can become multiple packages. Independent package creation can over-allocate a line and carrier callbacks can arrive out of order.

## Constraints
- Allocated quantity cannot exceed ordered quantity.
- Carrier event ordering must be monotonic/idempotent.
- Shipment state must be reconcilable with Order snapshots.

## Candidate solutions
- One shipment per order — rejected for marketplace/warehouse splits.
- Trust every carrier webhook — rejected because retries/out-of-order delivery are normal.

## Selected solution
ShipmentAllocationUseCase validates allocations against immutable Order snapshots and existing packages. ShipmentLifecycleUseCase applies monotonic carrier sequence/state transitions. FulfillmentReconciliationUseCase exposes ordered/allocated/delivered drift and FulfillmentSlaUseCase detects breaches.

## Important failure modes
- Second package over-allocates -> rejected.
- Stale carrier sequence -> ignored.
- Order/shipment drift -> reconciliation result instead of silent correction.

## Main implementation references
- `backend/services/be-fulfillment-api/.../ShipmentAllocationUseCase.java`
- `backend/services/be-fulfillment-api/.../ShipmentLifecycleUseCase.java`
- `backend/services/be-fulfillment-api/.../FulfillmentReconciliationUseCase.java`

## Verification
- `ShipmentTest`
- `ShipmentStatusTest`
- `verification/src/CrossContextWorkflowSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
