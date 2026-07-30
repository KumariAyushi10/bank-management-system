package com.banking.exception;

/**
 * Thrown for loan-related errors (eligibility, repayment, etc.).
 */
public class LoanException extends BankingException {
    public LoanException(String message) {
        super(message, "ERR_LOAN");
    }

    public LoanException(String message, String errorCode) {
        super(message, errorCode);
    }
}
