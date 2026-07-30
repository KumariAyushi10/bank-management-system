package com.banking.exception;

/**
 * Thrown when attempting to create an account that already exists.
 */
public class DuplicateAccountException extends BankingException {
    private final String accountId;

    public DuplicateAccountException(String accountId) {
        super("Account already exists: " + accountId, "ERR_DUPLICATE_ACCOUNT");
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
