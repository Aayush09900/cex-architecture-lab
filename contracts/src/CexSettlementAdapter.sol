// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/// @title CEX Settlement Adapter
/// @notice Educational settlement boundary. It records authorized settlement intents;
///         it is deliberately not a custody wallet or production exchange contract.
contract CexSettlementAdapter {
    address public immutable operator;
    mapping(bytes32 => bool) public processed;

    event SettlementRecorded(bytes32 indexed settlementId, address indexed beneficiary, uint256 amount);

    error NotOperator();
    error ZeroBeneficiary();
    error ZeroAmount();
    error AlreadyProcessed();

    constructor(address operator_) {
        if (operator_ == address(0)) revert ZeroBeneficiary();
        operator = operator_;
    }

    function recordSettlement(
        bytes32 settlementId,
        address beneficiary,
        uint256 amount
    ) external {
        if (msg.sender != operator) revert NotOperator();
        if (beneficiary == address(0)) revert ZeroBeneficiary();
        if (amount == 0) revert ZeroAmount();
        if (processed[settlementId]) revert AlreadyProcessed();

        processed[settlementId] = true;
        emit SettlementRecorded(settlementId, beneficiary, amount);
    }
}
