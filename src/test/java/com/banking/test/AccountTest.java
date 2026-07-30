package com.banking.test;

import com.banking.exception.InsufficientFundsException;
import com.banking.model.*;
import com.banking.pattern.factory.AccountFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Account operations.
 * Demonstrates JUnit 5 testing with assertions, exceptions, and lifecycle hooks.
 */
@DisplayName("Account Unit Tests")
class AccountTest {

    private SavingsAccount savingsAccount;
    private CurrentAccount currentAccount;

    @BeforeEach
    void setUp() {
        savingsAccount = AccountFactory.createSavingsAccount("CUST001", 10000, "SBIT001", "Main");
        currentAccount = AccountFactory.createCurrentAccount("CUST002", 50000, 10000, "SBIT001", "Main");
    }

    // ── Savings Account ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Deposit increases balance correctly")
    void testDeposit() {
        savingsAccount.deposit(5000, "Test deposit");
        assertEquals(15000.0, savingsAccount.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Withdrawal reduces balance and records transaction")
    void testWithdrawal() throws InsufficientFundsException {
        savingsAccount.withdraw(3000, "Test withdrawal");
        assertEquals(7000.0, savingsAccount.getBalance(), 0.01);
        assertEquals(1, savingsAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Withdrawal below minimum balance throws InsufficientFundsException")
    void testWithdrawalBelowMinBalance() {
        InsufficientFundsException ex = assertThrows(
            InsufficientFundsException.class,
            () -> savingsAccount.withdraw(9600, "Over-limit withdrawal")
        );
        assertNotNull(ex);
        assertEquals("ERR_INSUFFICIENT_FUNDS", ex.getErrorCode());
    }

    @Test
    @DisplayName("Interest calculation returns positive value")
    void testInterestCalculation() {
        double interest = savingsAccount.calculateInterest();
        assertTrue(interest > 0, "Interest should be positive for non-zero balance");
        // Expected: 10000 * 4.0/100 / 4 = 100.0
        assertEquals(100.0, interest, 0.01);
    }

    @Test
    @DisplayName("Interest is applied and recorded as transaction")
    void testApplyInterest() {
        double balanceBefore = savingsAccount.getBalance();
        savingsAccount.applyInterest();
        assertTrue(savingsAccount.getBalance() > balanceBefore);
        assertEquals(1, savingsAccount.getTransactions().size());
        assertEquals(TransactionType.INTEREST_CREDIT,
                     savingsAccount.getTransactions().get(0).getType());
    }

    @Test
    @DisplayName("Operations on CLOSED account throw IllegalStateException")
    void testClosedAccountOperations() {
        savingsAccount.setStatus(Account.AccountStatus.CLOSED);
        assertThrows(IllegalStateException.class,
                () -> savingsAccount.deposit(100, "Should fail"));
    }

    // ── Current Account ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Current account allows overdraft within limit")
    void testOverdraftAllowed() throws InsufficientFundsException {
        // Balance 50000 + OD 10000 = effective 60000
        assertDoesNotThrow(() -> currentAccount.withdraw(55000, "Overdraft withdrawal"));
    }

    @Test
    @DisplayName("Current account blocks withdrawal beyond overdraft limit")
    void testOverdraftExceeded() {
        assertThrows(InsufficientFundsException.class,
                () -> currentAccount.withdraw(61000, "Beyond OD"));
    }

    @Test
    @DisplayName("Current account earns no interest")
    void testCurrentAccountNoInterest() {
        assertEquals(0.0, currentAccount.calculateInterest(), 0.001);
    }

    // ── Fixed Deposit ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Fixed deposit maturity amount is greater than principal")
    void testFDMaturityAmount() {
        FixedDepositAccount fd = AccountFactory.createFixedDepositAccount(
                "CUST001", 100000, 12, "SBIT001", "Main");
        assertTrue(fd.calculateMaturityAmount() > 100000);
    }

    @Test
    @DisplayName("Fixed deposit cannot be withdrawn before maturity")
    void testFDPrematureWithdrawal() {
        FixedDepositAccount fd = AccountFactory.createFixedDepositAccount(
                "CUST001", 50000, 24, "SBIT001", "Main");
        assertFalse(fd.canWithdraw(50000), "FD should not allow withdrawal before maturity");
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Transfer credits destination and debits source")
    void testTransfer() throws InsufficientFundsException {
        SavingsAccount destination = AccountFactory.createSavingsAccount("CUST003", 5000, "SBIT001", "Main");
        savingsAccount.addTransferDebit(2000, destination.getAccountId());
        destination.addTransferCredit(2000, savingsAccount.getAccountId());
        assertEquals(8000.0, savingsAccount.getBalance(), 0.01);
        assertEquals(7000.0, destination.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Negative deposit amount throws IllegalArgumentException")
    void testNegativeDeposit() {
        assertThrows(IllegalArgumentException.class,
                () -> savingsAccount.deposit(-500, "Negative deposit"));
    }
}
