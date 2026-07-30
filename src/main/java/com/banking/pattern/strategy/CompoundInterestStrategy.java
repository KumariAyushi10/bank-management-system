package com.banking.pattern.strategy;

/**
 * Compound Interest: A = P * (1 + r/n)^(n*t)   [n = compounding frequency per year]
 */
public class CompoundInterestStrategy implements InterestStrategy {

    private final int compoundingFrequency; // 1=annual, 2=half-yearly, 4=quarterly, 12=monthly

    public CompoundInterestStrategy(int compoundingFrequency) {
        this.compoundingFrequency = compoundingFrequency;
    }

    @Override
    public double calculateInterest(double principal, double ratePerAnnum, int months) {
        double r = ratePerAnnum / 100.0;
        double t = months / 12.0;
        double n = compoundingFrequency;
        double amount = principal * Math.pow(1 + r / n, n * t);
        return amount - principal;
    }

    @Override
    public String getStrategyName() {
        String freq = switch (compoundingFrequency) {
            case 1  -> "Annual";
            case 2  -> "Half-Yearly";
            case 4  -> "Quarterly";
            case 12 -> "Monthly";
            default -> "Every " + (12 / compoundingFrequency) + " months";
        };
        return "Compound Interest (" + freq + ")";
    }
}
