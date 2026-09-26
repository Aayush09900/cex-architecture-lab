# CEX Architecture Notes

## Why split the systems?

A centralized exchange has two different truths:

1. **Trading/accounting truth** — what the exchange says the user owns and what orders are locked.
2. **Blockchain settlement truth** — what external networks have actually confirmed.

They must be connected through explicit boundaries rather than treating an RPC balance as the internal ledger.

## Order lifecycle

```text
NEW → VALIDATED → OPEN → PARTIALLY_FILLED → FILLED
                     │
                     └──────────────→ CANCELLED
```

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
