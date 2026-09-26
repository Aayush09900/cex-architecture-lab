// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/// @title CEX Settlement Adapter
/// @notice Educational boundary between an internal CEX ledger and an EVM settlement layer.
/// @dev This contract is not a custody wallet and should not be used with real funds.
contract CexSettlementAdapter {
    address public immutable operator;
    mapping(bytes32 => bool) public processedSettlements;

    event SettlementRecorded(
        bytes32 indexed settlementId,
        address indexed recipient,
        uint256 amount,
        bytes32 asset,
        uint256 timestamp
    );

    error NotOperator();
    error AlreadyProcessed();
    error InvalidRecipient();
    error ZeroAmount();

    constructor(address operator_) {
        if (operator_ == address(0)) revert InvalidRecipient();
        operator = operator_;
    }

    modifier onlyOperator() {
        if (msg.sender != operator) revert NotOperator();
        _;
    }

    function recordSettlement(
        bytes32 settlementId,
        address recipient,
        uint256 amount,
        bytes32 asset
    ) external onlyOperator {
        if (processedSettlements[settlementId]) revert AlreadyProcessed();
        if (recipient == address(0)) revert InvalidRecipient();
        if (amount == 0) revert ZeroAmount();

        processedSettlements[settlementId] = true;
        emit SettlementRecorded(settlementId, recipient, amount, asset, block.timestamp);
    }
}
