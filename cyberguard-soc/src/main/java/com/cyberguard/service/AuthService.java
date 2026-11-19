package com.cyberguard.service;

import com.cyberguard.database.DatabaseManager;
import com.cyberguard.model.Admin;
import com.cyberguard.model.SecurityEvent;
import com.cyberguard.util.PasswordHasher;

import java.time.LocalDateTime;

/**
 * Authentication service handling login validation, password verification,
 * and security event logging for administrative access.
 */
public class AuthService {

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final Admin admin;

        public AuthResult(boolean success, String message, Admin admin) {
            this.success = success;
            this.message = message;
            this.admin = admin;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Admin getAdmin() {
            return admin;
        }
    }

    /**
     * Authenticates an administrator against stored credentials.
     * Generates and logs a SecurityEvent in real-time.
     *
     * @param username The entered username
     * @param password The entered plain text password
     * @param clientIp The source IP address of the login attempt
     * @return AuthResult containing status, message, and Admin object if successful
     */
    public AuthResult login(String username, String password, String clientIp) {
        if (clientIp == null || clientIp.trim().isEmpty()) {
            clientIp = "127.0.0.1";
        }

        // Input validation
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username cannot be empty", null);
        }

        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password cannot be empty", null);
        }

        username = username.trim();

        // Retrieve admin from database/cache
        Admin admin = DatabaseManager.getAdminByUsername(username);

        if (admin == null) {
            // Log security failure for non-existent account
            recordLoginEvent(username, clientIp, false, "Invalid username attempted: account not found");
            return new AuthResult(false, "Invalid administrator credentials.", null);
        }

        // Verify cryptographically salted hash
        boolean passwordMatches = PasswordHasher.verifyPassword(password, admin.getSalt(), admin.getPasswordHash());

        if (!passwordMatches) {
            // Log security failure
            recordLoginEvent(username, clientIp, false, "Incorrect password attempt for admin: " + username);
            return new AuthResult(false, "Invalid administrator credentials.", null);
        }

        // Successful authentication
        DatabaseManager.updateAdminLastLogin(username);
        recordLoginEvent(username, clientIp, true, "Successful administrative login via SOC console");

        return new AuthResult(true, "Authentication successful. Welcome, " + admin.getFullName() + ".", admin);
    }

    private void recordLoginEvent(String username, String ip, boolean success, String details) {
        SecurityEvent event = new SecurityEvent(
                0,
                LocalDateTime.now(),
                username,
                ip,
                success ? "Admin Portal Login" : "Failed Authentication Attempt",
                success ? "SUCCESS" : "FAILED",
                success ? "LOW" : "MEDIUM",
                details
        );
        DatabaseManager.addSecurityEvent(event);

        // Notify threat detection service of failed attempt
        if (!success) {
            ThreatDetectionService.getInstance().evaluateEvent(event);
        }
    }
}
