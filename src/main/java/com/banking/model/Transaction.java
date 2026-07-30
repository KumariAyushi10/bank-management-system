package com.banking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Immutable record of a single banking transaction.
 * Implements Serializable for file persistence.
 */
public class Transaction implements Serializable, Comparable<Transaction> {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final String transactionId;
    private final String accountId;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String description;
    private final String referenceId;    // for transfers: the counterpart account

    public Transaction(String accountId, TransactionType type, double amount,
                       double balanceAfter, String description, String referenceId) {
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountId     = accountId;
        this.type          = type;
        this.amount        = amount;
        this.balanceAfter  = balanceAfter;
        this.description   = description;
        this.referenceId   = referenceId;
        this.timestamp     = LocalDateTime.now();
    }

    // Getters
    public String          getTransactionId() { return transactionId; }
    public String          getAccountId()     { return accountId;     }
    public TransactionType getType()          { return type;          }
    public double          getAmount()        { return amount;        }
    public double          getBalanceAfter()  { return balanceAfter;  }
    public LocalDateTime   getTimestamp()     { return timestamp;     }
    public String          getDescription()  { return description;   }
    public String          getReferenceId()  { return referenceId;   }

    @Override
    public int compareTo(Transaction other) {
        return other.timestamp.compareTo(this.timestamp); // newest first
    }

    @Override
    public String toString() {
        return String.format("| %-18s | %-16s | %s%-10.2f | %-12s | %-30s |",
                transactionId,
                timestamp.format(FMT),
                type.getSign(),
                amount,
                type.getDisplayName(),
                description.length() > 30 ? description.substring(0, 27) + "..." : description);
    }
}
