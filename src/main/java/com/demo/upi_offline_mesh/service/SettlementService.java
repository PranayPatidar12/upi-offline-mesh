package com.demo.upi_offline_mesh.service;
import com.demo.upi_offline_mesh.model.Account;
import com.demo.upi_offline_mesh.model.AccountRepo;
import com.demo.upi_offline_mesh.model.Transaction;
import com.demo.upi_offline_mesh.model.TransactionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
public class SettlementService {

    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;

    public SettlementService(AccountRepo accountRepo, TransactionRepo transactionRepo){
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
    }

    @Transactional
    public SettlementResult settle(String senderVpa, String receiverVpa, BigDecimal amount, String packetHash){
        Optional<Account> senderOpt = accountRepo.findById(senderVpa);
        Optional<Account> receiverOpt = accountRepo.findById(receiverVpa);

        if(senderOpt.isEmpty() || receiverOpt.isEmpty()){
            return SettlementResult.failed(SettlementOutcome.ACCOUNT_NOT_FOUND);
        }

        Account sender = senderOpt.get();
        Account receiver = receiverOpt.get();

        if(sender.getBalance().compareTo(amount)<0){
            return SettlementResult.failed(SettlementOutcome.INSUFFICIENT_FUND);
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepo.save(sender);
        accountRepo.save(receiver);

        Transaction transaction = new Transaction(
                senderVpa, receiverVpa, amount, packetHash, Instant.now());
        transactionRepo.save(transaction);

        return SettlementResult.success(transaction.getId());
    }
}
