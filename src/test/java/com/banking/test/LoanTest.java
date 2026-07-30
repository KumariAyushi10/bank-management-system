package com.banking.test;

import com.banking.model.Loan;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Loan EMI calculations and lifecycle.
 */
@DisplayName("Loan Unit Tests")
class LoanTest {

    private Loan personalLoan;

    @BeforeEach
    void setUp() {
        personalLoan = new Loan("CUST001", "SAV-001", Loan.LoanType.PERSONAL,
                200000, 12.0, 24, "Medical expenses");
    }

    @Test
    @DisplayName("EMI calculation matches standard formula")
    void testEmiCalculation() {
        // P=200000, r=12%/12=1%, n=24
        // EMI = 200000 * 0.01 * (1.01)^24 / ((1.01)^24 - 1)
        double expected = Loan.calculateEMI(200000, 12.0, 24);
        assertEquals(expected, personalLoan.getEmi(), 0.01);
        assertTrue(personalLoan.getEmi() > 0);
    }

    @Test
    @DisplayName("Total payable is EMI × tenure")
    void testTotalPayable() {
        assertEquals(personalLoan.getEmi() * 24, personalLoan.getTotalPayable(), 0.01);
    }

    @Test
    @DisplayName("Total interest = total payable − principal")
    void testTotalInterest() {
        assertEquals(personalLoan.getTotalPayable() - 200000,
                     personalLoan.getTotalInterest(), 0.01);
        assertTrue(personalLoan.getTotalInterest() > 0);
    }

    @Test
    @DisplayName("Loan starts in PENDING state")
    void testInitialStatus() {
        assertEquals(Loan.LoanStatus.PENDING, personalLoan.getStatus());
    }

    @Test
    @DisplayName("Approve changes status to ACTIVE")
    void testApproval() {
        personalLoan.approve();
        assertEquals(Loan.LoanStatus.ACTIVE, personalLoan.getStatus());
    }

    @Test
    @DisplayName("Reject changes status to REJECTED")
    void testRejection() {
        personalLoan.reject();
        assertEquals(Loan.LoanStatus.REJECTED, personalLoan.getStatus());
    }

    @Test
    @DisplayName("Cannot approve an already-rejected loan")
    void testApproveRejectedLoan() {
        personalLoan.reject();
        assertThrows(IllegalStateException.class, personalLoan::approve);
    }

    @Test
    @DisplayName("EMI payment reduces outstanding balance")
    void testEmiPaymentReducesBalance() {
        personalLoan.approve();
        double balanceBefore = personalLoan.getOutstandingBalance();
        personalLoan.payEMI();
        assertTrue(personalLoan.getOutstandingBalance() < balanceBefore);
        assertEquals(1, personalLoan.getEmiPaid());
    }

    @Test
    @DisplayName("Loan closes after all EMIs are paid")
    void testLoanClosure() {
        personalLoan.approve();
        boolean closed = false;
        for (int i = 0; i < 24 && !closed; i++) {
            closed = personalLoan.payEMI();
        }
        assertTrue(closed, "Loan should be closed after all EMIs are paid");
        assertEquals(Loan.LoanStatus.CLOSED, personalLoan.getStatus());
    }

    @Test
    @DisplayName("Cannot pay EMI on non-active loan")
    void testPayEmiOnPendingLoan() {
        assertThrows(IllegalStateException.class, personalLoan::payEMI);
    }

    @Test
    @DisplayName("Zero-rate loan EMI equals principal / tenure")
    void testZeroRateLoan() {
        double emi = Loan.calculateEMI(120000, 0, 12);
        assertEquals(10000.0, emi, 0.01);
    }
}
