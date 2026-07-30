package com.banking.service;

import com.banking.exception.*;
import com.banking.model.*;
import com.banking.pattern.factory.AccountFactory;
import com.banking.pattern.observer.*;
import com.banking.pattern.singleton.BankDatabase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Full implementation of BankingService.
 * Demonstrates: Dependency Injection, Observer pattern notification,
 *               Streams & Lambdas, Optional, and comprehensive exception handling.
 */
public class BankingServiceImpl implements BankingService {

    private static final String DEFAULT_IFSC   = "SBIT0001234";
    private static final String DEFAULT_BRANCH = "Main Branch";

    private final BankDatabase db;
    private final List<TransactionObserver> observers;

    public BankingServiceImpl() {
        this.db        = BankDatabase.getInstance();
        this.observers = new ArrayList<>();
        // Register default observers
        registerObserver(new AuditLogObserver());
        registerObserver(new FraudDetectionObserver());
        registerObserver(new NotificationObserver());
    }

    public void registerObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(List<Transaction> transactions) {
        for (Transaction txn : transactions) {
            observers.forEach(obs -> obs.onTransaction(txn));
        }
    }

    // ── Customer Operations ───────────────────────────────────────────────────

    @Override
    public Customer registerCustomer(Customer customer) throws DuplicateAccountException {
        if (db.customerExists(customer.getCustomerId()))
            throw new DuplicateAccountException(customer.getCustomerId());
        db.saveCustomer(customer);
        System.out.printf("✔ Customer registered: %s (ID: %s)%n",
                customer.getName(), customer.getCustomerId());
        return customer;
    }

    @Override
    public Customer getCustomer(String customerId) throws AccountNotFoundException {
        return Optional.ofNullable(db.findCustomer(customerId))
                .orElseThrow(() -> new AccountNotFoundException(customerId));
    }

    @Override
    public void updateCustomerCreditScore(String customerId, int delta) throws AccountNotFoundException {
        Customer c = getCustomer(customerId);
        c.updateCreditScore(delta);
        System.out.printf("Credit score updated for %s → %d%n", c.getName(), c.getCreditScore());
    }

    // ── Account Operations ────────────────────────────────────────────────────

    @Override
    public Account openSavingsAccount(String customerId, double initialDeposit) throws BankingException {
        Customer customer = getCustomer(customerId);
        SavingsAccount account = AccountFactory.createSavingsAccount(
                customerId, initialDeposit, DEFAULT_IFSC, DEFAULT_BRANCH);
        db.saveAccount(account);
        customer.linkAccount(account.getAccountId());
        System.out.printf("✔ Savings Account opened: %s | Balance: ₹%.2f%n",
                account.getAccountId(), account.getBalance());
        return account;
    }

    @Override
    public Account openCurrentAccount(String customerId, double initialDeposit,
                                      double overdraftLimit) throws BankingException {
        Customer customer = getCustomer(customerId);
        CurrentAccount account = AccountFactory.createCurrentAccount(
                customerId, initialDeposit, overdraftLimit, DEFAULT_IFSC, DEFAULT_BRANCH);
        db.saveAccount(account);
        customer.linkAccount(account.getAccountId());
        System.out.printf("✔ Current Account opened: %s | Balance: ₹%.2f | OD: ₹%.2f%n",
                account.getAccountId(), account.getBalance(), overdraftLimit);
        return account;
    }

    @Override
    public Account openFixedDeposit(String customerId, double amount, int tenureMonths)
            throws BankingException {
        Customer customer = getCustomer(customerId);
        FixedDepositAccount fd = AccountFactory.createFixedDepositAccount(
                customerId, amount, tenureMonths, DEFAULT_IFSC, DEFAULT_BRANCH);
        db.saveAccount(fd);
        customer.linkAccount(fd.getAccountId());
        System.out.printf("✔ FD opened: %s | ₹%.2f | %d months | Maturity: ₹%.2f%n",
                fd.getAccountId(), amount, tenureMonths, fd.calculateMaturityAmount());
        return fd;
    }

