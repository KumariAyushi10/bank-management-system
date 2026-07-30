package com.banking.pattern.observer;

import com.banking.model.Transaction;

/**
 * Observer interface for the Observer design pattern.
 * Any class that wants to react to transactions must implement this.
 */
public interface TransactionObserver {
    void onTransaction(Transaction transaction);
    String getObserverName();
}
