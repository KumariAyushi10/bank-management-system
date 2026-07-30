package com.banking.concurrent;

import com.banking.exception.BankingException;
import com.banking.model.Account;
import com.banking.model.Transaction;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Multithreaded transaction processor using a thread pool.
 * Demonstrates Java concurrency: ExecutorService, Future, AtomicLong, locks.
 *
 * In a real banking system, transactions queue here and are processed
 * by multiple worker threads concurrently.
 */
public class TransactionProcessor {

    private final ExecutorService executor;
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final BlockingQueue<String> processedLog = new LinkedBlockingQueue<>(1000);

    public TransactionProcessor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize,
                r -> {
                    Thread t = new Thread(r, "BankWorker-" + System.nanoTime() % 100);
                    t.setDaemon(true);
                    return t;
                });
    }

    /**
     * Submit a transaction task for async processing.
     * Returns a Future so callers can block or poll.
     */
    public <T> Future<T> submit(Supplier<T> task, String description) {
        return executor.submit(() -> {
            try {
                T result = task.get();
                successCount.incrementAndGet();
                processedLog.offer("[OK]  " + Thread.currentThread().getName() + " | " + description);
                return result;
            } catch (Exception e) {
                failureCount.incrementAndGet();
                processedLog.offer("[ERR] " + Thread.currentThread().getName() + " | " + description + " | " + e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Submit a fire-and-forget task (no return value needed).
     */
    public void submitAndForget(Runnable task, String description) {
        executor.execute(() -> {
            try {
                task.run();
                successCount.incrementAndGet();
                processedLog.offer("[OK]  " + Thread.currentThread().getName() + " | " + description);
            } catch (Exception e) {
                failureCount.incrementAndGet();
                processedLog.offer("[ERR] " + Thread.currentThread().getName() + " | " + description + " | " + e.getMessage());
            }
        });
    }

    /**
     * Batch-process multiple accounts for interest calculation concurrently.
     */
    public void applyInterestBatch(Iterable<Account> accounts) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch((int) ((java.util.Collection<?>) accounts).size());
        for (Account account : accounts) {
            executor.submit(() -> {
                try {
                    account.applyInterest();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    public long getSuccessCount() { return successCount.get(); }
    public long getFailureCount() { return failureCount.get(); }

    public void printStats() {
        System.out.printf("TransactionProcessor stats — Success: %d | Failure: %d%n",
                successCount.get(), failureCount.get());
    }
}
