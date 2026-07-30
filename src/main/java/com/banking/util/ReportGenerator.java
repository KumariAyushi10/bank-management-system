package com.banking.util;

import com.banking.model.*;
import com.banking.pattern.singleton.BankDatabase;
import com.banking.pattern.strategy.CompoundInterestStrategy;
import com.banking.pattern.strategy.InterestCalculator;
import com.banking.pattern.strategy.SimpleInterestStrategy;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report generator using Java Streams, Collectors, and grouping operations.
 * Demonstrates functional-style data aggregation.
 */
public class ReportGenerator {

    private final BankDatabase db = BankDatabase.getInstance();

    // ── Account Portfolio Report ──────────────────────────────────────────────

    public String generatePortfolioReport(String customerId) {
        Customer customer = db.findCustomer(customerId);
        if (customer == null) return "Customer not found: " + customerId;

        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(60)).append("\n");
        sb.append(String.format("  PORTFOLIO REPORT — %s (%s)%n", customer.getName(), customerId));
        sb.append(String.format("  Generated: %s%n", LocalDate.now()));
        sb.append("═".repeat(60)).append("\n\n");

        List<Account> accounts = customer.getLinkedAccountIds().stream()
                .map(db::findAccount).filter(Objects::nonNull)
                .sorted().collect(Collectors.toList());

        if (accounts.isEmpty()) {
            sb.append("No accounts found.\n");
        } else {
            double totalBalance = 0;
            for (Account a : accounts) {
                sb.append(String.format("  %-40s : ₹%12.2f  [%s]%n",
                        a.getAccountId() + " (" + a.getAccountType().getDisplayName() + ")",
                        a.getBalance(), a.getStatus()));
                totalBalance += a.getBalance();
            }
            sb.append("─".repeat(60)).append("\n");
            sb.append(String.format("  %-40s : ₹%12.2f%n", "TOTAL NET WORTH", totalBalance));
        }

        // Loans
        List<Loan> loans = db.getAllLoans().values().stream()
                .filter(l -> l.getCustomerId().equals(customerId))
                .collect(Collectors.toList());

        if (!loans.isEmpty()) {
            sb.append("\n  ACTIVE LOANS:\n");
            for (Loan l : loans) {
                if (l.getStatus() == Loan.LoanStatus.ACTIVE) {
                    sb.append(String.format("  %-15s %-10s EMI:₹%.2f Outstanding:₹%.2f%n",
                            l.getLoanId(), l.getLoanType(), l.getEmi(), l.getOutstandingBalance()));
                }
            }
        }

        sb.append("═".repeat(60)).append("\n");
        return sb.toString();
    }

    // ── Bank-Wide Summary ──────────────────────────────────────────────────────

    public String generateBankSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(65)).append("\n");
        sb.append("              SMART BANK — EXECUTIVE SUMMARY\n");
        sb.append(String.format("              As on: %s%n", LocalDate.now()));
        sb.append("═".repeat(65)).append("\n\n");

        // Accounts by type
        Map<AccountType, Long> byType = db.getAllAccounts().values().stream()
                .collect(Collectors.groupingBy(Account::getAccountType, Collectors.counting()));

        sb.append("  ACCOUNTS BY TYPE:\n");
        byType.forEach((type, count) ->
                sb.append(String.format("    %-30s : %d%n", type.getDisplayName(), count)));

        // Total deposits
        double totalDeposits = db.getAllAccounts().values().stream()
                .mapToDouble(Account::getBalance).sum();
        sb.append(String.format("%n  Total Deposits   : ₹%.2f%n", totalDeposits));

        // Loans by type
        Map<Loan.LoanType, DoubleSummaryStatistics> loanStats = db.getAllLoans().values().stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                .collect(Collectors.groupingBy(Loan::getLoanType,
                         Collectors.summarizingDouble(Loan::getOutstandingBalance)));

        if (!loanStats.isEmpty()) {
            sb.append("\n  ACTIVE LOAN BOOK BY TYPE:\n");
            loanStats.forEach((type, stats) ->
                    sb.append(String.format("    %-15s Count:%-4d Outstanding:₹%.2f%n",
                            type, stats.getCount(), stats.getSum())));
        }

        // Top 5 accounts by balance
        sb.append("\n  TOP 5 ACCOUNTS BY BALANCE:\n");
        db.getAllAccounts().values().stream()
                .sorted(Comparator.comparingDouble(Account::getBalance).reversed())
                .limit(5)
                .forEach(a -> sb.append(String.format("    %-25s ₹%.2f%n", a.getAccountId(), a.getBalance())));

        sb.append("\n").append("═".repeat(65)).append("\n");
        return sb.toString();
    }

    // ── Interest Comparison Table ──────────────────────────────────────────────

    public String generateInterestComparisonTable(double principal, double rate, int[] tenures) {
        InterestCalculator calc = new InterestCalculator(new SimpleInterestStrategy());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n  Interest Comparison Table — Principal: ₹%.2f | Rate: %.2f%% p.a.%n",
                principal, rate));
        sb.append("─".repeat(70)).append("\n");
        sb.append(String.format("  %-15s %-20s %-20s %-20s%n",
                "Tenure", "Simple", "Quarterly CI", "Monthly CI"));
        sb.append("─".repeat(70)).append("\n");

        for (int months : tenures) {
            double simple    = new SimpleInterestStrategy().calculateInterest(principal, rate, months);
            double quarterly = new CompoundInterestStrategy(4).calculateInterest(principal, rate, months);
            double monthly   = new CompoundInterestStrategy(12).calculateInterest(principal, rate, months);
            sb.append(String.format("  %-15s ₹%-19.2f ₹%-19.2f ₹%-19.2f%n",
                    months + " months", simple, quarterly, monthly));
        }
        sb.append("─".repeat(70)).append("\n");
        return sb.toString();
    }
}
