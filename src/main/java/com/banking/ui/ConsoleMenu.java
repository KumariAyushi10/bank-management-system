package com.banking.ui;

import com.banking.exception.BankingException;
import com.banking.model.*;
import com.banking.service.BankingService;
import com.banking.service.BankingServiceImpl;
import com.banking.util.FileHandler;
import com.banking.util.ReportGenerator;

import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive Console Menu for the Smart Banking System.
 * Provides a full-featured text UI demonstrating all system capabilities.
 */
public class ConsoleMenu {

    private final BankingService service = new BankingServiceImpl();
    private final ReportGenerator reportGen = new ReportGenerator();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1  -> customerMenu();
                case 2  -> accountMenu();
                case 3  -> transactionMenu();
                case 4  -> loanMenu();
                case 5  -> reportMenu();
                case 6  -> { service.printBankSummary(); }
                case 0  -> { running = false; System.out.println("Thank you for using Smart Bank. Goodbye!"); }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ── Menus ─────────────────────────────────────────────────────────────────

    private void customerMenu() {
        System.out.println("\n── Customer Management ──");
        System.out.println("1. Register New Customer");
        System.out.println("2. View Customer Details");
        int c = readInt("Choice: ");
        try {
            if (c == 1) {
                System.out.print("Customer ID (e.g. CUST001): "); String id = scanner.nextLine().trim();
                System.out.print("Full Name: ");               String name = scanner.nextLine().trim();
                System.out.print("Email: ");                   String email = scanner.nextLine().trim();
                System.out.print("Phone: ");                   String phone = scanner.nextLine().trim();
                System.out.print("Address: ");                 String addr = scanner.nextLine().trim();
                System.out.print("DOB (yyyy-MM-dd): ");        LocalDate dob = LocalDate.parse(scanner.nextLine().trim());
                Customer customer = new Customer.Builder(id)
                        .name(name).email(email).phone(phone)
                        .address(addr).dateOfBirth(dob).build();
                service.registerCustomer(customer);
            } else if (c == 2) {
                System.out.print("Customer ID: "); String id = scanner.nextLine().trim();
                Customer cust = service.getCustomer(id);
                System.out.println(cust);
                Map<String, Double> portfolio = service.getPortfolioSummary(id);
                System.out.println("\nPortfolio:");
                portfolio.forEach((k, v) -> System.out.printf("  %-50s : ₹%.2f%n", k, v));
            }
        } catch (BankingException e) {
            System.out.println("Error: " + e);
        }
    }

