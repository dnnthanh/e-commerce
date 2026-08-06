# Use Case — Notify Customers About Material Seller Changes

## Problem
A seller can update many fields, but notifying every customer for every edit is noisy. Some changes are important: identity/verification, suspension/closure, or fulfillment/policy changes affecting active relationships. Large sellers may have a very large audience.

## Candidate solutions
1. Notify on every seller update — rejected due to noise/fan-out cost.
2. UI polling — weak realtime UX and unnecessary reads.
3. Classify material changes, emit a domain event, resolve an explicit audience asynchronously — selected.

## Design concerns
- material-change classification;
- followers/subscribers vs customers with active orders;
- chunked/checkpointed fan-out;
- idempotent notification key;
- failure/retry/DLT;
- realtime plus persisted inbox;
- rate limiting/digest option for noisy changes.

## Practice
Seed a seller with a large follower audience, trigger a material change, observe worker backlog/fan-out/realtime delivery, kill the worker midway, restart and prove exactly one logical notification per recipient.
