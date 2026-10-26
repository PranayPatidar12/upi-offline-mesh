package com.demo.upi_offline_mesh.service;

import com.demo.upi_offline_mesh.crypto.HybridCryptoService;
import com.demo.upi_offline_mesh.model.MeshPacket;
import com.demo.upi_offline_mesh.model.PaymentInstruction;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class BridgeIngestionService {
    private static final Duration MAX_PACKET_AGE = Duration.ofHours(24);

    private final HybridCryptoService cryptoService;
    private final IdempotencyService idempotencyService;
    private final SettlementService settlementService;

    public BridgeIngestionService(HybridCryptoService cryptoService, IdempotencyService idempotencyService, SettlementService settlementService){
        this.cryptoService = cryptoService;
        this.idempotencyService = idempotencyService;
        this.settlementService = settlementService;
    }

    public IngestionResult ingest(MeshPacket packet) {
        String packetHash;
        try {
            packetHash = cryptoService.hash(packet.getCipherText());
        } catch (Exception e) {
            return IngestionResult.invalid("unknown", "failed to hash packet");
        }

        boolean claimed = idempotencyService.claim(packetHash);
        if(!claimed){
            return IngestionResult.duplicate(packetHash);
        }

        PaymentInstruction instruction;
        try{
            instruction = cryptoService.decrypt(packet.getCipherText());
        } catch(Exception e){
           return IngestionResult.invalid(packetHash,"Decryption Failed or tempered cipherText");
        }

        Instant signedAt = Instant.ofEpochMilli(instruction.getSignedAt());
        Duration age = Duration.between(signedAt, Instant.now());
        if (age.compareTo(MAX_PACKET_AGE) > 0) {
            return IngestionResult.invalid(packetHash, "Packet expired (older than 24h)");
        }

        SettlementResult settlementResult = settlementService.settle(
                instruction.getSenderVpa(),
                instruction.getReceiverVpa(),
                instruction.getAmount(),
                packetHash
        );

        if(settlementResult.outcome()!=SettlementOutcome.SUCCESS){
            return IngestionResult.invalid(packetHash, settlementResult.outcome().toString());
        }

        return IngestionResult.settled(packetHash,settlementResult.TransactionId());









    }




}
