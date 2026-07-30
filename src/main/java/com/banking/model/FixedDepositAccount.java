package com.banking.model;

import com.banking.exception.InsufficientFundsException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Fixed Deposit Account – locked for a tenure, earns higher interest.
 * Demonstrates penalty calculation for premature withdrawal.
 */
public class FixedDepositAccount extends Account {
    private static final long serialVersionUID = 1L;

    private final double interestRate;     // per annum
    private final int tenureMonths;
    private final LocalDate maturityDate;
    private final double principalAmount;
    private boolean matured;

    public FixedDepositAccount(String accountId, String customerId, double depositAmount,
                               int tenureMonths, String ifscCode, String branchName) {
        super(accountId, customerId, AccountType.FIXED_DEPOSIT, depositAmount, ifscCode, branchName);
        this.principalAmount = depositAmount;
        this.tenureMonths    = tenureMonths;
        this.maturityDate    = LocalDate.now().plusMonths(tenureMonths);
        this.matured         = false;
        // Interest rate scales with tenure
        this.interestRate    = resolveRate(tenureMonths);
    }

    private static double resolveRate(int months) {
        if (months <= 3)  return 5.5;
        if (months <= 6)  return 6.0;
        if (months <= 12) return 7.0;
        if (months <= 24) return 7.5;
        return 8.0;   // > 24 months
    }

    @Override
    public double calculateInterest() {
        // Simple interest for entire tenure
        return principalAmount * (interestRate / 100.0) * (tenureMonths / 12.0);
    }

    public double calculateMaturityAmount() {
        return principalAmount + calculateInterest();
    }

    @Override
    public boolean canWithdraw(double amount) {
        return LocalDate.now().isAfter(maturityDate) || matured;
    }

    /** Premature withdrawal with 1% penalty on earned interest. */
    public double getPrematureWithdrawalAmount() {
        long daysElapsed = ChronoUnit.DAYS.between(openDate, LocalDate.now());
        double earnedInterest = principalAmount * (interestRate / 100.0) * (daysElapsed / 365.0);
        double penalty = earnedInterest * 0.01;
        return principalAmount + earnedInterest - penalty;
    }

    public void markMatured() { this.matured = true; }

    @Override
    public String getAccountSummary() {
        return String.format(
            "╔══════════════════════════════════════════╗\n" +
            "║       FIXED DEPOSIT ACCOUNT SUMMARY      ║\n" +
            "╠══════════════════════════════════════════╣\n" +
            "║ Account ID     : %-24s║\n" +
            "║ Principal      : ₹%-23.2f║\n" +
            "║ Interest Rate  : %-24s║\n" +
            "║ Tenure         : %-24s║\n" +
            "║ Maturity Date  : %-24s║\n" +
            "║ Maturity Amount: ₹%-23.2f║\n" +
            "║ Status         : %-24s║\n" +
            "╚══════════════════════════════════════════╝",
            accountId, principalAmount, interestRate + "% p.a.",
            tenureMonths + " months", maturityDate.toString(),
            calculateMaturityAmount(), status);
    }

    public double   getInterestRate()   { return interestRate;  }
    public int      getTenureMonths()   { return tenureMonths;  }
    public LocalDate getMaturityDate()  { return maturityDate;  }
    public double   getPrincipalAmount(){ return principalAmount;}
    public boolean  isMatured()         { return matured || LocalDate.now().isAfter(maturityDate); }
}
