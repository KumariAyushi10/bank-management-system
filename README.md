# bank-management-system

A secure and structured banking application developed using Java and managed with the Maven build automation tool. The system handles core banking operations including account administration, transactional data tracking, and structured audit logs.

## Features

* **Account Management:** System tools to create, modify, and track user banking accounts.
* **Transaction Engine:** Handles banking actions securely like deposits, withdrawals, and log storage.
* **Audit Logging:** Automatically records system activity to a centralized local file (`audit_trail.log`).
* **Maven Build System:** Uses a clean object model setup (`pom.xml`) for managing dependencies and compilation tasks.

## Project Structure

```text
├── data/              # Database files or local information stores
├── reports/           # Generated banking statements and output data
├── src/               # Core Java source application code
├── pom.xml            # Maven project object model and dependency management
├── audit_trail.log    # Automatically generated transaction history tracking
└── README.md          # Project documentation
```

## How to Run It

To run this application locally on your machine, you will need Java Development Kit (JDK) and Maven installed:

1. Click the green **Code** button at the top of this GitHub page and select **Download ZIP**.
2. Extract the downloaded ZIP file on your computer.
3. Open your terminal or command prompt and navigate inside the extracted folder:
   ```bash
   cd bank-management-system
   ```
4. Build the application dependencies using Maven:
   ```bash
   mvn clean package
   ```
5. Run the compiled application file:
   ```bash
   mvn exec:java
   ```


