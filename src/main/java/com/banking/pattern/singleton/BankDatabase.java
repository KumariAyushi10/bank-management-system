package com.banking.pattern.singleton;

import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.Loan;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory database for the banking system.
 * Thread-safe: uses double-checked locking + ConcurrentHashMap.
 *
 * Design Pattern: Singleton
 */
public final class BankDatabase {

    private static volatile BankDatabase instance;

    private final Map<String, Customer> customers;
    private final Map<String, Account>  accounts;
    private final Map<String, Loan>     loans;
    private long transactionCounter;

    private BankDatabase() {
        customers          = new ConcurrentHashMap<>();
        accounts           = new ConcurrentHashMap<>();
        loans              = new ConcurrentHashMap<>();
        transactionCounter = 1000L;
    }

    /** Double-checked locking for thread-safe lazy initialization. */
    public static BankDatabase getInstance() {
        if (instance == null) {
            synchronized (BankDatabase.class) {
                if (instance == null) {
                    instance = new BankDatabase();
                }
            }
        }
        return instance;
    }

    // ── Customer CRUD ────────────────────────────────────────────────────────
    public void saveCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer findCustomer(String customerId) {
        return customers.get(customerId);
    }

    public boolean customerExists(String customerId) {
        return customers.containsKey(customerId);
    }

    public Map<String, Customer> getAllCustomers() { return customers; }

    // ── Account CRUD ─────────────────────────────────────────────────────────
    public void saveAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Account findAccount(String accountId) {
        return accounts.get(accountId);
    }

    public boolean accountExists(String accountId) {
        return accounts.containsKey(accountId);
    }

    public Map<String, Account> getAllAccounts() { return accounts; }

    // ── Loan CRUD ────────────────────────────────────────────────────────────
    public void saveLoan(Loan loan) {
        loans.put(loan.getLoanId(), loan);
    }

    public Loan findLoan(String loanId) {
        return loans.get(loanId);
    }

    public Map<String, Loan> getAllLoans() { return loans; }

    // ── Counter ───────────────────────────────────────────────────────────────
    public synchronized long nextTransactionNumber() {
        return ++transactionCounter;
    }

    public int getCustomerCount() { return customers.size(); }
    public int getAccountCount()  { return accounts.size();  }
    public int getLoanCount()     { return loans.size();     }

    @Override
    public String toString() {
        return String.format("BankDatabase{customers=%d, accounts=%d, loans=%d}",
                customers.size(), accounts.size(), loans.size());
    }
}
