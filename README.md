# CEX Architecture Lab

A practical architecture lab for designing the **backend foundations of a centralized crypto exchange**.

The goal is to document and validate the boundaries that matter when exchange infrastructure separates trading, internal accounting, custody, compliance, and blockchain settlement.

## Architecture

```text
Clients
  │
  ├── REST / WebSocket APIs
  │
  ▼
API Gateway / Auth
  │
  ├── Order Service ──► Matching Engine
  │                       │
  │                       ▼
  │                 Trade Events
  │                       │
  ▼                       ▼
Internal Ledger ◄──── Settlement Coordinator
  │                       │
  ├── Available balance   ├── Deposit detection
  ├── Locked balance      ├── Confirmation tracking
  └── Double-entry        └── Withdrawal execution
          │
          ▼
     Reconciliation
          │
          ▼
Monitoring / Audit / Recovery
```

Detailed decisions are documented in [docs/architecture.md](docs/architecture.md).

## Engineering invariants

- **Ledger-first accounting:** user balances are derived from an internal double-entry ledger, not directly from blockchain RPC reads.
- **Idempotency:** deposits, withdrawals, settlement callbacks, and external events must be safe to process more than once.
- **Reconciliation:** blockchain state and internal ledger state are periodically compared.
- **Confirmation awareness:** an observed transaction is not automatically a final transaction.
- **Separation of concerns:** matching, accounting, custody, compliance, and chain settlement remain independently testable.
- **Auditability:** important state changes carry durable identifiers and timestamps.

## Reliability topics

- Order lifecycle and state transitions
- Double-entry ledger design
- Wallet and custody boundaries
- Withdrawal controls
- Deposit detection
- Confirmation depth
- Retry and backoff
- Reconciliation
- WebSocket event delivery
- Idempotency
- Observability
- Disaster recovery

## Security

GitHub Actions use least-privilege permissions. The repository also includes:

- secret scanning with Gitleaks
- CodeQL analysis for GitHub Actions
- Dependabot configuration
- protected environment/secret patterns
- CI validation on push and pull request
- a security policy and `.env.example`

See [SECURITY.md](SECURITY.md).

## CI/CD

Every push and pull request to `main` runs the project checks. Security scanning also runs on a schedule.

The workflows are intentionally small and reviewable so the repository demonstrates **engineering process**, not just documentation.

## Current status

This repository is an architecture and engineering lab. It does not implement a production exchange, custody system, or matching engine yet.

The next implementation layer is planned around:

1. order-service API contract
2. deterministic order state machine
3. double-entry ledger schema
4. idempotent transaction processor
5. reconciliation worker
6. event-driven settlement adapter
7. integration tests

## Related work

- [Ayu Wallet](https://github.com/Aayush09900/ayuwallet_daap) — wallet and transaction-flow DApp
- [Blockchain Transaction Service Lab](https://github.com/Aayush09900/Blockchain-transcation-service-lab) — idempotent transaction processing lab
- [Metaverse Flat Marketplace](https://github.com/Aayush09900/metaverse-flat-marketplace) — on-chain inventory and booking flows

> Educational/testnet project. Do not use this repository as a production exchange or custody implementation without independent security, operational, and compliance review.
