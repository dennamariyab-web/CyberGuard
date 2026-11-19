-- =======================================================
-- CyberGuard - Security Operations Center (SOC) Database
-- Database: cyberguard_soc
-- =======================================================

CREATE DATABASE IF NOT EXISTS cyberguard_soc;
USE cyberguard_soc;

-- Table: admin
-- Stores administrative users authorized to access the SOC dashboard.
CREATE TABLE IF NOT EXISTS admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) DEFAULT 'SOC Analyst',
    last_login DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: security_events
-- Stores real-time monitored authentication & system security events.
CREATE TABLE IF NOT EXISTS security_events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_time DATETIME NOT NULL,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'SUCCESS', 'FAILED', 'BLOCKED', 'SUSPICIOUS'
    severity VARCHAR(20) NOT NULL, -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    details TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: alerts
-- Stores defensive alerts triggered by the threat detection rule engine.
CREATE TABLE IF NOT EXISTS alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    threat_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    detection_time DATETIME NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'UNDER_INVESTIGATION', 'RESOLVED'
    description TEXT NOT NULL,
    resolved_at DATETIME NULL,
    resolved_by VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default Admin Credential:
-- Username: admin
-- Password: password123 (or admin123, seeded programmatically with dynamic salt)
