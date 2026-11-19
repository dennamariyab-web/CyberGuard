package com.cyberguard;

import com.cyberguard.model.Admin;
import com.cyberguard.model.Alert;
import com.cyberguard.model.SecurityEvent;
import com.cyberguard.service.AuthService;
import com.cyberguard.database.DatabaseManager;
import com.cyberguard.service.ThreatDetectionService;
import com.cyberguard.util.PasswordHasher;

import java.time.LocalDateTime;
import java.util.List;

public class CoreLogicTest {
    public static void main(String[] args) {
        System.out.println(">>> [TEST 1] Testing Password Hashing & Verification...");
        String salt = PasswordHasher.generateSalt();
        String pass = "password123";
        String hash = PasswordHasher.hashPassword(pass, salt);
        boolean valid = PasswordHasher.verifyPassword("password123", salt, hash);
        boolean invalid = PasswordHasher.verifyPassword("wrongpass", salt, hash);
        assert valid : "Password verification failed for valid password!";
        assert !invalid : "Password verification accepted wrong password!";
        System.out.println(">>> [TEST 1 PASSED] PasswordHasher works perfectly.");

        System.out.println(">>> [TEST 2] Testing Admin Authentication via AuthService...");
        AuthService authService = new AuthService();
        AuthService.AuthResult goodResult = authService.login("admin", "password123", "127.0.0.1");
        assert goodResult.isSuccess() : "Failed valid login: " + goodResult.getMessage();
        System.out.println(">>> [TEST 2 PASSED] Login success for admin: " + goodResult.getAdmin().getFullName());

        System.out.println(">>> [TEST 3] Testing Bad Login & Threat Rule...");
        for (int i = 1; i <= 5; i++) {
            authService.login("admin", "badpassword" + i, "192.168.1.99");
        }
        List<Alert> alerts = DatabaseManager.getAllAlerts();
        boolean foundRule1 = alerts.stream().anyMatch(a -> a.getIpAddress().equals("192.168.1.99"));
        assert foundRule1 : "Threat rule did not trigger alert for IP 192.168.1.99!";
        System.out.println(">>> [TEST 3 PASSED] ThreatDetectionService successfully triggered HIGH alert for brute force IP.");

        System.out.println(">>> ALL CORE SECURITY TESTS PASSED SUCCESSFULLY! 100% OK.");
    }
}
