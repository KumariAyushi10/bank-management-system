package com.banking.model;

/**
 * Enum representing all possible transaction types in the banking system.
 */
public enum TransactionType {
    DEPOSIT("Credit", "+"),
    WITHDRAWAL("Debit", "-"),
    TRANSFER_IN("Transfer In", "+"),
    TRANSFER_OUT("Transfer Out", "-"),
    INTEREST_CREDIT("Interest Credit", "+"),
    LOAN_DISBURSEMENT("Loan Disbursement", "+"),
    LOAN_REPAYMENT("Loan Repayment", "-"),
    PENALTY("Penalty", "-");

    private final String displayName;
    private final String sign;

    TransactionType(String displayName, String sign) {
        this.displayName = displayName;
        this.sign        = sign;
    }

    public String getDisplayName() { return displayName; }
    public String getSign()        { return sign;        }

    public boolean isCredit() { return "+".equals(sign); }
    public boolean isDebit()  { return "-".equals(sign); }
}
