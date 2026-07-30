package com.banking.pattern.strategy;

/**
 * Context class that uses an InterestStrategy.
 * Demonstrates how Strategy pattern allows runtime algorithm switching.
 */
public class InterestCalculator {
    private InterestStrategy strategy;

    public InterestCalculator(InterestStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(InterestStrategy strategy) {
        this.strategy = strategy;
    }

    public double calculate(double principal, double ratePerAnnum, int months) {
        return strategy.calculateInterest(principal, ratePerAnnum, months);
    }

    public String getStrategyName() { return strategy.getStrategyName(); }

    public void printComparison(double principal, double ratePerAnnum, int months) {
        InterestStrategy simple    = new SimpleInterestStrategy();
        InterestStrategy quarterly = new CompoundInterestStrategy(4);
        InterestStrategy monthly   = new CompoundInterestStrategy(12);

        System.out.println("\n═══════ Interest Strategy Comparison ═══════");
        System.out.printf("Principal: ₹%.2f | Rate: %.2f%% p.a. | Period: %d months%n",
                principal, ratePerAnnum, months);
        System.out.println("─".repeat(45));
        System.out.printf("%-30s : ₹%.2f%n", simple.getStrategyName(),
                simple.calculateInterest(principal, ratePerAnnum, months));
        System.out.printf("%-30s : ₹%.2f%n", quarterly.getStrategyName(),
                quarterly.calculateInterest(principal, ratePerAnnum, months));
        System.out.printf("%-30s : ₹%.2f%n", monthly.getStrategyName(),
                monthly.calculateInterest(principal, ratePerAnnum, months));
        System.out.println("═".repeat(45));
    }
}
