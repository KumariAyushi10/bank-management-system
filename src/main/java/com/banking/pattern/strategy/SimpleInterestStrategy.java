package com.banking.pattern.strategy;

/**
 * Simple Interest: I = P * R * T / 100
 */
public class SimpleInterestStrategy implements InterestStrategy {

    @Override
    public double calculateInterest(double principal, double ratePerAnnum, int months) {
        double years = months / 12.0;
        return principal * ratePerAnnum * years / 100.0;
    }

    @Override
    public String getStrategyName() { return "Simple Interest"; }
}
