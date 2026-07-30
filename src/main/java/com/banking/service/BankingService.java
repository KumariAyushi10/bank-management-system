package com.banking.service;

import com.banking.exception.*;
import com.banking.model.*;

import java.util.List;
import java.util.Map;

/**
 * Main service interface defining all banking operations.
 * Demonstrates Interface-based programming and dependency inversion.
 */
public interface BankingService {

    // ── Customer Operations ───────────────────────────────────────────────────
    Customer registerCustomer(Customer customer) throws DuplicateAccountException;
    Customer getCustomer(String customerId) throws AccountNotFoundException;
    void     updateCustomerCreditScore(String customerId, int delta) throws AccountNotFoundException;

    // ── Account Operations ────────────────────────────────────────────────────
    Account openSavingsAccount(String customerId, double initialDeposit) throws BankingException;
    Account openCurrentAccount(String customerId, double initialDeposit, double overdraftLimit) throws BankingException;
    Account openFixedDeposit(String customerId, double amount, int tenureMonths) throws BankingException;
    Account getAccount(String accountId) throws AccountNotFoundException;
    void    closeAccount(String accountId) throws BankingException;
    List<Account> getCustomerAccounts(String customerId) throws AccountNotFoundException;

    // ── Transaction Operations ────────────────────────────────────────────────
    void deposit(String accountId, double amount, String description) throws BankingException;
    void withdraw(String accountId, double amount, String description) throws BankingException;
    void transfer(String fromAccountId, String toAccountId, double amount) throws BankingException;
    void applyInterestToAll();

    // ── Loan Operations ───────────────────────────────────────────────────────
    Loan applyForLoan(String customerId, String accountId, Loan.LoanType type,
                      double amount, int tenureMonths, String purpose) throws BankingException;
    void approveLoan(String loanId) throws BankingException;
    void rejectLoan(String loanId) throws BankingException;
    void payLoanEMI(String loanId) throws BankingException;
    Loan getLoan(String loanId) throws BankingException;

    // ── Reporting ─────────────────────────────────────────────────────────────
    void printAccountStatement(String accountId) throws AccountNotFoundException;
    void printBankSummary();
    Map<String, Double> getPortfolioSummary(String customerId) throws AccountNotFoundException;
}
