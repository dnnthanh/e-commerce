# Demo cross-service scenarios

The demo seed is intentionally a small set of **business scenarios**, not 100 cloned rows with a
modulo status. Identifiers line up across Checkout, Order, Payment and Fulfillment where the workflow
has reached that boundary.

| Scenario | Checkout | Order | Payment | Fulfillment | Why it exists |
| --- | --- | --- | --- | --- | --- |
| 0001–0004 | COMPLETED | COMPLETED | PAID | DELIVERED / partial shipment | normal happy paths, including multi-seller split |
| 0005 | COMPLETED | COMPLETED | PARTIALLY_REFUNDED | RETURN_TO_SENDER | refund/settlement adjustment practice |
| 0006 | COMPLETED | FULFILLING | PAID | two packages, one still READY_TO_SHIP | partial shipment and seller split |
| 0007 | COMPLETED | COMPLETED | REFUNDED | DELIVERY_FAILED + another delivered package | partial logistics failure after payment |
| 0008–0010 | COMPLETED | PAID/FULFILLING/COMPLETED | PAID | mixed ready/in-transit/delivered | lifecycle queries |
| 0011 | PAYMENT_PENDING | PAYMENT_PENDING | PENDING | none | customer is still paying |
| 0012 | PAYMENT_UNKNOWN | PAYMENT_PENDING | UNKNOWN + reconciliation row | none | provider timeout/reconciliation |
| 0013 | FAILED_RETRYABLE | none | none | none | failure before durable order creation |
| 0014 | COMPENSATED | none | none | none | promotion/inventory reservation released |
| 0015 | STARTED | none | none | none | checkout started but no reservation yet |
| 0016 | RESERVED | none | none | none | reserved resources waiting for next step |
| 0017 | ORDER_CREATED snapshot | CANCELLED | FAILED | none | **eventual-consistency drift** for operations/reconciliation labs |
| 0018–0019 | COMPLETED | COMPLETED | PAID | DELIVERED | older customer-history cases |
| 0020 | PAYMENT_PENDING | PAYMENT_PENDING | PENDING | none | current active checkout |

Scenario 0017 is deliberately not “perfectly synchronized”. Real distributed systems can expose a
short-lived **eventual-consistency drift**: Payment failure has already cancelled Order while the
Checkout read model still shows its previous durable step. Operations/reconciliation exercises should
detect this rather than hiding it in fake-perfect data.

The large dataset follows the same philosophy statistically: hot sellers/SKUs, long-tail demand,
seasonality, rare UNKNOWN/FAILED states, stockouts, refund adjustments and non-uniform customer
frequency. It remains deterministic so before/after execution plans are reproducible.
