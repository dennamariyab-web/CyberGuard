package com.cyberguard.model;

import java.time.LocalDateTime;

/**
 * Model representing an authentication or system security event monitored by CyberGuard.
 */
public class SecurityEvent {
    private int id;
    private LocalDateTime timestamp;
    private String username;
    private String ipAddress;
    private String eventType;
    private String status;     // SUCCESS, FAILED, SUSPICIOUS, BLOCKED
    private String severity;   // LOW, MEDIUM, HIGH, CRITICAL
    private String details;

    public SecurityEvent() {
    }

    public SecurityEvent(int id, LocalDateTime timestamp, String username, String ipAddress,
                         String eventType, String status, String severity, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.username = username;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.status = status;
        this.severity = severity;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
