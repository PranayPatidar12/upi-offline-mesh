package com.demo.upi_offline_mesh.cypto;
import com.demo.upi_offline_mesh.crypto.HybridCryptoService;
import com.demo.upi_offline_mesh.model.PaymentInstruction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class HybridCryptoServiceTest {
    @Autowired
    private HybridCryptoService cryptoService;

    @Test
    void encryptDecryptRoundTrip() throws Exception{
        PaymentInstruction original = new PaymentInstruction(
                "alice@upi",
                "bob@upi",
                new BigDecimal("500.00"),
                "1234hashed",
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        String cipherText = cryptoService.encrypt(original);
        PaymentInstruction decrypted = cryptoService.decrypt(cipherText);

        assertEquals(original.getSenderVpa(), decrypted.getSenderVpa());
        assertEquals(original.getReceiverVpa(), decrypted.getReceiverVpa());
        assertEquals(original.getAmount(), decrypted.getAmount());
        assertEquals(original.getNonce(), decrypted.getNonce());

    }



}
