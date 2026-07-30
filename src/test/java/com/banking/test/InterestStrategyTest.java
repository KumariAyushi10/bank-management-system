package com.banking.test;

import com.banking.pattern.strategy.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Strategy pattern — interest calculation algorithms.
 */
@DisplayName("Strategy Pattern — Interest Calculation Tests")
class InterestStrategyTest {

    @Test
    @DisplayName("Simple interest: P=100000, R=10%, T=1yr → ₹10000")
    void testSimpleInterest() {
        InterestStrategy strategy = new SimpleInterestStrategy();
        double result = strategy.calculateInterest(100000, 10, 12);
        assertEquals(10000.0, result, 0.01);
    }

    @Test
    @DisplayName("Simple interest for 6 months is half of annual")
    void testSimpleInterestHalfYear() {
        InterestStrategy strategy = new SimpleInterestStrategy();
        double annual   = strategy.calculateInterest(100000, 10, 12);
        double halfYear = strategy.calculateInterest(100000, 10, 6);
        assertEquals(annual / 2, halfYear, 0.01);
    }

    @Test
    @DisplayName("Compound interest (quarterly) > simple interest")
    void testCompoundGreaterThanSimple() {
        double principal = 100000;
        double rate      = 10;
        int months       = 12;
        double simple    = new SimpleInterestStrategy().calculateInterest(principal, rate, months);
        double compound  = new CompoundInterestStrategy(4).calculateInterest(principal, rate, months);
        assertTrue(compound > simple, "Compound interest must exceed simple interest");
    }

    @Test
    @DisplayName("Monthly compounding > quarterly compounding")
    void testMonthlyGreaterThanQuarterly() {
        double principal = 100000;
        double rate      = 10;
        int months       = 12;
        double quarterly = new CompoundInterestStrategy(4).calculateInterest(principal, rate, months);
        double monthly   = new CompoundInterestStrategy(12).calculateInterest(principal, rate, months);
        assertTrue(monthly > quarterly);
    }

    @Test
    @DisplayName("Strategy can be swapped at runtime via InterestCalculator")
    void testStrategySwap() {
        InterestCalculator calc = new InterestCalculator(new SimpleInterestStrategy());
        double simple = calc.calculate(100000, 10, 12);

        calc.setStrategy(new CompoundInterestStrategy(4));
        double compound = calc.calculate(100000, 10, 12);

        assertNotEquals(simple, compound, 0.01, "Swapping strategy should change result");
    }

    @Test
    @DisplayName("Zero principal returns zero interest")
    void testZeroPrincipal() {
        assertEquals(0.0, new SimpleInterestStrategy().calculateInterest(0, 10, 12), 0.001);
        assertEquals(0.0, new CompoundInterestStrategy(4).calculateInterest(0, 10, 12), 0.001);
    }
}
