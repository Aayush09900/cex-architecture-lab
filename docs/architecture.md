# CEX Architecture Notes

## Why split the systems?

A centralized exchange has two different truths:

1. **Trading/accounting truth** — what the exchange says the user owns and what orders are locked.
2. **Blockchain settlement truth** — what external networks have actually confirmed.

They must be connected through explicit boundaries rather than treating an RPC balance as the internal ledger.

## Order lifecycle

The order lifecycle uses the statuses defined by the service. The transition table is the contract; any transition not listed below is invalid.

| Current state | Allowed next state(s) | Meaning |
| --- | --- | --- |
| `NEW` | `OPEN`, `CANCELLED`, `REJECTED` | A created order is accepted for matching, cancelled before opening, or rejected. |
| `OPEN` | `PARTIALLY_FILLED`, `FILLED`, `CANCELLED` | An open order can receive a partial fill, complete, or be cancelled. |
| `PARTIALLY_FILLED` | `FILLED`, `CANCELLED` | Remaining quantity can complete or be cancelled. |
| `FILLED` | *(none)* | Terminal state; no further transition is allowed. |
| `CANCELLED` | *(none)* | Terminal state; no further transition is allowed. |
| `REJECTED` | *(none)* | Terminal state; no further transition is allowed. |

Examples of invalid transitions:

- `NEW → FILLED`: an order cannot be filled before being opened and matched.
- `OPEN → REJECTED`: once an order is open, this lifecycle does not transition it to rejected; cancel it or advance it through fill states.
- `PARTIALLY_FILLED → OPEN`: filled quantity cannot be undone by returning to the open state.
- `FILLED → CANCELLED`: terminal states cannot be reopened or changed.
- `CANCELLED → OPEN`: cancellation is terminal, so reopening requires a new order with a new identifier.

A transition must be validated against the current persisted state at the point of update. In a multi-worker deployment, a read-check-write sequence without locking or a compare-and-set/optimistic version check is not sufficient: concurrent commands could both read the same old state and overwrite each other. The state transition and any corresponding ledger reservation/release must be committed atomically in the persistence boundary.

## Idempotent order creation

Treat a client-provided idempotency key (in this service, `clientOrderId`) as the identity of a create-order request within its authenticated user/account scope:

1. Normalize and validate the request, then compute a stable fingerprint of the material order fields (for example symbol, side, type, quantity, and price).
2. Enforce a durable unique constraint on the key plus user/account scope. Create the order and reserve any required funds in one transaction.
3. If the same key is replayed with the same fingerprint, return the previously created order and its original result. Do not create another order, reserve funds again, or emit a second create event.
4. If the same key is reused with a different fingerprint, reject the request with a conflict instead of silently returning an unrelated order.
5. Store the idempotency result long enough to cover client retries and operational replay. Downstream ledger/outbox consumers should use their own unique event keys as well.

The current in-memory lab implementation uses `clientOrderId` with `computeIfAbsent` to reuse an order within one process. That is a demonstration of the basic idea, not a production guarantee: the map is not durable across restarts, it is not shared between replicas, and it does not verify that a repeated key has the same request payload. A production implementation must enforce the durable uniqueness, payload-mismatch, and transaction-boundary rules above in the database.

An order service should be deterministic and idempotent. Replaying the same command must not create a second order or second ledger reservation.

## Ledger model

Use double-entry accounting:

| Account | Debit | Credit |
| --- | --- | --- |
| User available | settlement in | withdrawal / lock |
| User locked | order lock | order release |
| Exchange hot wallet | deposit sweep | withdrawal |
| Fees revenue | fee debit | fee credit |

Every economic event should balance to zero across the ledger.

## Settlement coordinator

The settlement boundary converts blockchain observations into internal events:

```text
RPC / Indexer
    ↓
Observed transaction
    ↓
Confirmation tracker
    ↓
Idempotency key
    ↓
Ledger event
    ↓
Reconciliation
```

An observed transaction should not immediately become spendable balance. The policy should define confirmation requirements, reorg handling, and failure states.

## Withdrawal path

```text
API request
   ↓
authorization + risk checks
   ↓
ledger lock
   ↓
custody/signing boundary
   ↓
broadcast
   ↓
tx hash recorded
   ↓
confirmation tracker
   ↓
ledger finalize / unlock
```

At every stage the operation needs a durable identifier so retries cannot duplicate value movement.

## Reconciliation

Reconciliation should answer:

- Which deposits are visible on-chain but missing internally?
- Which internal withdrawals lack a matching confirmed transaction?
- Which ledger entries disagree with custody balances?
- Which transactions are stuck beyond their expected lifecycle?

Reconciliation is a control plane, not a replacement for the source ledger.

## Operational controls

- rate limiting
- role-based access control
- withdrawal allowlists
- separation of hot and cold funds
- audit logs
- alerting on invariant violations
- deterministic retries
- dead-letter queues
- incident runbooks
- database backups and recovery drills

## Implementation sequence

1. Define API contracts.
2. Implement an in-memory state machine.
3. Add a persistent double-entry ledger.
4. Add idempotent transaction processing.
5. Add reconciliation.
6. Add asynchronous workers and queues.
7. Add integration tests against a Sepolia test environment.
8. Add production observability and recovery controls.

This document describes an engineering learning lab, not a production exchange design.
