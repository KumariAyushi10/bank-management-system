package com.banking.exception;

/**
 * Thrown when a requested account does not exist in the system.
 */
public class AccountNotFoundException extends BankingException {
    private final String accountId;

    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId, "ERR_ACCOUNT_NOT_FOUND");
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
