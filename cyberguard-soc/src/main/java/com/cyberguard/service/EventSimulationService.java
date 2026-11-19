package com.cyberguard.service;

import com.cyberguard.database.DatabaseManager;
import com.cyberguard.model.SecurityEvent;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service to simulate realistic security events for educational and testing demonstrations.
 * Generates local simulated authentication streams without any external network probing.
 */
public class EventSimulationService {

    private static final Random RANDOM = new Random();

    private static final String[] USERNAMES = {
            "root", "admin", "sec_officer", "jdoe", "analyst_clara", "guest", "db_backup", "test_user"
    };

    private static final String[] ATTACK_IPS = {
            "192.168.10.88", "10.14.2.99", "172.16.50.4", "192.168.1.150", "10.0.12.7"
    };

    private static final String[] BENIGN_IPS = {
            "192.168.1.102", "192.168.1.105", "10.0.5.21", "127.0.0.1", "192.168.2.14"
    };

    /**
     * Simulates a single benign successful login event.
     */
    public static SecurityEvent simulateSuccessfulLogin() {
        String user = USERNAMES[RANDOM.nextInt(USERNAMES.length)];
        String ip = BENIGN_IPS[RANDOM.nextInt(BENIGN_IPS.length)];
        SecurityEvent event = new SecurityEvent(
                0,
                LocalDateTime.now(),
                user,
                ip,
                "User Authentication [SIMULATED]",
                "SUCCESS",
                "LOW",
                "Routine successful multi-factor authentication (Simulated)"
        );
        DatabaseManager.addSecurityEvent(event);
        return event;
    }

    /**
     * Simulates a single isolated failed login attempt.
     */
    public static SecurityEvent simulateFailedLogin() {
        String user = USERNAMES[RANDOM.nextInt(USERNAMES.length)];
        String ip = BENIGN_IPS[RANDOM.nextInt(BENIGN_IPS.length)];
        SecurityEvent event = new SecurityEvent(
                0,
                LocalDateTime.now(),
                user,
                ip,
                "Password Authentication [SIMULATED]",
                "FAILED",
                "MEDIUM",
                "Single bad password attempt (Simulated)"
        );
        DatabaseManager.addSecurityEvent(event);
        ThreatDetectionService.getInstance().evaluateEvent(event);
        return event;
    }

    /**
     * Simulates a coordinated multi-attempt brute force attack from a single IP.
     * Generates 5 rapid failed attempts that immediately trigger Rule 1.
     */
    public static void simulateBruteForceAttack() {
        String attackerIp = ATTACK_IPS[RANDOM.nextInt(ATTACK_IPS.length)];
        String targetUser = USERNAMES[RANDOM.nextInt(USERNAMES.length)];

        for (int i = 1; i <= 5; i++) {
            SecurityEvent event = new SecurityEvent(
                    0,
                    LocalDateTime.now().minusSeconds((5 - i) * 3L),
                    targetUser,
                    attackerIp,
                    "Brute Force Probe #" + i + " [SIMULATED]",
                    "FAILED",
                    "HIGH",
                    "Simulated automated dictionary attack attempt " + i + "/5"
            );
            DatabaseManager.addSecurityEvent(event);
            ThreatDetectionService.getInstance().evaluateEvent(event);
        }
    }
}
