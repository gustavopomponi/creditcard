package br.com.gustavopomponi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

    // CRITICAL: In production, use AWS KMS, Azure Key Vault, or HashiCorp Vault
    @Value("${encryption.key}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_SIZE = 256; // AES-256 required by PCI DSS

    /**
     * Encrypts PAN (Primary Account Number) using AES-256-GCM
     * PCI DSS Requirement 3.4: Render PAN unreadable anywhere it is stored
     */
    public String encryptPAN(String pan) throws Exception {
        if (pan == null || pan.isEmpty()) {
            throw new IllegalArgumentException("PAN cannot be null or empty");
        }

        // Remove any spaces or dashes
        String cleanPan = pan.replaceAll("[\\s-]", "");

        // Validate PAN format (13-19 digits)
        if (!cleanPan.matches("^[0-9]{13,19}$")) {
            throw new IllegalArgumentException("Invalid PAN format");
        }

        try {
            SecretKey key = getKeyFromString();
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // Generate cryptographically secure random IV
            byte[] iv = generateSecureIV();

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] encryptedData = cipher.doFinal(cleanPan.getBytes());

            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            logger.info("PAN encrypted successfully");
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            logger.error("Failed to encrypt PAN", e);
            throw new Exception("Encryption failed", e);
        }
    }

    /**
     * Decrypts PAN
     * PCI DSS Requirement 3.4: Access to decrypted PANs must be restricted
     */
    public String decryptPAN(String encryptedPan) throws Exception {
        if (encryptedPan == null || encryptedPan.isEmpty()) {
            throw new IllegalArgumentException("Encrypted PAN cannot be null or empty");
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedPan);

            if (combined.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted data");
            }

            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            SecretKey key = getKeyFromString();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encrypted);

            logger.warn("PAN decrypted - Access should be audited");
            return new String(decryptedData);

        } catch (Exception e) {
            logger.error("Failed to decrypt PAN", e);
            throw new Exception("Decryption failed", e);
        }
    }

    /**
     * Masks PAN for display (PCI DSS Requirement 3.3)
     * Shows only first 6 and last 4 digits
     */
    public String maskPAN(String pan) {
        if (pan == null || pan.length() < 13) {
            return "****";
        }

        String cleanPan = pan.replaceAll("[\\s-]", "");

        if (cleanPan.length() < 13) {
            return "****";
        }

        // Show first 6 and last 4 (BIN and last 4)
        String first6 = cleanPan.substring(0, 6);
        String last4 = cleanPan.substring(cleanPan.length() - 4);
        int maskedLength = cleanPan.length() - 10;

        return first6 + "*".repeat(maskedLength) + last4;
    }

    private SecretKey getKeyFromString() throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);

        if (decodedKey.length != 32) { // 256 bits = 32 bytes
            throw new IllegalStateException("Invalid key size. Must be 256 bits (32 bytes)");
        }

        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    private byte[] generateSecureIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}