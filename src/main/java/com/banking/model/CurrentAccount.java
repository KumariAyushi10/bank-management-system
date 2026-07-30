package com.banking.model;

/**
 * Current Account – supports overdraft facility, no interest.
 * Used by businesses for high-frequency transactions.
 */
public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;

    private double overdraftLimit;

    public CurrentAccount(String accountId, String customerId, double initialDeposit,
                          double overdraftLimit, String ifscCode, String branchName) {
        super(accountId, customerId, AccountType.CURRENT, initialDeposit, ifscCode, branchName);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double calculateInterest() {
        return 0.0; // Current accounts do not earn interest
    }

    @Override
    public boolean canWithdraw(double amount) {
        return (balance + overdraftLimit - amount) >= 0;
    }

    @Override
    public String getAccountSummary() {
        return String.format(
            "╔══════════════════════════════════════════╗\n" +
            "║         CURRENT ACCOUNT SUMMARY          ║\n" +
            "╠══════════════════════════════════════════╣\n" +
            "║ Account ID     : %-24s║\n" +
            "║ Balance        : ₹%-23.2f║\n" +
            "║ Overdraft Limit: ₹%-23.2f║\n" +
            "║ Available Limit: ₹%-23.2f║\n" +
            "║ Status         : %-24s║\n" +
            "╚══════════════════════════════════════════╝",
            accountId, balance, overdraftLimit,
            balance + overdraftLimit, status);
    }

    public double getOverdraftLimit()       { return overdraftLimit; }
    public void setOverdraftLimit(double l) { this.overdraftLimit = l; }
    public double getEffectiveBalance()     { return balance + overdraftLimit; }
}
