package com.banking.util;

import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.Transaction;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Utility class for persisting and reading banking data to/from the filesystem.
 * Demonstrates Java I/O: BufferedWriter, FileOutputStream, ObjectOutputStream,
 * NIO2 Paths/Files, and serialization.
 */
public class FileHandler {

    private static final String DATA_DIR    = "data/";
    private static final String REPORT_DIR  = "reports/";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        createDirIfAbsent(DATA_DIR);
        createDirIfAbsent(REPORT_DIR);
    }

    private static void createDirIfAbsent(String dir) {
        try { Files.createDirectories(Paths.get(dir)); }
        catch (IOException e) { System.err.println("Cannot create directory: " + dir); }
    }

    // ── Serialization (binary) ────────────────────────────────────────────────

    public static <T extends Serializable> void serialize(T object, String filename) {
        String path = DATA_DIR + filename;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(object);
        } catch (IOException e) {
            System.err.println("Serialization failed [" + path + "]: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> deserialize(String filename) {
        String path = DATA_DIR + filename;
        File file = new File(path);
        if (!file.exists()) return Optional.empty();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return Optional.of((T) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Deserialization failed [" + path + "]: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ── CSV Export ────────────────────────────────────────────────────────────

    public static void exportTransactionsToCsv(Account account) {
        String filename = REPORT_DIR + account.getAccountId() + "_statement_"
                + LocalDateTime.now().format(TS) + ".csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("TransactionID,Date,Type,Amount,BalanceAfter,Description");
            bw.newLine();
            for (Transaction txn : account.getTransactions()) {
                bw.write(String.join(",",
                        txn.getTransactionId(),
                        txn.getTimestamp().toString(),
                        txn.getType().getDisplayName(),
                        String.valueOf(txn.getAmount()),
                        String.valueOf(txn.getBalanceAfter()),
                        "\"" + txn.getDescription().replace("\"", "'") + "\""));
                bw.newLine();
            }
            System.out.println("Statement exported → " + filename);
        } catch (IOException e) {
            System.err.println("CSV export failed: " + e.getMessage());
        }
    }

    // ── Text Report ───────────────────────────────────────────────────────────

    public static void writeTextReport(String content, String reportName) {
        String filename = REPORT_DIR + reportName + "_" + LocalDateTime.now().format(TS) + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write(content);
            System.out.println("Report written → " + filename);
        } catch (IOException e) {
            System.err.println("Report write failed: " + e.getMessage());
        }
    }

    // ── Append Log ────────────────────────────────────────────────────────────

    public static void appendLog(String logFile, String entry) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + logFile, true))) {
            pw.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "] " + entry);
        } catch (IOException e) {
            System.err.println("Log append failed: " + e.getMessage());
        }
    }
}
