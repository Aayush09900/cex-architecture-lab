// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {Test} from "forge-std/Test.sol";
import {CexSettlementAdapter} from "../src/CexSettlementAdapter.sol";

contract CexSettlementAdapterTest is Test {
    CexSettlementAdapter adapter;
    address operator = makeAddr("operator");
    address beneficiary = makeAddr("beneficiary");

    function setUp() public {
        adapter = new CexSettlementAdapter(operator);
    }

    function testOperatorCanRecordOnce() public {
        bytes32 id = keccak256("settlement-1");
        vm.prank(operator);
        adapter.recordSettlement(id, beneficiary, 1 ether);
        assertTrue(adapter.processed(id));
    }

    function testZeroAmountRejected() public {
        bytes32 id = keccak256("settlement-zero");
        vm.prank(operator);
        vm.expectRevert(CexSettlementAdapter.ZeroAmount.selector);
        adapter.recordSettlement(id, beneficiary, 0);
    }

    function testDuplicateRejected() public {
        bytes32 id = keccak256("settlement-duplicate");
        vm.startPrank(operator);
        adapter.recordSettlement(id, beneficiary, 1);
        vm.expectRevert(CexSettlementAdapter.AlreadyProcessed.selector);
        adapter.recordSettlement(id, beneficiary, 1);
        vm.stopPrank();
    }

    function testUnauthorizedRejected() public {
        bytes32 id = keccak256("settlement-auth");
        vm.prank(makeAddr("attacker"));
        vm.expectRevert(CexSettlementAdapter.NotOperator.selector);
        adapter.recordSettlement(id, beneficiary, 1);
    }

    function testZeroBeneficiaryRejected() public {
        vm.prank(operator);
        vm.expectRevert(CexSettlementAdapter.ZeroBeneficiary.selector);
        adapter.recordSettlement(keccak256("zero-beneficiary"), address(0), 1);
    }
}
