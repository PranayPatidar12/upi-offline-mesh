package com.demo.upi_offline_mesh.service;
import com.demo.upi_offline_mesh.crypto.HybridCryptoService;
import com.demo.upi_offline_mesh.model.Account;
import com.demo.upi_offline_mesh.model.AccountRepo;
import com.demo.upi_offline_mesh.model.MeshPacket;
import com.demo.upi_offline_mesh.model.PaymentInstruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrentIngestionTest {
    @Autowired
    private BridgeIngestionService bridgeIngestionService;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private HybridCryptoService cryptoService;

    @BeforeEach
    void setUp() {
        accountRepo.deleteAll();
        accountRepo.save(new Account("test-sender@upi", "Sender", new BigDecimal("1000.00")));
        accountRepo.save(new Account("test-receiver@upi", "Receiver", new BigDecimal("500.00")));
    }

    @Test
    void sameDuplicatePacketFromMultipleThreads_onlyOneSettles() throws Exception {
        PaymentInstruction instruction = new PaymentInstruction(
                "test-sender@upi",
                "test-receiver@upi",
                new BigDecimal("100.00"),
                "1234hashed",
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        String cipherText = cryptoService.encrypt(instruction);
        MeshPacket packet = new MeshPacket(UUID.randomUUID().toString(), 5, System.currentTimeMillis(), cipherText);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Callable<IngestionResult>> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                readyLatch.countDown();
                startLatch.await();
                return bridgeIngestionService.ingest(packet);
            });
        }

        List<Future<IngestionResult>> futures = new java.util.ArrayList<>();
        for (Callable<IngestionResult> task : tasks) {
            futures.add(executor.submit(task));
        }

        readyLatch.await();
        startLatch.countDown();

        AtomicInteger settledCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);

        for (Future<IngestionResult> future : futures) {
            IngestionResult result = future.get(10, TimeUnit.SECONDS);
            if (result.outcome() == IngestionOutcome.SETTLED) {
                settledCount.incrementAndGet();
            } else if (result.outcome() == IngestionOutcome.DUPLICATE_DROPPED) {
                duplicateCount.incrementAndGet();
            }
        }

        executor.shutdown();

        assertEquals(1, settledCount.get());
        assertEquals(threadCount - 1, duplicateCount.get());

        Account sender = accountRepo.findById("test-sender@upi").orElseThrow();
        Account receiver = accountRepo.findById("test-receiver@upi").orElseThrow();

        assertEquals(new BigDecimal("900.00"), sender.getBalance());
        assertEquals(new BigDecimal("600.00"), receiver.getBalance());
    }
}
