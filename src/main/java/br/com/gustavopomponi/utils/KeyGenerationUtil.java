package br.com.gustavopomponi.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class KeyGenerationUtil {

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
    }
}