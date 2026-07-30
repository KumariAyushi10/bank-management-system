package com.banking.pattern.observer;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fraud detection observer – flags suspicious transactions.
 * Demonstrates the Observer pattern consumer side.
 */
public class FraudDetectionObserver implements TransactionObserver {

    private static final double LARGE_TRANSACTION_THRESHOLD = 100_000.0;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;

    private final Map<String, Integer> hourlyTransactionCounts = new HashMap<>();
    private final java.util.List<String> fraudAlerts = new java.util.ArrayList<>();

    @Override
    public void onTransaction(Transaction txn) {
        boolean suspicious = false;
        StringBuilder reason = new StringBuilder();

        // Rule 1: Large transaction
        if (txn.getAmount() >= LARGE_TRANSACTION_THRESHOLD) {
            suspicious = true;
            reason.append(String.format("Large transaction ₹%.2f. ", txn.getAmount()));
        }

        // Rule 2: Late-night / odd-hours transaction (12am–4am)
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.MIDNIGHT) && now.isBefore(LocalTime.of(4, 0))) {
            suspicious = true;
            reason.append("Odd-hours transaction. ");
        }

        // Rule 3: High-frequency (> threshold per hour per account)
        String key = txn.getAccountId() + ":" + LocalTime.now().getHour();
        int count = hourlyTransactionCounts.getOrDefault(key, 0) + 1;
        hourlyTransactionCounts.put(key, count);
        if (count > MAX_TRANSACTIONS_PER_HOUR) {
            suspicious = true;
            reason.append("High-frequency transactions (" + count + " this hour). ");
        }

        // Rule 4: Back-to-back large withdrawals
        if (txn.getType() == TransactionType.WITHDRAWAL && txn.getAmount() > 50_000) {
            suspicious = true;
            reason.append("Large withdrawal flagged. ");
        }

        if (suspicious) {
            String alert = String.format("[FRAUD ALERT] TxnID=%s | Account=%s | Amount=₹%.2f | %s",
                    txn.getTransactionId(), txn.getAccountId(), txn.getAmount(), reason);
            fraudAlerts.add(alert);
            System.out.println("\u001B[31m" + alert + "\u001B[0m");
        }
    }

    @Override
    public String getObserverName() { return "FraudDetectionObserver"; }

    public java.util.List<String> getFraudAlerts() {
        return java.util.Collections.unmodifiableList(fraudAlerts);
    }
}
