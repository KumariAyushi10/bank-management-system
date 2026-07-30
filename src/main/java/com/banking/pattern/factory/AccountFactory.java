package com.banking.pattern.factory;

import com.banking.model.*;

import java.util.UUID;

/**
 * Factory for creating different Account types without exposing constructors.
 *
 * Design Pattern: Factory Method
 */
public class AccountFactory {

    private AccountFactory() {} // Utility class – no instantiation

    private static String generateAccountId(AccountType type) {
        String prefix = switch (type) {
            case SAVINGS          -> "SAV";
            case CURRENT          -> "CUR";
            case FIXED_DEPOSIT    -> "FD";
            case RECURRING_DEPOSIT-> "RD";
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Create a Savings Account.
     */
    public static SavingsAccount createSavingsAccount(String customerId, double initialDeposit,
                                                       String ifscCode, String branchName) {
        validateInitialDeposit(AccountType.SAVINGS, initialDeposit);
        String id = generateAccountId(AccountType.SAVINGS);
        return new SavingsAccount(id, customerId, initialDeposit, ifscCode, branchName);
    }

    /**
     * Create a Current Account with optional overdraft.
     */
    public static CurrentAccount createCurrentAccount(String customerId, double initialDeposit,
                                                       double overdraftLimit,
                                                       String ifscCode, String branchName) {
        validateInitialDeposit(AccountType.CURRENT, initialDeposit);
        String id = generateAccountId(AccountType.CURRENT);
        return new CurrentAccount(id, customerId, initialDeposit, overdraftLimit, ifscCode, branchName);
    }

    /**
     * Create a Fixed Deposit Account.
     */
    public static FixedDepositAccount createFixedDepositAccount(String customerId, double depositAmount,
                                                                  int tenureMonths,
                                                                  String ifscCode, String branchName) {
        validateInitialDeposit(AccountType.FIXED_DEPOSIT, depositAmount);
        if (tenureMonths < 1 || tenureMonths > 120)
            throw new IllegalArgumentException("FD tenure must be between 1 and 120 months.");
        String id = generateAccountId(AccountType.FIXED_DEPOSIT);
        return new FixedDepositAccount(id, customerId, depositAmount, tenureMonths, ifscCode, branchName);
    }

    private static void validateInitialDeposit(AccountType type, double amount) {
        if (amount < type.getMinimumBalance())
            throw new IllegalArgumentException(
                String.format("Minimum opening balance for %s is ₹%.2f",
                              type.getDisplayName(), type.getMinimumBalance()));
    }
}
