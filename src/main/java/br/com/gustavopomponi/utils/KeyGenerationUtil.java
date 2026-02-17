package br.com.gustavopomponi.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerationUtil {

    /**
     * Generates a JWT secret key suitable for HS256 and similar HMAC algorithms.
     * Uses 256 bits (32 bytes) of cryptographically secure random data.
     *
     * @return Base64 encoded JWT secret
     */
    public static String generateJwtSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] jwtSecret = new byte[32]; // 256 bits for HS256
        secureRandom.nextBytes(jwtSecret);
        return Base64.getEncoder().encodeToString(jwtSecret);
    }

    public static void main(String[] args) throws Exception {
        // Generate AES-256 key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("=== ENCRYPTION KEY (Store Securely) ===");
        System.out.println(encodedKey);
        System.out.println("\nAdd this to your environment variables:");
        System.out.println("export ENCRYPTION_KEY=" + encodedKey);
        System.out.println("\nWARNING: NEVER commit this key to version control!");
        System.out.println("Store in: AWS KMS, Azure Key Vault, or HashiCorp Vault");

        // Generate JWT Secret
        String jwtSecret = generateJwtSecret();
        System.out.println("\n=== JWT SECRET (Store Securely) ===");
        System.out.println(jwtSecret);
        System.out.println("\nAdd this to your environment variables:");
        System.out.println("export JWT_SECRET=" + jwtSecret);
        System.out.println("\nWARNING: NEVER commit this secret to version control!");
        System.out.println("Store in: AWS KMS, Azure Key Vault, or HashiCorp Vault");
    }
}