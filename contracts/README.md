# Solidity settlement adapter

The `CexSettlementAdapter` represents the **on-chain settlement boundary** of the lab.

## Why Solidity here?

The exchange should keep its authoritative user accounting in an internal ledger. The EVM contract is deliberately limited to recording settlement instructions/events so the architecture demonstrates a clean boundary between:

- Java/Spring Boot — API, order workflow and business rules
- Internal ledger — authoritative accounting (future implementation layer)
- Solidity/EVM — chain-side settlement boundary
- JavaScript — operator/user-facing console

## Invariants

1. Only the configured operator can submit a settlement record.
2. A settlement ID can be processed only once (idempotency).
3. Zero-value settlements are rejected.
4. Zero-address recipients are rejected.
5. Every accepted settlement emits an auditable event.

This is an educational/testnet contract. It intentionally does **not** hold user funds or implement custody.