    private void accountMenu() {
        System.out.println("\n── Account Management ──");
        System.out.println("1. Open Savings Account");
        System.out.println("2. Open Current Account");
        System.out.println("3. Open Fixed Deposit");
        System.out.println("4. View Account Summary");
        System.out.println("5. Close Account");
        int c = readInt("Choice: ");
        try {
            System.out.print("Customer ID: "); String custId = scanner.nextLine().trim();
            switch (c) {
                case 1 -> {
                    double dep = readDouble("Initial Deposit (min ₹500): ");
                    service.openSavingsAccount(custId, dep);
                }
                case 2 -> {
                    double dep = readDouble("Initial Deposit (min ₹5000): ");
                    double od  = readDouble("Overdraft Limit: ");
                    service.openCurrentAccount(custId, dep, od);
                }
                case 3 -> {
                    double amt    = readDouble("FD Amount: ");
                    int    tenure = readInt("Tenure (months): ");
                    service.openFixedDeposit(custId, amt, tenure);
                }
                case 4 -> {
                    System.out.print("Account ID: "); String accId = scanner.nextLine().trim();
                    service.printAccountStatement(accId);
                }
                case 5 -> {
                    System.out.print("Account ID: "); String accId = scanner.nextLine().trim();
                    service.closeAccount(accId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transactionMenu() {
        System.out.println("\n── Transactions ──");
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. Fund Transfer");
        System.out.println("4. Apply Quarterly Interest");
        int c = readInt("Choice: ");
        try {
            switch (c) {
                case 1 -> {
                    System.out.print("Account ID: "); String accId = scanner.nextLine().trim();
                    double amt = readDouble("Amount: ");
                    System.out.print("Description: "); String desc = scanner.nextLine().trim();
                    service.deposit(accId, amt, desc);
                }
                case 2 -> {
                    System.out.print("Account ID: "); String accId = scanner.nextLine().trim();
                    double amt = readDouble("Amount: ");
                    System.out.print("Description: "); String desc = scanner.nextLine().trim();
                    service.withdraw(accId, amt, desc);
                }
                case 3 -> {
                    System.out.print("From Account ID: "); String from = scanner.nextLine().trim();
                    System.out.print("To Account ID:   "); String to   = scanner.nextLine().trim();
                    double amt = readDouble("Amount: ");
                    service.transfer(from, to, amt);
                }
                case 4 -> service.applyInterestToAll();
            }
        } catch (BankingException e) {
            System.out.println("Error: " + e);
        }
    }

    private void loanMenu() {
        System.out.println("\n── Loan Management ──");
        System.out.println("1. Apply for Loan");
        System.out.println("2. Approve Loan");
        System.out.println("3. Reject Loan");
        System.out.println("4. Pay EMI");
        System.out.println("5. View Loan Details");
        int c = readInt("Choice: ");
        try {
            switch (c) {
                case 1 -> {
                    System.out.print("Customer ID: ");   String custId = scanner.nextLine().trim();
                    System.out.print("Account ID: ");    String accId  = scanner.nextLine().trim();
                    System.out.print("Loan Type (HOME/PERSONAL/VEHICLE/EDUCATION/BUSINESS): ");
                    Loan.LoanType type = Loan.LoanType.valueOf(scanner.nextLine().trim().toUpperCase());
                    double amt    = readDouble("Principal Amount: ");
                    int    tenure = readInt("Tenure (months): ");
                    System.out.print("Purpose: "); String purpose = scanner.nextLine().trim();
                    Loan loan = service.applyForLoan(custId, accId, type, amt, tenure, purpose);
                    System.out.println("Loan ID: " + loan.getLoanId());
                    System.out.printf("Monthly EMI: ₹%.2f | Total Payable: ₹%.2f | Interest: ₹%.2f%n",
                            loan.getEmi(), loan.getTotalPayable(), loan.getTotalInterest());
                }
                case 2 -> { System.out.print("Loan ID: "); service.approveLoan(scanner.nextLine().trim()); }
                case 3 -> { System.out.print("Loan ID: "); service.rejectLoan(scanner.nextLine().trim()); }
                case 4 -> { System.out.print("Loan ID: "); service.payLoanEMI(scanner.nextLine().trim()); }
                case 5 -> {
                    System.out.print("Loan ID: "); Loan loan = service.getLoan(scanner.nextLine().trim());
                    System.out.println(loan);
                }
            }
        } catch (BankingException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void reportMenu() {
        System.out.println("\n── Reports ──");
        System.out.println("1. Customer Portfolio Report");
        System.out.println("2. Bank Summary Report");
        System.out.println("3. Interest Comparison Table");
        System.out.println("4. Export Account Statement to CSV");
        int c = readInt("Choice: ");
        try {
            switch (c) {
                case 1 -> {
                    System.out.print("Customer ID: ");
                    String report = reportGen.generatePortfolioReport(scanner.nextLine().trim());
                    System.out.println(report);
                    FileHandler.writeTextReport(report, "portfolio");
                }
                case 2 -> {
                    String report = reportGen.generateBankSummaryReport();
                    System.out.println(report);
                    FileHandler.writeTextReport(report, "bank_summary");
                }
                case 3 -> {
                    double principal = readDouble("Principal Amount: ");
                    double rate      = readDouble("Annual Rate (%): ");
                    System.out.println(reportGen.generateInterestComparisonTable(
                            principal, rate, new int[]{6, 12, 24, 36, 60}));
                }
                case 4 -> {
                    System.out.print("Account ID: ");
                    String accId = scanner.nextLine().trim();
                    Account acc = service.getAccount(accId);
                    FileHandler.exportTransactionsToCsv(acc);
                }
            }
        } catch (BankingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println("\n");
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                      ║");
        System.out.println("  ║       SMART BANKING MANAGEMENT SYSTEM v2.0           ║");
        System.out.println("  ║       Built with Java 17 · Design Patterns           ║");
        System.out.println("  ║       Multithreading · Streams · OOP                 ║");
        System.out.println("  ║                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private void printMainMenu() {
        System.out.println("\n╔════════════════════════════╗");
        System.out.println("║       MAIN MENU            ║");
        System.out.println("╠════════════════════════════╣");
        System.out.println("║ 1. Customer Management     ║");
        System.out.println("║ 2. Account Management      ║");
        System.out.println("║ 3. Transactions            ║");
        System.out.println("║ 4. Loan Management         ║");
        System.out.println("║ 5. Reports                 ║");
        System.out.println("║ 6. Bank Summary            ║");
        System.out.println("║ 0. Exit                    ║");
        System.out.println("╚════════════════════════════╝");
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            int v = Integer.parseInt(scanner.nextLine().trim());
            return v;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double readDouble(String prompt) {
        System.out.print(prompt);
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Using 0.");
            return 0;
        }
    }
}
