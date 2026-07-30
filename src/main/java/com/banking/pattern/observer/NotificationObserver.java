package com.banking.pattern.observer;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;

/**
 * Notification observer – simulates SMS/email alerts to customers.
 */
public class NotificationObserver implements TransactionObserver {

    @Override
    public void onTransaction(Transaction txn) {
        String msg = buildMessage(txn);
        // In a real system this would call SMS/email gateway
        System.out.printf("\u001B[36m[NOTIFICATION] %s\u001B[0m%n", msg);
    }

    private String buildMessage(Transaction txn) {
        if (txn.getType().isCredit()) {
            return String.format("Account %s credited with ₹%.2f. Available balance: ₹%.2f. Ref: %s",
                    txn.getAccountId(), txn.getAmount(), txn.getBalanceAfter(), txn.getTransactionId());
        } else {
            return String.format("Account %s debited ₹%.2f. Available balance: ₹%.2f. Ref: %s",
                    txn.getAccountId(), txn.getAmount(), txn.getBalanceAfter(), txn.getTransactionId());
        }
    }

    @Override
    public String getObserverName() { return "NotificationObserver"; }
}
