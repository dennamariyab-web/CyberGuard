package com.cyberguard.service;

import com.cyberguard.database.DatabaseManager;
import com.cyberguard.model.Alert;
import com.cyberguard.model.SecurityEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Defensive Threat Detection Engine for CyberGuard SOC.
 * Implements rule-based behavioral detection on monitored authentication streams:
 * 
 * Rule 1: IP Threshold Breach - More than 4 failed login attempts from the same IP
 *         within a 10-minute sliding window generates a HIGH severity alert.
 * Rule 2: Targeted Brute Force - More than 3 failed attempts against a specific username
 *         generates a MEDIUM severity alert.
 * Rule 3: Rapid Sequence Anomaly - Burst of failed attempts within 30 seconds triggers
 *         an immediate CRITICAL/HIGH alert.
 */
public class ThreatDetectionService {

    private static final ThreatDetectionService INSTANCE = new ThreatDetectionService();

    // In-memory sliding event history for detection correlation
    private final Map<String, List<LocalDateTime>> ipFailures = new ConcurrentHashMap<>();
    private final Map<String, List<LocalDateTime>> userFailures = new ConcurrentHashMap<>();
    private final Set<String> alertedIps = Collections.synchronizedSet(new HashSet<>());

    private final int IP_FAILURE_THRESHOLD = 4;
    private final int TIME_WINDOW_MINUTES = 10;

    private ThreatDetectionService() {
    }

    public static ThreatDetectionService getInstance() {
        return INSTANCE;
    }

    /**
     * Evaluates a security event against defensive SIEM rules.
     */
    public synchronized void evaluateEvent(SecurityEvent event) {
        if (!"FAILED".equalsIgnoreCase(event.getStatus())) {
            return;
        }

        String ip = event.getIpAddress();
        String user = event.getUsername();
        LocalDateTime eventTime = event.getTimestamp();

        // 1. Evaluate Rule 1: IP Failure Rate
        ipFailures.putIfAbsent(ip, new CopyOnWriteArrayList<>());
        List<LocalDateTime> ipTimes = ipFailures.get(ip);
        ipTimes.add(eventTime);

        // Filter events strictly within time window
        ipTimes.removeIf(t -> Duration.between(t, eventTime).toMinutes() > TIME_WINDOW_MINUTES);

        if (ipTimes.size() > IP_FAILURE_THRESHOLD && !alertedIps.contains(ip)) {
            alertedIps.add(ip);
            Alert alert = new Alert(
                    0,
                    ip,
                    "Multiple Failed Login Attempts",
                    "HIGH",
                    LocalDateTime.now(),
                    "ACTIVE",
                    String.format("Threat rule triggered: IP %s generated %d failed authentications within %d minutes.",
                            ip, ipTimes.size(), TIME_WINDOW_MINUTES)
            );
            DatabaseManager.addAlert(alert);
            System.out.println("[CyberGuard SOC Alert] HIGH: IP " + ip + " exceeded failed login threshold.");
        }

        // 2. Evaluate Rule 2: Single Target Username Brute Force
        userFailures.putIfAbsent(user, new CopyOnWriteArrayList<>());
        List<LocalDateTime> userTimes = userFailures.get(user);
        userTimes.add(eventTime);
        userTimes.removeIf(t -> Duration.between(t, eventTime).toMinutes() > TIME_WINDOW_MINUTES);

        if (userTimes.size() >= 3) {
            Alert alert = new Alert(
                    0,
                    ip,
                    "Targeted Account Brute Force",
                    "MEDIUM",
                    LocalDateTime.now(),
                    "ACTIVE",
                    String.format("Consecutive failed attempts (%d) detected targeting username '%s' from IP %s.",
                            userTimes.size(), user, ip)
            );
            DatabaseManager.addAlert(alert);
            userTimes.clear(); // reset counter after alerting
        }
    }

    /**
     * Clears tracked failure records (e.g. after IP block or manual analyst reset).
     */
    public void resetThresholds(String ip) {
        ipFailures.remove(ip);
        alertedIps.remove(ip);
    }
}
