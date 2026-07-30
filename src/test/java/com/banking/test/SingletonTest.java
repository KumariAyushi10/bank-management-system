package com.banking.test;

import com.banking.pattern.singleton.BankDatabase;
import com.banking.model.Customer;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Singleton BankDatabase — including thread-safety verification.
 */
@DisplayName("Singleton Pattern Tests")
class SingletonTest {

    @Test
    @DisplayName("getInstance always returns the same object reference")
    void testSingletonReference() {
        BankDatabase db1 = BankDatabase.getInstance();
        BankDatabase db2 = BankDatabase.getInstance();
        assertSame(db1, db2, "BankDatabase must be a singleton");
    }

    @Test
    @DisplayName("Singleton is thread-safe — 50 threads all get the same instance")
    void testSingletonThreadSafety() throws InterruptedException {
        int threadCount = 50;
        BankDatabase[] instances = new BankDatabase[threadCount];
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService es = Executors.newFixedThreadPool(10);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            es.submit(() -> {
                instances[idx] = BankDatabase.getInstance();
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        es.shutdown();

        BankDatabase first = instances[0];
        for (BankDatabase db : instances) {
            assertSame(first, db, "All threads must see the same singleton instance");
        }
    }

    @Test
    @DisplayName("Transaction counter is thread-safe under concurrent access")
    void testTransactionCounterConcurrency() throws InterruptedException {
        BankDatabase db = BankDatabase.getInstance();
        int threadCount = 100;
        AtomicInteger duplicates = new AtomicInteger(0);
        ConcurrentHashMap<Long, Boolean> seen = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService es = Executors.newFixedThreadPool(10);

        for (int i = 0; i < threadCount; i++) {
            es.submit(() -> {
                long num = db.nextTransactionNumber();
                if (seen.put(num, true) != null) duplicates.incrementAndGet();
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        es.shutdown();
        assertEquals(0, duplicates.get(), "No duplicate transaction numbers should be generated");
    }
}
