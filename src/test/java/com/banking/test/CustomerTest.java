package com.banking.test;

import com.banking.model.Customer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

/**
 * Unit tests for Customer Builder pattern and business rules.
 */
@DisplayName("Customer Unit Tests")
class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer.Builder("CUST001")
                .name("Alice Sharma")
                .email("alice@email.com")
                .phone("9876543210")
                .address("Bangalore")
                .dateOfBirth(LocalDate.of(1992, 4, 15))
                .creditScore(780)
                .build();
    }

    @Test
    @DisplayName("Builder creates customer with correct fields")
    void testBuilderCreatesCustomer() {
        assertEquals("CUST001", customer.getCustomerId());
        assertEquals("Alice Sharma", customer.getName());
        assertEquals("alice@email.com", customer.getEmail());
        assertEquals(780, customer.getCreditScore());
        assertEquals(Customer.CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("Age is calculated correctly from DOB")
    void testAgeCalculation() {
        int expectedAge = LocalDate.now().getYear() - 1992;
        // Account for birthday not yet passed this year
        int actualAge = customer.getAge();
        assertTrue(actualAge == expectedAge || actualAge == expectedAge - 1);
    }

    @Test
    @DisplayName("Customer is loan eligible with score >= 600 and age >= 21")
    void testLoanEligibility() {
        assertTrue(customer.isLoanEligible());
    }

    @Test
    @DisplayName("Customer with low credit score is not loan eligible")
    void testLowCreditScoreIneligible() {
        Customer lowCredit = new Customer.Builder("CUST002")
                .name("Test User").email("t@t.com").phone("0000000000")
                .address("Test").dateOfBirth(LocalDate.of(1990, 1, 1))
                .creditScore(550).build();
        assertFalse(lowCredit.isLoanEligible());
    }

    @Test
    @DisplayName("Credit score update stays within 300–900 bounds")
    void testCreditScoreBounds() {
        customer.updateCreditScore(200);
        assertEquals(900, customer.getCreditScore(), "Score should cap at 900");

        customer.updateCreditScore(-700);
        assertEquals(300, customer.getCreditScore(), "Score should floor at 300");
    }

    @Test
    @DisplayName("Account linking works correctly")
    void testAccountLinking() {
        customer.linkAccount("SAV-001");
        customer.linkAccount("FD-002");
        assertEquals(2, customer.getLinkedAccountIds().size());
        customer.unlinkAccount("FD-002");
        assertEquals(1, customer.getLinkedAccountIds().size());
    }

    @Test
    @DisplayName("Builder throws without required fields")
    void testBuilderRequiredFields() {
        assertThrows(IllegalStateException.class,
                () -> new Customer.Builder("CUST003").build());
    }
}
