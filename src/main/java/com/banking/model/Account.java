package com.banking.model;

import com.banking.exception.InsufficientFundsException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for all account types.
 * Demonstrates Abstraction, Encapsulation, and Template Method pattern.
 */
public abstract class Account implements Serializable, Comparable<Account> {
    private static final long serialVersionUID = 1L;

    protected final String accountId;
    protected final String customerId;
    protected final AccountType accountType;
    protected double balance;
    protected final LocalDate openDate;
    protected AccountStatus status;
    protected final List<Transaction> transactions;
    protected String ifscCode;
    protected String branchName;

    public enum AccountStatus { ACTIVE, FROZEN, CLOSED, DORMANT }

    protected Account(String accountId, String customerId, AccountType accountType,
                      double initialDeposit, String ifscCode, String branchName) {
        this.accountId    = accountId;
        this.customerId   = customerId;
        this.accountType  = accountType;
        this.balance      = initialDeposit;
        this.openDate     = LocalDate.now();
        this.status       = AccountStatus.ACTIVE;
        this.transactions = new ArrayList<>();
        this.ifscCode     = ifscCode;
        this.branchName   = branchName;
    }

    // ── Abstract methods (Template Method pattern) ────────────────────────────
    public abstract double calculateInterest();
    public abstract boolean canWithdraw(double amount);
    public abstract String getAccountSummary();

    // ── Core operations ───────────────────────────────────────────────────────
    public synchronized void deposit(double amount, String description) {
        validateActive();
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        Transaction txn = new Transaction(accountId, TransactionType.DEPOSIT, amount, balance, description, null);
        transactions.add(txn);
    }

    public synchronized void withdraw(double amount, String description)
            throws InsufficientFundsException {
        validateActive();
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (!canWithdraw(amount))
            throw new InsufficientFundsException(amount, balance);
        balance -= amount;
        Transaction txn = new Transaction(accountId, TransactionType.WITHDRAWAL, amount, balance, description, null);
        transactions.add(txn);
    }

    public synchronized void addTransferCredit(double amount, String fromAccount) {
        validateActive();
        balance += amount;
        transactions.add(new Transaction(accountId, TransactionType.TRANSFER_IN, amount, balance,
                "Transfer from " + fromAccount, fromAccount));
    }

    public synchronized void addTransferDebit(double amount, String toAccount)
            throws InsufficientFundsException {
        validateActive();
        if (!canWithdraw(amount))
            throw new InsufficientFundsException(amount, balance);
        balance -= amount;
        transactions.add(new Transaction(accountId, TransactionType.TRANSFER_OUT, amount, balance,
                "Transfer to " + toAccount, toAccount));
    }

    public synchronized void applyInterest() {
        double interest = calculateInterest();
        if (interest > 0) {
            balance += interest;
            transactions.add(new Transaction(accountId, TransactionType.INTEREST_CREDIT,
                    interest, balance, "Quarterly interest applied", null));
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────
    protected void validateActive() {
        if (status != AccountStatus.ACTIVE)
            throw new IllegalStateException("Account " + accountId + " is " + status + ". Operations not permitted.");
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String        getAccountId()   { return accountId;   }
    public String        getCustomerId()  { return customerId;  }
    public AccountType   getAccountType() { return accountType; }
    public double        getBalance()     { return balance;     }
    public LocalDate     getOpenDate()    { return openDate;    }
    public AccountStatus getStatus()      { return status;      }
    public String        getIfscCode()    { return ifscCode;    }
    public String        getBranchName()  { return branchName;  }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void setStatus(AccountStatus status) { this.status = status; }

    @Override
    public int compareTo(Account other) {
        return this.accountId.compareTo(other.accountId);
    }

    @Override
    public String toString() {
        return String.format("Account{id=%s, type=%s, balance=%.2f, status=%s}",
                accountId, accountType.getDisplayName(), balance, status);
    }
}
