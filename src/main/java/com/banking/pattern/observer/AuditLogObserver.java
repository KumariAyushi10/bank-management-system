package com.banking.pattern.observer;

import com.banking.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit log observer – writes every transaction to a flat-file audit trail.
 */
public class AuditLogObserver implements TransactionObserver {
    private static final String LOG_FILE = "audit_trail.log";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onTransaction(Transaction txn) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.printf("[AUDIT] %s | %s | %s | ₹%.2f | Balance After: ₹%.2f | %s%n",
                    LocalDateTime.now().format(FMT),
                    txn.getAccountId(),
                    txn.getTransactionId(),
                    txn.getAmount(),
                    txn.getBalanceAfter(),
                    txn.getDescription());
        } catch (IOException e) {
            System.err.println("Audit log write failed: " + e.getMessage());
        }
    }

    @Override
    public String getObserverName() { return "AuditLogObserver"; }
}
