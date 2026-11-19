package com.cyberguard.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class providing secure password hashing and verification.
 * 
 * In cybersecurity applications, passwords must NEVER be stored in plain text.
 * We use SHA-256 with a unique random cryptographic 'salt' per user to protect
 * against rainbow table and dictionary attacks.
 */
public class PasswordHasher {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a cryptographically strong 16-byte random salt, Base64-encoded.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plain password with a specific salt using SHA-256.
     *
     * @param password Plain-text password entered by user
     * @param salt     Cryptographic salt string
     * @return Hexadecimal or Base64 string representing the computed digest
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Combine salt and password to defeat pre-computed lookup tables
            String saltedInput = salt + password;
            byte[] hashBytes = digest.digest(saltedInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies whether an entered password matches an expected stored hash.
     *
     * @param enteredPassword The password provided at login
     * @param salt            The salt stored with the user record
     * @param expectedHash    The hashed password stored in the database
     * @return true if credentials match, false otherwise
     */
    public static boolean verifyPassword(String enteredPassword, String salt, String expectedHash) {
        if (enteredPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String calculatedHash = hashPassword(enteredPassword, salt);
        return calculatedHash.equals(expectedHash);
    }
}
