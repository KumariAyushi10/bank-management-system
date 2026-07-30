package com.banking.model;

/**
 * Enum representing account types supported by the system.
 */
public enum AccountType {
    SAVINGS("Savings Account", 500.0, 4.0),
    CURRENT("Current Account", 5000.0, 0.0),
    FIXED_DEPOSIT("Fixed Deposit Account", 1000.0, 7.5),
    RECURRING_DEPOSIT("Recurring Deposit Account", 500.0, 6.5);

    private final String displayName;
    private final double minimumBalance;
    private final double baseInterestRate;   // per annum %

    AccountType(String displayName, double minimumBalance, double baseInterestRate) {
        this.displayName      = displayName;
        this.minimumBalance   = minimumBalance;
        this.baseInterestRate = baseInterestRate;
    }

    public String getDisplayName()      { return displayName;      }
    public double getMinimumBalance()   { return minimumBalance;   }
    public double getBaseInterestRate() { return baseInterestRate; }
}
