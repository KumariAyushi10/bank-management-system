package com.banking.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a bank loan with EMI calculation using standard formula.
 * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 */
public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum LoanType   { HOME, PERSONAL, VEHICLE, EDUCATION, BUSINESS }
    public enum LoanStatus { PENDING, APPROVED, REJECTED, ACTIVE, CLOSED, DEFAULTED }

    private final String loanId;
    private final String customerId;
    private final String accountId;
    private final LoanType loanType;
    private final double principalAmount;
    private final double interestRate;   // per annum
    private final int tenureMonths;
    private final double emi;
    private final LocalDate startDate;
    private LocalDate endDate;
    private LoanStatus status;
    private double outstandingBalance;
    private int emiPaid;
    private final String purpose;

    public Loan(String customerId, String accountId, LoanType loanType,
                double principalAmount, double interestRate, int tenureMonths, String purpose) {
        this.loanId             = "LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerId         = customerId;
        this.accountId          = accountId;
        this.loanType           = loanType;
        this.principalAmount    = principalAmount;
        this.interestRate       = interestRate;
        this.tenureMonths       = tenureMonths;
        this.purpose            = purpose;
        this.startDate          = LocalDate.now();
        this.status             = LoanStatus.PENDING;
        this.outstandingBalance = principalAmount;
        this.emiPaid            = 0;
        this.emi                = calculateEMI(principalAmount, interestRate, tenureMonths);
    }

    /** Standard reducing-balance EMI formula. */
    public static double calculateEMI(double principal, double annualRate, int months) {
        if (annualRate == 0) return principal / months;
        double r = annualRate / (12.0 * 100.0);
        double power = Math.pow(1 + r, months);
        return principal * r * power / (power - 1);
    }

    /** Total amount payable over the loan tenure. */
    public double getTotalPayable() { return emi * tenureMonths; }

    /** Total interest component. */
    public double getTotalInterest() { return getTotalPayable() - principalAmount; }

    /** Apply an EMI payment and return true if loan is now fully paid. */
    public synchronized boolean payEMI() {
        if (status != LoanStatus.ACTIVE)
            throw new IllegalStateException("Loan is not active.");
        outstandingBalance = Math.max(0, outstandingBalance - emi);
        emiPaid++;
        if (emiPaid >= tenureMonths || outstandingBalance <= 0.01) {
            status  = LoanStatus.CLOSED;
            endDate = LocalDate.now();
            return true;
        }
        return false;
    }

    public void approve() {
        if (status != LoanStatus.PENDING)
            throw new IllegalStateException("Only PENDING loans can be approved.");
        status = LoanStatus.ACTIVE;
    }

    public void reject() {
        if (status != LoanStatus.PENDING)
            throw new IllegalStateException("Only PENDING loans can be rejected.");
        status = LoanStatus.REJECTED;
    }

    // Getters
    public String     getLoanId()            { return loanId;            }
    public String     getCustomerId()        { return customerId;        }
    public String     getAccountId()         { return accountId;         }
    public LoanType   getLoanType()          { return loanType;          }
    public double     getPrincipalAmount()   { return principalAmount;   }
    public double     getInterestRate()      { return interestRate;      }
    public int        getTenureMonths()      { return tenureMonths;      }
    public double     getEmi()               { return emi;               }
    public LocalDate  getStartDate()         { return startDate;         }
    public LocalDate  getEndDate()           { return endDate;           }
    public LoanStatus getStatus()            { return status;            }
    public double     getOutstandingBalance(){ return outstandingBalance; }
    public int        getEmiPaid()           { return emiPaid;           }
    public String     getPurpose()           { return purpose;           }
    public int        getEmiRemaining()      { return tenureMonths - emiPaid; }

    @Override
    public String toString() {
        return String.format(
            "Loan{id=%s, type=%s, principal=%.2f, rate=%.1f%%, EMI=%.2f, outstanding=%.2f, status=%s}",
            loanId, loanType, principalAmount, interestRate, emi, outstandingBalance, status);
    }
}
