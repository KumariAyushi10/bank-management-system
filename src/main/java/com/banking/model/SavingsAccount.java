package com.banking.model;

/**
 * Savings Account – earns interest, enforces minimum balance.
 * Demonstrates Inheritance and method overriding.
 */
public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;

    private double interestRate;  // per annum
    private int withdrawalsThisMonth;
    private static final int MAX_FREE_WITHDRAWALS = 5;

    public SavingsAccount(String accountId, String customerId, double initialDeposit,
                          String ifscCode, String branchName) {
        super(accountId, customerId, AccountType.SAVINGS, initialDeposit, ifscCode, branchName);
        this.interestRate = AccountType.SAVINGS.getBaseInterestRate();
    }

    @Override
    public double calculateInterest() {
        // Quarterly compounding
        return balance * (interestRate / 100.0) / 4.0;
    }

    @Override
    public boolean canWithdraw(double amount) {
        double postBalance = balance - amount;
        return postBalance >= AccountType.SAVINGS.getMinimumBalance();
    }

    @Override
    public String getAccountSummary() {
        return String.format(
            "╔══════════════════════════════════════════╗\n" +
            "║         SAVINGS ACCOUNT SUMMARY          ║\n" +
            "╠══════════════════════════════════════════╣\n" +
            "║ Account ID   : %-26s║\n" +
            "║ Balance      : ₹%-25.2f║\n" +
            "║ Interest Rate: %-25s║\n" +
            "║ Min Balance  : ₹%-25.2f║\n" +
            "║ Status       : %-26s║\n" +
            "╚══════════════════════════════════════════╝",
            accountId, balance, interestRate + "% p.a.",
            AccountType.SAVINGS.getMinimumBalance(), status);
    }

    public void resetMonthlyWithdrawals() { this.withdrawalsThisMonth = 0; }
    public int  getWithdrawalsThisMonth() { return withdrawalsThisMonth; }
    public double getInterestRate()        { return interestRate; }
    public void setInterestRate(double r) { this.interestRate = r; }

    @Override
    public synchronized void withdraw(double amount, String description)
            throws com.banking.exception.InsufficientFundsException {
        super.withdraw(amount, description);
        withdrawalsThisMonth++;
    }
}
