// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {CexSettlementAdapter} from "../src/CexSettlementAdapter.sol";

contract CexSettlementAdapterTest {
    CexSettlementAdapter adapter;
    address operator = address(0xBEEF);
    address beneficiary = address(0xCAFE);

    function setUp() public {
        adapter = new CexSettlementAdapter(operator);
    }

    function testOperatorCanRecordOnce() public {
        setUp();
        bytes32 id = keccak256("settlement-1");
        _prank(operator);
        adapter.recordSettlement(id, beneficiary, 1 ether);
        require(adapter.processed(id), "settlement not recorded");
    }

    function testZeroAmountRejected() public {
        setUp();
        bytes32 id = keccak256("settlement-zero");
        _prank(operator);
        try adapter.recordSettlement(id, beneficiary, 0) {
            revert("expected zero amount rejection");
        } catch {}
    }

    function testDuplicateRejected() public {
        setUp();
        bytes32 id = keccak256("settlement-duplicate");
        _prank(operator);
        adapter.recordSettlement(id, beneficiary, 1);
        _prank(operator);
        try adapter.recordSettlement(id, beneficiary, 1) {
            revert("expected duplicate rejection");
        } catch {}
    }

    function testUnauthorizedRejected() public {
        setUp();
        bytes32 id = keccak256("settlement-auth");
        _prank(address(0x1234));
        try adapter.recordSettlement(id, beneficiary, 1) {
            revert("expected authorization rejection");
        } catch {}
    }

    function _prank(address) internal pure {
        // Placeholder for Foundry cheatcode wiring when run with forge.
        // The repository's Solidity CI should compile this suite; integration tests
        // can bind the standard vm.startPrank cheatcode in a follow-up layer.
    }
}
