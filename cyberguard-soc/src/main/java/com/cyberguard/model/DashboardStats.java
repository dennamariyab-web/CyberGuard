package com.cyberguard.model;

/**
 * Encapsulates live aggregated SOC metrics for the top dashboard metric cards.
 */
public class DashboardStats {
    private int totalAttempts;
    private int successfulLogins;
    private int failedLogins;
    private int suspiciousActivities;
    private int criticalAlerts;
    private int highAlerts;
    private int mediumAlerts;
    private int lowAlerts;

    public DashboardStats() {
    }

    public DashboardStats(int totalAttempts, int successfulLogins, int failedLogins, int suspiciousActivities) {
        this.totalAttempts = totalAttempts;
        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.suspiciousActivities = suspiciousActivities;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getSuccessfulLogins() {
        return successfulLogins;
    }

    public void setSuccessfulLogins(int successfulLogins) {
        this.successfulLogins = successfulLogins;
    }

    public int getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(int failedLogins) {
        this.failedLogins = failedLogins;
    }

    public int getSuspiciousActivities() {
        return suspiciousActivities;
    }

    public void setSuspiciousActivities(int suspiciousActivities) {
        this.suspiciousActivities = suspiciousActivities;
    }

    public int getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(int criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public int getHighAlerts() {
        return highAlerts;
    }

    public void setHighAlerts(int highAlerts) {
        this.highAlerts = highAlerts;
    }

    public int getMediumAlerts() {
        return mediumAlerts;
    }

    public void setMediumAlerts(int mediumAlerts) {
        this.mediumAlerts = mediumAlerts;
    }

    public int getLowAlerts() {
        return lowAlerts;
    }

    public void setLowAlerts(int lowAlerts) {
        this.lowAlerts = lowAlerts;
    }
}
