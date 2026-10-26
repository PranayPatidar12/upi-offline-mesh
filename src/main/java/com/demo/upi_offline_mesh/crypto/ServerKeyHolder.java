package com.demo.upi_offline_mesh.crypto;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.security.*;

@Component
@Getter
public class ServerKeyHolder {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public ServerKeyHolder() throws NoSuchAlgorithmException{
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize((2048));
        KeyPair keyPair = generator.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

}
