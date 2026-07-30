package com.banking.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a bank customer with KYC details.
 * Uses Builder pattern for clean object construction.
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private final LocalDate dateOfBirth;
    private String panNumber;
    private String aadharNumber;
    private final LocalDate accountOpenDate;
    private CustomerStatus status;
    private final List<String> linkedAccountIds;
    private int creditScore;

    public enum CustomerStatus { ACTIVE, SUSPENDED, CLOSED }

    // Private constructor – use Builder
    private Customer(Builder b) {
        this.customerId       = b.customerId;
        this.name             = b.name;
        this.email            = b.email;
        this.phone            = b.phone;
        this.address          = b.address;
        this.dateOfBirth      = b.dateOfBirth;
        this.panNumber        = b.panNumber;
        this.aadharNumber     = b.aadharNumber;
        this.accountOpenDate  = LocalDate.now();
        this.status           = CustomerStatus.ACTIVE;
        this.linkedAccountIds = new ArrayList<>();
        this.creditScore      = b.creditScore;
    }

    // ── Builder ──────────────────────────────────────────────────────────────
    public static class Builder {
        private final String customerId;
        private String name;
        private String email;
        private String phone;
        private String address;
        private LocalDate dateOfBirth;
        private String panNumber    = "";
        private String aadharNumber = "";
        private int    creditScore  = 650;

        public Builder(String customerId) { this.customerId = customerId; }

        public Builder name(String v)         { this.name         = v; return this; }
        public Builder email(String v)        { this.email        = v; return this; }
        public Builder phone(String v)        { this.phone        = v; return this; }
        public Builder address(String v)      { this.address      = v; return this; }
        public Builder dateOfBirth(LocalDate v){ this.dateOfBirth = v; return this; }
        public Builder panNumber(String v)    { this.panNumber    = v; return this; }
        public Builder aadharNumber(String v) { this.aadharNumber = v; return this; }
        public Builder creditScore(int v)     { this.creditScore  = v; return this; }

        public Customer build() {
            if (name == null || email == null || phone == null || dateOfBirth == null)
                throw new IllegalStateException("name, email, phone, dateOfBirth are required");
            return new Customer(this);
        }
    }

    // ── Computed fields ───────────────────────────────────────────────────────
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isLoanEligible() {
        return status == CustomerStatus.ACTIVE && creditScore >= 600 && getAge() >= 21;
    }

    // ── Mutators ──────────────────────────────────────────────────────────────
    public void linkAccount(String accountId)   { linkedAccountIds.add(accountId); }
    public void unlinkAccount(String accountId) { linkedAccountIds.remove(accountId); }
    public void updateCreditScore(int delta)    { creditScore = Math.max(300, Math.min(900, creditScore + delta)); }
    public void setStatus(CustomerStatus s)     { this.status = s; }
    public void setEmail(String email)          { this.email = email; }
    public void setPhone(String phone)          { this.phone = phone; }
    public void setAddress(String address)      { this.address = address; }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String         getCustomerId()      { return customerId;      }
    public String         getName()            { return name;            }
    public String         getEmail()           { return email;           }
    public String         getPhone()           { return phone;           }
    public String         getAddress()         { return address;         }
    public LocalDate      getDateOfBirth()     { return dateOfBirth;     }
    public String         getPanNumber()       { return panNumber;       }
    public String         getAadharNumber()    { return aadharNumber;    }
    public LocalDate      getAccountOpenDate() { return accountOpenDate; }
    public CustomerStatus getStatus()          { return status;          }
    public int            getCreditScore()     { return creditScore;     }
    public List<String>   getLinkedAccountIds(){ return Collections.unmodifiableList(linkedAccountIds); }

    @Override
    public String toString() {
        return String.format("Customer{id=%s, name='%s', email='%s', age=%d, credit=%d, status=%s}",
                customerId, name, email, getAge(), creditScore, status);
    }
}
