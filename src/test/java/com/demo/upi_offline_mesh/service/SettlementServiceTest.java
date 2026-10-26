package com.demo.upi_offline_mesh.service;
import com.demo.upi_offline_mesh.model.Account;
import com.demo.upi_offline_mesh.model.AccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AccountRepo accountRepo;

    @BeforeEach
    void setUp(){
        accountRepo.deleteAll();
        accountRepo.save((new Account("test-sender@upi", "Sender", new BigDecimal("1000.00"))));
        accountRepo.save(new Account("test-receiver@upi", "Receiver", new BigDecimal("500.00")));
    }

    @Test
    void settleMovesBalanceCorrectly(){
        SettlementResult result = settlementService.settle(
                "test-sender@upi", "test-receiver@upi",
                new BigDecimal("200.00"), UUID.randomUUID().toString()
        );

        assertEquals(SettlementOutcome.SUCCESS, result.outcome());

        Account sender = accountRepo.findById("test-sender@upi").orElseThrow();
        Account receiver = accountRepo.findById("test-receiver@upi").orElseThrow();

        assertEquals(new BigDecimal("800.00"),sender.getBalance());
        assertEquals(new BigDecimal("700.00"), receiver.getBalance());
    }

    @Test
    void settleFailsOnInsufficientFunds() {
        SettlementResult result = settlementService.settle(
                "test-sender@upi", "test-receiver@upi",
                new BigDecimal("5000.00"), UUID.randomUUID().toString()
        );

        assertEquals(SettlementOutcome.INSUFFICIENT_FUND, result.outcome());

        Account sender = accountRepo.findById("test-sender@upi").orElseThrow();
        assertEquals(new BigDecimal("1000.00"), sender.getBalance());
    }

    @Test
    void settleFailsOnUnknownAccount() {
        SettlementResult result = settlementService.settle(
                "ghost@upi", "test-receiver@upi",
                new BigDecimal("100.00"), UUID.randomUUID().toString()
        );

        assertEquals(SettlementOutcome.ACCOUNT_NOT_FOUND, result.outcome());
    }


}
