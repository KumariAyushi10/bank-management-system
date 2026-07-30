package com.banking.exception;

/**
 * Thrown when an account has insufficient balance for a withdrawal or transfer.
 */
public class InsufficientFundsException extends BankingException {
    private final double requestedAmount;
    private final double availableBalance;

    public InsufficientFundsException(double requestedAmount, double availableBalance) {
        super(String.format("Insufficient funds: requested ₹%.2f but available balance is ₹%.2f",
                requestedAmount, availableBalance), "ERR_INSUFFICIENT_FUNDS");
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public double getRequestedAmount()  { return requestedAmount;  }
    public double getAvailableBalance() { return availableBalance; }
    public double getShortfall()        { return requestedAmount - availableBalance; }
}