    @Override
    public Account getAccount(String accountId) throws AccountNotFoundException {
        return Optional.ofNullable(db.findAccount(accountId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    public void closeAccount(String accountId) throws BankingException {
        Account account = getAccount(accountId);
        if (account.getBalance() > 0)
            throw new BankingException("Cannot close account with non-zero balance. Please withdraw ₹"
                    + account.getBalance() + " first.", "ERR_CLOSE_NONZERO");
        account.setStatus(Account.AccountStatus.CLOSED);
        System.out.printf("Account %s closed.%n", accountId);
    }

    @Override
    public List<Account> getCustomerAccounts(String customerId) throws AccountNotFoundException {
        Customer customer = getCustomer(customerId);
        return customer.getLinkedAccountIds().stream()
                .map(db::findAccount)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    // ── Transaction Operations ────────────────────────────────────────────────

    @Override
    public void deposit(String accountId, double amount, String description) throws BankingException {
        Account account = getAccount(accountId);
        account.deposit(amount, description);
        List<Transaction> latest = account.getTransactions();
        notifyObservers(List.of(latest.get(latest.size() - 1)));
        System.out.printf("  DEPOSIT  ₹%8.2f → %s | Balance: ₹%.2f%n",
                amount, accountId, account.getBalance());
    }

    @Override
    public void withdraw(String accountId, double amount, String description) throws BankingException {
        Account account = getAccount(accountId);
        account.withdraw(amount, description);
        List<Transaction> txns = account.getTransactions();
        notifyObservers(List.of(txns.get(txns.size() - 1)));
        System.out.printf("  WITHDRAW ₹%8.2f ← %s | Balance: ₹%.2f%n",
                amount, accountId, account.getBalance());
    }

    @Override
    public void transfer(String fromId, String toId, double amount) throws BankingException {
        Account from = getAccount(fromId);
        Account to   = getAccount(toId);

        // Acquire locks in consistent order to prevent deadlock
        Object first  = fromId.compareTo(toId) < 0 ? from : to;
        Object second = fromId.compareTo(toId) < 0 ? to   : from;

        synchronized (first) {
            synchronized (second) {
                from.addTransferDebit(amount, toId);
                to.addTransferCredit(amount, fromId);
            }
        }

        List<Transaction> fromTxns = from.getTransactions();
        List<Transaction> toTxns   = to.getTransactions();
        notifyObservers(List.of(
                fromTxns.get(fromTxns.size() - 1),
                toTxns.get(toTxns.size() - 1)
        ));

        System.out.printf("  TRANSFER ₹%8.2f : %s → %s%n", amount, fromId, toId);
    }

    @Override
    public void applyInterestToAll() {
        db.getAllAccounts().values().stream()
                .filter(a -> a.getStatus() == Account.AccountStatus.ACTIVE)
                .forEach(a -> {
                    a.applyInterest();
                    List<Transaction> txns = a.getTransactions();
                    if (!txns.isEmpty() &&
                        txns.get(txns.size() - 1).getType() == TransactionType.INTEREST_CREDIT) {
                        notifyObservers(List.of(txns.get(txns.size() - 1)));
                    }
                });
        System.out.println("✔ Quarterly interest applied to all eligible accounts.");
    }

    // ── Loan Operations ───────────────────────────────────────────────────────

    @Override
    public Loan applyForLoan(String customerId, String accountId, Loan.LoanType type,
                             double amount, int tenureMonths, String purpose) throws BankingException {
        Customer customer = getCustomer(customerId);
        if (!customer.isLoanEligible())
            throw new LoanException(
                    "Customer is not eligible: credit score=" + customer.getCreditScore()
                    + ", age=" + customer.getAge(), "ERR_LOAN_INELIGIBLE");

        double rate = resolveLoanRate(type, customer.getCreditScore());
        Loan loan = new Loan(customerId, accountId, type, amount, rate, tenureMonths, purpose);
        db.saveLoan(loan);
        System.out.printf("✔ Loan application submitted: %s | ₹%.2f | EMI: ₹%.2f/month%n",
                loan.getLoanId(), amount, loan.getEmi());
        return loan;
    }

    private double resolveLoanRate(Loan.LoanType type, int creditScore) {
        double base = switch (type) {
            case HOME      -> 8.5;
            case PERSONAL  -> 12.0;
            case VEHICLE   -> 9.5;
            case EDUCATION -> 10.5;
            case BUSINESS  -> 11.0;
        };
        // Better credit score = lower rate
        if (creditScore >= 800) base -= 1.0;
        else if (creditScore >= 750) base -= 0.5;
        else if (creditScore < 650)  base += 1.0;
        return base;
    }

    @Override
    public void approveLoan(String loanId) throws BankingException {
        Loan loan = getLoan(loanId);
        loan.approve();
        // Disburse amount to linked account
        Account account = getAccount(loan.getAccountId());
        account.deposit(loan.getPrincipalAmount(), "Loan disbursement: " + loanId);
        System.out.printf("✔ Loan %s APPROVED. ₹%.2f disbursed to account %s%n",
                loanId, loan.getPrincipalAmount(), loan.getAccountId());
    }

    @Override
    public void rejectLoan(String loanId) throws BankingException {
        getLoan(loanId).reject();
        System.out.printf("✘ Loan %s REJECTED.%n", loanId);
    }

    @Override
    public void payLoanEMI(String loanId) throws BankingException {
        Loan loan = getLoan(loanId);
        Account account = getAccount(loan.getAccountId());
        account.withdraw(loan.getEmi(), "EMI payment: " + loanId);
        boolean closed = loan.payEMI();
        System.out.printf("  EMI ₹%.2f paid for loan %s. Outstanding: ₹%.2f%s%n",
                loan.getEmi(), loanId, loan.getOutstandingBalance(),
                closed ? " [LOAN CLOSED]" : "");
    }

    @Override
    public Loan getLoan(String loanId) throws BankingException {
        return Optional.ofNullable(db.findLoan(loanId))
                .orElseThrow(() -> new BankingException("Loan not found: " + loanId, "ERR_LOAN_NOT_FOUND"));
    }

    // ── Reporting ─────────────────────────────────────────────────────────────

    @Override
    public void printAccountStatement(String accountId) throws AccountNotFoundException {
        Account account = getAccount(accountId);
        System.out.println(account.getAccountSummary());
        System.out.println("\nTransaction History (last 20):");
        System.out.println("─".repeat(90));
        System.out.printf("| %-18s | %-16s | %-11s | %-12s | %-30s |%n",
                "Transaction ID", "Date & Time", "Amount", "Type", "Description");
        System.out.println("─".repeat(90));
        account.getTransactions().stream()
                .sorted()
                .limit(20)
                .forEach(System.out::println);
        System.out.println("─".repeat(90));
    }

    @Override
    public void printBankSummary() {
        System.out.println("\n" + "═".repeat(55));
        System.out.println("         SMART BANK — SYSTEM SUMMARY");
        System.out.println("═".repeat(55));
        System.out.printf("  Total Customers : %d%n", db.getCustomerCount());
        System.out.printf("  Total Accounts  : %d%n", db.getAccountCount());
        System.out.printf("  Total Loans     : %d%n", db.getLoanCount());

        double totalDeposits = db.getAllAccounts().values().stream()
                .mapToDouble(Account::getBalance).sum();
        double totalLoans = db.getAllLoans().values().stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                .mapToDouble(Loan::getOutstandingBalance).sum();

        System.out.printf("  Total Deposits  : ₹%.2f%n", totalDeposits);
        System.out.printf("  Active Loan Book: ₹%.2f%n", totalLoans);
        System.out.println("═".repeat(55));
    }

    @Override
    public Map<String, Double> getPortfolioSummary(String customerId) throws AccountNotFoundException {
        List<Account> accounts = getCustomerAccounts(customerId);
        Map<String, Double> portfolio = new LinkedHashMap<>();
        double total = 0;
        for (Account a : accounts) {
            portfolio.put(a.getAccountId() + " (" + a.getAccountType().getDisplayName() + ")",
                    a.getBalance());
            total += a.getBalance();
        }
        portfolio.put("TOTAL NET WORTH", total);
        return portfolio;
    }
}
