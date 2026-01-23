package it.unisannio.chat.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {

    private static final int GCM_TAG_LENGTH = 16; // in byte (128 bit)
    private static final int IV_LENGTH = 12; // 96 bit, raccomandato per GCM
    private static CryptoUtil INSTANCE; // singleton
    private final SecretKey key;

    private CryptoUtil(SecretKey key) {
        this.key = key;
    }

    /**
     * Inizializza l'istanza singleton con una chiave casuale o fornita.
     */
    public static CryptoUtil getInstance() {
        if (INSTANCE == null) {
            try {
                SecretKey generatedKey = generateKey();
                INSTANCE = new CryptoUtil(generatedKey);
            } catch (Exception e) {
                throw new RuntimeException("Error initializing CryptoUtil", e);
            }
        }
        return INSTANCE;
    }

    /**
     * Genera una chiave AES a 256 bit.
     */
    private static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    /**
     * Cifra un testo in chiaro usando AES-GCM e restituisce Base64(iv + testo cifrato)
     */
    public String encrypt(String plaintext) {
        try {
            if (plaintext == null || plaintext.isEmpty()) return plaintext;

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    /**
     * Decifra un testo cifrato Base64(iv + testo cifrato)
     */
    public String decrypt(String encrypted) {
        try {
            if (encrypted == null || encrypted.isEmpty()) return encrypted;

            byte[] combined = Base64.getDecoder().decode(encrypted);
            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting", e);
        }
    }

}