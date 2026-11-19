package com.cyberguard.database;

import com.cyberguard.model.Admin;
import com.cyberguard.model.Alert;
import com.cyberguard.model.SecurityEvent;
import com.cyberguard.util.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object (DAO) and Database Manager for CyberGuard SOC.
 * Executes secure SQL statements using PreparedStatements.
 * Seamlessly caches and syncs with in-memory storage if MySQL is unreachable.
 */
public class DatabaseManager {

    private static final List<Admin> memoryAdmins = Collections.synchronizedList(new ArrayList<>());
    private static final List<SecurityEvent> memoryEvents = Collections.synchronizedList(new ArrayList<>());
    private static final List<Alert> memoryAlerts = Collections.synchronizedList(new ArrayList<>());
    private static int nextEventId = 1001;
    private static int nextAlertId = 5001;

    static {
        seedDefaultAdmin();
        seedInitialSampleData();
    }

    /**
     * Seeds default administrator in memory and database if not present.
     */
    private static void seedDefaultAdmin() {
        String defaultUsername = "admin";
        String defaultPassword = "password123";
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(defaultPassword, salt);

        Admin admin = new Admin(1, defaultUsername, hash, salt, "Chief Security Officer", "SOC Administrator", LocalDateTime.now());
        memoryAdmins.add(admin);

        // Also seed into MySQL if available
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String checkSql = "SELECT COUNT(*) FROM admin WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, defaultUsername);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO admin (username, password_hash, salt, full_name, role) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, defaultUsername);
                        insertStmt.setString(2, hash);
                        insertStmt.setString(3, salt);
                        insertStmt.setString(4, "Chief Security Officer");
                        insertStmt.setString(5, "SOC Administrator");
                        insertStmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] Notice seeding admin: " + e.getMessage());
            }
        }
    }

    /**
     * Seeds initial sample security events and alerts so the dashboard looks complete upon initial launch.
     */
    private static void seedInitialSampleData() {
        LocalDateTime now = LocalDateTime.now();

        // Sample initial events
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(45), "jsmith", "192.168.1.45", "User Authentication", "SUCCESS", "LOW", "Successful MFA login via corporate portal"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(38), "sec_admin", "10.0.0.12", "Admin Portal Access", "SUCCESS", "LOW", "Admin session established"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(25), "unknown_user", "198.51.100.24", "SSH Login Attempt", "FAILED", "MEDIUM", "Password mismatch on port 22"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(22), "unknown_user", "198.51.100.24", "SSH Login Attempt", "FAILED", "MEDIUM", "Invalid credentials repeat attempt"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(20), "unknown_user", "198.51.100.24", "SSH Login Attempt", "FAILED", "MEDIUM", "Failed login streak"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(18), "unknown_user", "198.51.100.24", "SSH Login Attempt", "FAILED", "MEDIUM", "Failed login attempt 4"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(17), "unknown_user", "198.51.100.24", "Brute Force Attack Detected", "SUSPICIOUS", "HIGH", "Exceeded threshold of 4 failed attempts"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(10), "rwilson", "192.168.1.110", "VPN Connection", "SUCCESS", "LOW", "Remote VPN authenticated"));
        addSecurityEvent(new SecurityEvent(nextEventId++, now.minusMinutes(5), "alex_dev", "10.0.2.88", "API Token Auth", "SUCCESS", "LOW", "OAuth token refresh validated"));

        // Sample initial alerts matching design
        addAlert(new Alert(nextAlertId++, "198.51.100.24", "Multiple Failed Login Attempts", "HIGH", now.minusMinutes(17), "ACTIVE", "IP produced >4 failed authentications within 10 minutes"));
        addAlert(new Alert(nextAlertId++, "203.0.113.88", "Brute Force Attack Sequence", "HIGH", now.minusMinutes(35), "UNDER_INVESTIGATION", "Rapid sequential dictionary attack detected"));
        addAlert(new Alert(nextAlertId++, "10.1.20.25", "Suspicious File Transfer Trigger", "MEDIUM", now.minusHours(1), "RESOLVED", "Large outbound encrypted data transfer observed"));
        addAlert(new Alert(nextAlertId++, "192.255.150.140", "Phishing Link Credential Harvest Attempt", "HIGH", now.minusHours(2), "ACTIVE", "Known malicious domain referrer flagged"));
        addAlert(new Alert(nextAlertId++, "172.16.8.12", "Unusual Network Activity Window", "LOW", now.minusHours(4), "RESOLVED", "Off-hours internal subnet scan detected"));
    }

    public static Admin getAdminByUsername(String username) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT id, username, password_hash, salt, full_name, role, last_login FROM admin WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Timestamp ts = rs.getTimestamp("last_login");
                        LocalDateTime lastLogin = (ts != null) ? ts.toLocalDateTime() : null;
                        return new Admin(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getString("salt"),
                                rs.getString("full_name"),
                                rs.getString("role"),
                                lastLogin
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error fetching admin: " + e.getMessage());
            }
        }

        // Memory fallback
        for (Admin a : memoryAdmins) {
            if (a.getUsername().equalsIgnoreCase(username)) {
                return a;
            }
        }
        return null;
    }

    public static void updateAdminLastLogin(String username) {
        LocalDateTime now = LocalDateTime.now();
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "UPDATE admin SET last_login = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(now));
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error updating last login: " + e.getMessage());
            }
        }

        for (Admin a : memoryAdmins) {
            if (a.getUsername().equalsIgnoreCase(username)) {
                a.setLastLogin(now);
                break;
            }
        }
    }

    public static synchronized void addSecurityEvent(SecurityEvent event) {
        if (event.getId() <= 0) {
            event.setId(nextEventId++);
        }
        memoryEvents.add(0, event); // Add to top for real-time visibility

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO security_events (event_time, username, ip_address, event_type, status, severity, details) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(event.getTimestamp()));
                pstmt.setString(2, event.getUsername());
                pstmt.setString(3, event.getIpAddress());
                pstmt.setString(4, event.getEventType());
                pstmt.setString(5, event.getStatus());
                pstmt.setString(6, event.getSeverity());
                pstmt.setString(7, event.getDetails());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error inserting security event: " + e.getMessage());
            }
        }
    }

    public static synchronized List<SecurityEvent> getAllSecurityEvents() {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            List<SecurityEvent> list = new ArrayList<>();
            String sql = "SELECT id, event_time, username, ip_address, event_type, status, severity, details FROM security_events ORDER BY event_time DESC LIMIT 200";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SecurityEvent(
                            rs.getInt("id"),
                            rs.getTimestamp("event_time").toLocalDateTime(),
                            rs.getString("username"),
                            rs.getString("ip_address"),
                            rs.getString("event_type"),
                            rs.getString("status"),
                            rs.getString("severity"),
                            rs.getString("details")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error fetching security events: " + e.getMessage());
            }
        }

        return new ArrayList<>(memoryEvents);
    }

    public static synchronized void addAlert(Alert alert) {
        if (alert.getId() <= 0) {
            alert.setId(nextAlertId++);
        }
        memoryAlerts.add(0, alert);

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO alerts (ip_address, threat_type, severity, detection_time, status, description) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, alert.getIpAddress());
                pstmt.setString(2, alert.getThreatType());
                pstmt.setString(3, alert.getSeverity());
                pstmt.setTimestamp(4, Timestamp.valueOf(alert.getDetectionTime()));
                pstmt.setString(5, alert.getStatus());
                pstmt.setString(6, alert.getDescription());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error inserting alert: " + e.getMessage());
            }
        }
    }

    public static synchronized List<Alert> getAllAlerts() {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            List<Alert> list = new ArrayList<>();
            String sql = "SELECT id, ip_address, threat_type, severity, detection_time, status, description, resolved_by, resolved_at FROM alerts ORDER BY detection_time DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Alert a = new Alert(
                            rs.getInt("id"),
                            rs.getString("ip_address"),
                            rs.getString("threat_type"),
                            rs.getString("severity"),
                            rs.getTimestamp("detection_time").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getString("description")
                    );
                    a.setResolvedBy(rs.getString("resolved_by"));
                    Timestamp rat = rs.getTimestamp("resolved_at");
                    if (rat != null) {
                        a.setResolvedAt(rat.toLocalDateTime());
                    }
                    list.add(a);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error fetching alerts: " + e.getMessage());
            }
        }

        return new ArrayList<>(memoryAlerts);
    }

    public static synchronized boolean updateAlertStatus(int alertId, String newStatus, String resolvedBy) {
        LocalDateTime now = LocalDateTime.now();
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "UPDATE alerts SET status = ?, resolved_by = ?, resolved_at = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, resolvedBy);
                pstmt.setTimestamp(3, Timestamp.valueOf(now));
                pstmt.setInt(4, alertId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[CyberGuard DB] SQL Error updating alert status: " + e.getMessage());
            }
        }

        for (Alert a : memoryAlerts) {
            if (a.getId() == alertId) {
                a.setStatus(newStatus);
                a.setResolvedBy(resolvedBy);
                a.setResolvedAt(now);
                return true;
            }
        }
        return false;
    }
}
