package com.demo.upi_offline_mesh.crypto;
import com.demo.upi_offline_mesh.model.PaymentInstruction;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class HybridCryptoService {
    private static final int RSA_KEY_LENGTH_BYTES = 256;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final ServerKeyHolder keyHolder;
    private final ObjectMapper objectMapper;

    public HybridCryptoService(ServerKeyHolder keyHolder, ObjectMapper objectMapper) {
        this.keyHolder = keyHolder;
        this.objectMapper = objectMapper;
    }

    public String encrypt(PaymentInstruction instruction) throws Exception{

        //Serialize to JSON
        byte[] plainText = objectMapper.writeValueAsBytes(instruction);

        //AES Key Generator
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey aesKey = keyGenerator.generateKey();

        //Initialization Vector to avoid Data Tempering
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        //Encryption of aesCipher
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] aesCiphertext = aesCipher.doFinal(plainText);


        //Encryption of AES Itself
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, keyHolder.getPublicKey());
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());


        //Combined Both in one
        byte[] combined = new byte[encryptedAesKey.length + iv.length + aesCiphertext.length];
        System.arraycopy(encryptedAesKey, 0, combined, 0, encryptedAesKey.length);
        System.arraycopy(iv, 0, combined, encryptedAesKey.length, iv.length);
        System.arraycopy(aesCiphertext, 0, combined, encryptedAesKey.length + iv.length, aesCiphertext.length);

        return Base64.getEncoder().encodeToString(combined);

    }



    //Decryption
    public PaymentInstruction decrypt(String base64Ciphertext) throws Exception{
        byte[] combined = Base64.getDecoder().decode(base64Ciphertext);

        //Decrypt the AES Key
        byte[] encryptedAesKey = Arrays.copyOfRange(combined, 0, RSA_KEY_LENGTH_BYTES);
        byte[] iv = Arrays.copyOfRange(combined, RSA_KEY_LENGTH_BYTES, RSA_KEY_LENGTH_BYTES + GCM_IV_LENGTH_BYTES);
        byte[] aesCiphertext = Arrays.copyOfRange(combined, RSA_KEY_LENGTH_BYTES + GCM_IV_LENGTH_BYTES, combined.length);


        //Decrypt the AES Content
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, keyHolder.getPrivateKey());
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        //Bring Back to Java Object
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] plaintext = aesCipher.doFinal(aesCiphertext);

        return objectMapper.readValue(plaintext, PaymentInstruction.class);
    }

    //Hash is used for Idempotency. If the same packet is seen twice, its hash will be identical
    public String hash(String base64Ciphertext) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashbytes = digest.digest(base64Ciphertext.getBytes());
        StringBuilder hex = new StringBuilder();

        for(byte b: hashbytes){
            hex.append(String.format("%02x",b));
        }
        return hex.toString();
    }


}
