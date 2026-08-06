# Feature 013 — Use-case coverage audit

`CORE-DEEP` means the implemented primary flow contains real invariants/failure/idempotency/concurrency/recovery behavior and is not a repository wrapper. `SPEC-PARTIAL` means the core flow is deep but one or more secondary mandatory bullets from Spec 013 are still open.

| API context | Status | Application use cases | Public operations | Test sources | Remaining spec gap |
| --- | --- | --- | --- | ---: | --- |
| `be-audit-api` | **CORE-DEEP** | `AuditQueryUseCase` | search | 1 | — |
| `be-authorization-api` | **SPEC-PARTIAL** | `AuthorizationUseCase`<br>`AuthorizationChangeRecorder` | snapshot, assignRole, removeRole, assignSeller, removeSeller, reconcilePending, ReconciliationResult<br>prepareChange, markApplied, markFailed, pending | 2 | abuse rate limiting for privileged mutations |
| `be-cart-api` | **SPEC-PARTIAL** | `CartImplement`<br>`CartMutationUseCase`<br>`CartCheckoutValidationImplement` | guest, guestPut, get, put, remove, saveForLater, moveToCart, merge<br>getOrCreate, upsert, remove, saveForLater, moveToCart, mergeGuest<br>validate, ValidationIssue, ValidationResult | 1 | durable expiration + explicit coupon preview |
| `be-catalog-api` | **SPEC-PARTIAL** | `SkuSnapshotUseCase`<br>`VariantGenerationUseCase`<br>`ProductUseCase` | get<br>generate<br>create, publish, getPublished, search | 2 | category tree, brand lifecycle, seller listing overlay, resumable bulk import |
| `be-checkout-api` | **SPEC-PARTIAL** | `CheckoutImplement`<br>`CheckoutRecoveryUseCase`<br>`CheckoutProcessUseCase` | checkout<br>recover<br>start, fail | 4 | address validation + shipping quote snapshot |
| `be-comment-api` | **SPEC-PARTIAL** | `CommentQueryUseCase`<br>`CommentCommandUseCase` | listThreads, listReplies<br>create, reply, edit, delete, hide, unhide, report, react, removeReaction | 1 | blocked-user policy + hot-thread cursor path |
| `be-fulfillment-api` | **SPEC-PARTIAL** | `FulfillmentReconciliationUseCase`<br>`FulfillmentSlaUseCase`<br>`ShipmentLifecycleUseCase`<br>`ShipmentAllocationUseCase`<br>`FulfillmentUseCase` | reconcile, LineReconciliation, allocationDrift, deliveryRemaining<br>findBreaches<br>changeStatus, changeStatusForSeller, applyCarrierCallback, byOrder<br>allocate, AllocationCommand, AllocationLine<br>shipments, update | 2 | shipping quote + carrier signature verification |
| `be-inventory-api` | **CORE-DEEP** | `InventoryTransferUseCase`<br>`ReservationLifecycleUseCase`<br>`InventoryReservationUseCase`<br>`ReserveInventoryUseCase`<br>`InventoryReconciliationUseCase`<br>`InventoryQueryUseCase`<br>`InventoryAdjustmentUseCase` | transfer, TransferResult<br>attach, release, byOrder<br>reserve, confirm, release, ReservationResult<br>reserve<br>reconcile<br>balances, available<br>adjust, AdjustmentResult | 2 | — |
| `be-media-api` | **CORE-DEEP** | `MediaProcessingUseCase`<br>`MediaUseCase` | startScan, scanPassed, scanFailed, markVariantGenerated, markReady, requestDelete<br>create, complete, variants, readiness | 1 | — |
| `be-notification-api` | **CORE-DEEP** | `NotificationDeliveryUseCase`<br>`NotificationInboxUseCase`<br>`NotificationDispatchUseCase`<br>`NotificationReplayUseCase` | deliverDue, DeliveryBatchResult<br>list, unreadCount, markRead, markAllRead, preference, savePreference, setSellerFollow<br>prepare<br>replay | 2 | — |
| `be-operations-api` | **SPEC-PARTIAL** | `OperationsUseCase` | open, recover, resolve | 1 | DLQ inspection + kill switch + durable bulk admin job |
| `be-order-api` | **SPEC-PARTIAL** | `OrderCommandUseCase`<br>`OrderQueryUseCase` | create, cancel, markPaid, markFulfilling, markCompleted, expireUnpaidBatch, cancelSellerOrder, manualOverrideCancel<br>internalGet, getOwned, search | 1 | timeline/notes/tags + reconciliation diagnostic endpoint |
| `be-payment-api` | **SPEC-PARTIAL** | `PaymentImplement`<br>`RefundUseCase`<br>`PaymentQueryUseCase`<br>`PaymentCallbackUseCase` | create, reconcile, Result<br>refund, RefundResult<br>list<br>handle | 1 | real-provider signed webhook verification |
| `be-pricing-api` | **SPEC-PARTIAL** | `PriceQuoteUseCase`<br>`PricingUseCase` | quote, PriceQuote<br>price | 2 | bulk update/history workflow |
| `be-promotion-api` | **SPEC-PARTIAL** | `PromotionUseCase`<br>`PromotionCheckoutReservationUseCase`<br>`PromotionReservationUseCase` | evaluate<br>reserve, confirm, release<br>evaluate, reserve, confirm, compensate | 2 | tiered/shipping benefit + audited manual override |
| `be-return-api` | **SPEC-PARTIAL** | `ReturnUseCase` | create, list, approve, reject, receive, inspect, openDispute, resolveDispute, refund | 1 | evidence/media + audit timeline projection |
| `be-review-api` | **SPEC-PARTIAL** | `ReviewUseCase`<br>`ReviewModerationUseCase` | list, summary, create, edit, delete, markHelpful<br>hide, restore | 1 | seller response + report queue + anti-spam/rate-limit |
| `be-search-api` | **SPEC-PARTIAL** | `ProductSearchUseCase` | search | 1 | autocomplete/typo + executable full-reindex alias swap |
| `be-seller-api` | **SPEC-PARTIAL** | `ShopProfileUseCase`<br>`SellerLifecycleUseCase` | get, list, update<br>verify, activate, suspend | 2 | staff/warehouse/settings/dashboard |
| `be-settlement-api` | **SPEC-PARTIAL** | `SettlementUseCase`<br>`SettlementEventUseCase` | list, approve, closePeriod, placeHold, releaseHold, rebuildBalance<br>apply | 1 | statement/payout-reference workflow |

## Interpretation

- All API bounded contexts currently have at least one `@UseCase` application entry point and at least one backend test source.
- The table does **not** equate test-source presence with executed Java-25 JUnit evidence.
- Remaining gaps are deliberately not filled with thin placeholder classes; they stay in the approved gap register until implemented with tests and persistence/integration semantics.
- Worker/job/outbox deployables are not required to have `@UseCase`; they own transport/scheduler/publisher responsibilities.
