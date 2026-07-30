package com.banking.pattern.strategy;

/**
 * Strategy interface for interest calculation.
 *
 * Design Pattern: Strategy
 */
public interface InterestStrategy {
    double calculateInterest(double principal, double ratePerAnnum, int months);
    String getStrategyName();
}
