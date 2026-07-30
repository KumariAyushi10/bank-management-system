package com.banking;

import com.banking.concurrent.TransactionProcessor;
import com.banking.exception.BankingException;
import com.banking.model.*;
import com.banking.pattern.singleton.BankDatabase;
import com.banking.pattern.strategy.InterestCalculator;
import com.banking.pattern.strategy.CompoundInterestStrategy;
import com.banking.service.BankingService;
import com.banking.service.BankingServiceImpl;
import com.banking.ui.ConsoleMenu;
import com.banking.util.FileHandler;
import com.banking.util.ReportGenerator;

import java.time.LocalDate;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║        SMART BANKING MANAGEMENT SYSTEM  —  MAIN CLASS       ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Demonstrates:                                               ║
 * ║  • Singleton, Factory, Observer, Strategy design patterns    ║
 * ║  • Multithreading with ExecutorService, AtomicLong           ║
 * ║  • Java Streams, Lambdas, Generics, Optional                 ║
 * ║  • Custom Exception Hierarchy                                ║
 * ║  • File I/O, Serialization, CSV export                       ║
 * ║  • Abstract classes, Interfaces, Inheritance, Polymorphism   ║
 * ║  • Collections: List, Map, Queue, PriorityQueue              ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 *  Run: java -jar SmartBankingSystem.jar
 *
 *  Pass "--demo" as argument for a full automated demonstration.
 *  No argument launches the interactive console menu.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            runAutomatedDemo();
        } else {
            new ConsoleMenu().run();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Automated Demo – shows every feature of the system
    // ─────────────────────────────────────────────────────────────────────────
    private static void runAutomatedDemo() throws Exception {
        BankingService bank = new BankingServiceImpl();
        ReportGenerator reporter = new ReportGenerator();
        TransactionProcessor processor = new TransactionProcessor(4);

        System.out.println("═".repeat(65));
        System.out.println("  SMART BANKING SYSTEM — FULL FEATURE DEMONSTRATION");
        System.out.println("═".repeat(65));

        // ── 1. Register Customers ─────────────────────────────────────────────
        section("1. CUSTOMER REGISTRATION (Builder Pattern)");
        Customer alice = new Customer.Builder("CUST001")
                .name("Alice Sharma").email("alice@email.com").phone("9876543210")
                .address("42 MG Road, Bangalore").dateOfBirth(LocalDate.of(1992, 4, 15))
                .panNumber("ABCDE1234F").aadharNumber("123412341234")
                .creditScore(780).build();

        Customer bob = new Customer.Builder("CUST002")
                .name("Bob Verma").email("bob@email.com").phone("9123456780")
                .address("12 Park St, Mumbai").dateOfBirth(LocalDate.of(1988, 9, 22))
                .panNumber("XYZPQ5678G").aadharNumber("987698769876")
                .creditScore(720).build();

        Customer corp = new Customer.Builder("CORP001")
                .name("Acme Pvt Ltd").email("accounts@acme.com").phone("0441234567")
                .address("100 Industrial Area, Chennai").dateOfBirth(LocalDate.of(1980, 1, 1))
                .creditScore(810).build();

        bank.registerCustomer(alice);
        bank.registerCustomer(bob);
        bank.registerCustomer(corp);

        // ── 2. Open Accounts (Factory Pattern) ───────────────────────────────
        section("2. OPENING ACCOUNTS (Factory Method Pattern)");
        Account aliceSavings  = bank.openSavingsAccount("CUST001", 25000);
        Account aliceFD       = bank.openFixedDeposit("CUST001", 100000, 24);
        Account bobSavings    = bank.openSavingsAccount("CUST002", 10000);
        Account corpCurrent   = bank.openCurrentAccount("CORP001", 500000, 200000);

        String aliceAccId  = aliceSavings.getAccountId();
        String bobAccId    = bobSavings.getAccountId();
        String corpAccId   = corpCurrent.getAccountId();

        // ── 3. Deposit Transactions ───────────────────────────────────────────
        section("3. DEPOSITS (Observer Pattern — notifications + audit trail)");
        bank.deposit(aliceAccId, 15000, "Salary credit - July 2026");
        bank.deposit(aliceAccId, 5000,  "Freelance income");
        bank.deposit(bobAccId,   8000,  "Bonus received");
        bank.deposit(corpAccId,  250000,"Client payment - Invoice #INV-2026-07");

        // ── 4. Withdrawals ────────────────────────────────────────────────────
        section("4. WITHDRAWALS");
        bank.withdraw(aliceAccId, 3000, "Electricity bill");
        bank.withdraw(bobAccId,   2000, "Online shopping");

        // ── 5. Fund Transfer ──────────────────────────────────────────────────
        section("5. FUND TRANSFER (Synchronized Deadlock-Safe)");
        bank.transfer(aliceAccId, bobAccId, 10000);
        bank.transfer(corpAccId, aliceAccId, 20000);

        // ── 6. Interest Application (Template Method Pattern) ─────────────────
        section("6. QUARTERLY INTEREST (Template Method Pattern)");
        bank.applyInterestToAll();

        // ── 7. Loan Management ────────────────────────────────────────────────
        section("7. LOAN MANAGEMENT (EMI Calculation)");
        Loan aliceLoan = bank.applyForLoan("CUST001", aliceAccId,
                Loan.LoanType.PERSONAL, 200000, 36, "Home renovation");
        bank.approveLoan(aliceLoan.getLoanId());

        Loan corpLoan = bank.applyForLoan("CORP001", corpAccId,
                Loan.LoanType.BUSINESS, 5000000, 60, "Factory expansion");
        bank.approveLoan(corpLoan.getLoanId());

        // Pay 3 EMIs
        System.out.println("\nPaying 3 EMIs for Alice's loan...");
        bank.payLoanEMI(aliceLoan.getLoanId());
        bank.payLoanEMI(aliceLoan.getLoanId());
        bank.payLoanEMI(aliceLoan.getLoanId());

        // ── 8. Account Statements ─────────────────────────────────────────────
        section("8. ACCOUNT STATEMENT");
        bank.printAccountStatement(aliceAccId);

        // ── 9. Strategy Pattern – Interest Calculator ─────────────────────────
        section("9. INTEREST STRATEGY COMPARISON (Strategy Pattern)");
        InterestCalculator calc = new InterestCalculator(new CompoundInterestStrategy(4));
        calc.printComparison(100000, 7.5, 12);
        System.out.println(reporter.generateInterestComparisonTable(
                100000, 7.5, new int[]{6, 12, 24, 36, 60}));

        // ── 10. Multithreaded Batch Processing ────────────────────────────────
        section("10. MULTITHREADED TRANSACTION PROCESSING");
        List<Runnable> batch = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            batch.add(() -> {
                try {
                    bank.deposit(bobAccId, 500 * (idx + 1), "Batch deposit #" + (idx + 1));
                } catch (BankingException e) {
                    System.err.println(e.getMessage());
                }
            });
        }
        batch.forEach(task -> processor.submitAndForget(task, "Batch deposit task"));
        Thread.sleep(500); // wait for all async tasks
        processor.printStats();
        processor.shutdown();

        // ── 11. Reports ───────────────────────────────────────────────────────
        section("11. REPORTS & FILE I/O");
        System.out.println(reporter.generatePortfolioReport("CUST001"));
        bank.printBankSummary();

        // Export CSV
        FileHandler.exportTransactionsToCsv(aliceSavings);

        // Export bank summary to file
        FileHandler.writeTextReport(reporter.generateBankSummaryReport(), "bank_summary");

        // ── 12. Singleton Verification ────────────────────────────────────────
        section("12. SINGLETON PATTERN VERIFICATION");
        BankDatabase db1 = BankDatabase.getInstance();
        BankDatabase db2 = BankDatabase.getInstance();
        System.out.println("Same instance? " + (db1 == db2));   // must print: true
        System.out.println(db1);

        // ── Done ──────────────────────────────────────────────────────────────
        System.out.println("\n" + "═".repeat(65));
        System.out.println("  DEMO COMPLETE — All features demonstrated successfully!");
        System.out.println("  Check  reports/   for exported CSV and text reports.");
        System.out.println("  Check  audit_trail.log  for the full audit log.");
        System.out.println("═".repeat(65));
    }

    private static void section(String title) {
        System.out.println("\n" + "─".repeat(65));
        System.out.println("  " + title);
        System.out.println("─".repeat(65));
    }
}
