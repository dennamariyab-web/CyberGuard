package com.cyberguard.model;

import java.time.LocalDateTime;

/**
 * Model representing an authenticated Security Operations Center Administrator.
 * Stores administrator credentials, security role, and session tracking information.
 */
public class Admin {
    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String role;
    private LocalDateTime lastLogin;

    public Admin() {
    }

    public Admin(int id, String username, String passwordHash, String salt, String fullName, String role, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.role = role;
        this.lastLogin = lastLogin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
