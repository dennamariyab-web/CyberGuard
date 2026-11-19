package com.cyberguard.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * JDBC Database Connection Manager for CyberGuard SOC.
 * Handles MySQL connection lifecycle, schema initialization, and graceful
 * fallback if MySQL service is temporarily offline.
 */
public class DBConnection {

    private static String dbHost = "localhost";
    private static int dbPort = 3306;
    private static String dbName = "cyberguard_soc";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    private static boolean useSSL = false;
    private static boolean allowPublicKeyRetrieval = true;
    private static String serverTimezone = "UTC";

    private static Connection connection = null;
    private static boolean isConnected = false;
    private static boolean checkedConnection = false;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream in = DBConnection.class.getResourceAsStream("/database/db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                dbHost = props.getProperty("db.host", dbHost);
                dbPort = Integer.parseInt(props.getProperty("db.port", String.valueOf(dbPort)));
                dbName = props.getProperty("db.name", dbName);
                dbUser = props.getProperty("db.user", dbUser);
                dbPassword = props.getProperty("db.password", dbPassword);
                useSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", "false"));
                allowPublicKeyRetrieval = Boolean.parseBoolean(props.getProperty("db.allowPublicKeyRetrieval", "true"));
                serverTimezone = props.getProperty("db.serverTimezone", "UTC");
            }
        } catch (Exception e) {
            System.err.println("[CyberGuard DB] Notice: Using default database configuration (" + e.getMessage() + ")");
        }
    }

    /**
     * Obtains an active JDBC Connection to MySQL.
     * Reconnects automatically if the previous connection was closed.
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            // Register MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s",
                    dbHost, dbPort, dbName, useSSL, allowPublicKeyRetrieval, serverTimezone
            );

            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            isConnected = true;
            checkedConnection = true;
            System.out.println("[CyberGuard DB] Successfully connected to MySQL database: " + dbName);
            return connection;
        } catch (Exception e) {
            isConnected = false;
            checkedConnection = true;
            // Silent debug notice - application falls back smoothly
            return null;
        }
    }

    /**
     * Checks if the MySQL database is reachable.
     */
    public static boolean isConnected() {
        if (!checkedConnection) {
            getConnection();
        }
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Attempts to create the database and tables automatically if MySQL is active.
     */
    public static void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String serverUrl = String.format(
                    "jdbc:mysql://%s:%d/?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s",
                    dbHost, dbPort, useSSL, allowPublicKeyRetrieval, serverTimezone
            );

            try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                System.out.println("[CyberGuard DB] Verified database existence: " + dbName);
            }

            // Now connect directly and create tables
            Connection appConn = getConnection();
            if (appConn != null) {
                try (Statement stmt = appConn.createStatement()) {
                    // Create admin table
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS admin (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "username VARCHAR(50) NOT NULL UNIQUE, " +
                            "password_hash VARCHAR(255) NOT NULL, " +
                            "salt VARCHAR(100) NOT NULL, " +
                            "full_name VARCHAR(100) NOT NULL, " +
                            "role VARCHAR(50) DEFAULT 'SOC Analyst', " +
                            "last_login DATETIME NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                    );

                    // Create security_events table
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS security_events (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "event_time DATETIME NOT NULL, " +
                            "username VARCHAR(50) NOT NULL, " +
                            "ip_address VARCHAR(45) NOT NULL, " +
                            "event_type VARCHAR(100) NOT NULL, " +
                            "status VARCHAR(20) NOT NULL, " +
                            "severity VARCHAR(20) NOT NULL, " +
                            "details TEXT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                    );

                    // Create alerts table
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS alerts (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "ip_address VARCHAR(45) NOT NULL, " +
                            "threat_type VARCHAR(100) NOT NULL, " +
                            "severity VARCHAR(20) NOT NULL, " +
                            "detection_time DATETIME NOT NULL, " +
                            "status VARCHAR(30) DEFAULT 'ACTIVE', " +
                            "description TEXT NOT NULL, " +
                            "resolved_at DATETIME NULL, " +
                            "resolved_by VARCHAR(50) NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                    );
                    System.out.println("[CyberGuard DB] Tables initialized successfully.");
                }
            }
        } catch (Exception e) {
            System.out.println("[CyberGuard DB] Running in Standby Memory Mode. (" + e.getMessage() + ")");
        }
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getDbHost() {
        return dbHost;
    }

    public static int getDbPort() {
        return dbPort;
    }
}
