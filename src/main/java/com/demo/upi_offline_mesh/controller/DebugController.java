package com.demo.upi_offline_mesh.controller;

import com.demo.upi_offline_mesh.crypto.HybridCryptoService;
import com.demo.upi_offline_mesh.model.MeshPacket;
import com.demo.upi_offline_mesh.model.PaymentInstruction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class DebugController {

    private final HybridCryptoService cryptoService;

    public DebugController(HybridCryptoService cryptoService){
        this.cryptoService = cryptoService;
    }
    @GetMapping("/api/debug/make-packet")
    public MeshPacket makePacket(@RequestParam String sender,
                                 @RequestParam String receiver,
                                 @RequestParam BigDecimal amount) throws Exception{
        PaymentInstruction instruction = new PaymentInstruction(sender, receiver, amount, "dummyPinHash",
                UUID.randomUUID().toString(), System.currentTimeMillis());

        String cipherText = cryptoService.encrypt(instruction);
        return new MeshPacket(UUID.randomUUID().toString(), 5, System.currentTimeMillis(), cipherText);
    }

}
