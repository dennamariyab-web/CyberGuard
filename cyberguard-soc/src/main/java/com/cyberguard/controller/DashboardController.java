package com.cyberguard.controller;

import com.cyberguard.database.DatabaseManager;
import com.cyberguard.model.Admin;
import com.cyberguard.model.Alert;
import com.cyberguard.model.DashboardStats;
import com.cyberguard.model.SecurityEvent;
import com.cyberguard.service.EventSimulationService;
import com.cyberguard.view.DashboardView;
import com.cyberguard.view.ViewNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller managing dashboard operations, data refresh cycles, event filtering,
 * simulation triggers, and alert resolution.
 */
public class DashboardController {

    private final DashboardView view;
    private final Admin currentAdmin;

    private final ObservableList<SecurityEvent> eventsMasterList = FXCollections.observableArrayList();
    private final ObservableList<Alert> alertsMasterList = FXCollections.observableArrayList();

    public DashboardController(DashboardView view, Admin currentAdmin) {
        this.view = view;
        this.currentAdmin = currentAdmin;
    }

    /**
     * Initializes initial data and triggers first metric recalculation.
     */
    public void initialize() {
        refreshAllData();
    }

    /**
     * Refreshes all security events, alerts, statistics and charts from the database.
     */
    public void refreshAllData() {
        List<SecurityEvent> events = DatabaseManager.getAllSecurityEvents();
        List<Alert> alerts = DatabaseManager.getAllAlerts();

        Platform.runLater(() -> {
            eventsMasterList.setAll(events);
            alertsMasterList.setAll(alerts);

            // Compute statistics
            DashboardStats stats = calculateStats(events, alerts);
            view.updateStats(stats);

            // Update tables and charts
            view.updateEventsTable(eventsMasterList);
            view.updateAlertsTable(alertsMasterList);
            view.updateCharts(events, alerts);
        });
    }

    private DashboardStats calculateStats(List<SecurityEvent> events, List<Alert> alerts) {
        int total = events.size();
        int success = 0;
        int failed = 0;
        int suspicious = 0;

        for (SecurityEvent e : events) {
            String s = e.getStatus();
            if ("SUCCESS".equalsIgnoreCase(s)) {
                success++;
            } else if ("FAILED".equalsIgnoreCase(s)) {
                failed++;
            } else if ("SUSPICIOUS".equalsIgnoreCase(s) || "BLOCKED".equalsIgnoreCase(s)) {
                suspicious++;
            }
        }

        DashboardStats stats = new DashboardStats(total, success, failed, suspicious);

        int crit = 0, high = 0, med = 0, low = 0;
        for (Alert a : alerts) {
            String sev = a.getSeverity();
            if ("CRITICAL".equalsIgnoreCase(sev)) crit++;
            else if ("HIGH".equalsIgnoreCase(sev)) high++;
            else if ("MEDIUM".equalsIgnoreCase(sev)) med++;
            else if ("LOW".equalsIgnoreCase(sev)) low++;
        }
        stats.setCriticalAlerts(crit);
        stats.setHighAlerts(high);
        stats.setMediumAlerts(med);
        stats.setLowAlerts(low);

        return stats;
    }

    /**
     * Filters security events based on keyword search and status dropdown.
     */
    public void filterEvents(String query, String statusFilter) {
        List<SecurityEvent> filtered = eventsMasterList.stream()
                .filter(e -> {
                    boolean matchesQuery = true;
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        matchesQuery = e.getUsername().toLowerCase().contains(q) ||
                                       e.getIpAddress().toLowerCase().contains(q) ||
                                       e.getEventType().toLowerCase().contains(q);
                    }

                    boolean matchesStatus = true;
                    if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
                        matchesStatus = e.getStatus().equalsIgnoreCase(statusFilter);
                    }

                    return matchesQuery && matchesStatus;
                })
                .collect(Collectors.toList());

        view.updateEventsTable(FXCollections.observableArrayList(filtered));
    }

    /**
     * Filters alerts by severity and keyword.
     */
    public void filterAlerts(String query, String severityFilter) {
        List<Alert> filtered = alertsMasterList.stream()
                .filter(a -> {
                    boolean matchesQuery = true;
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        matchesQuery = a.getIpAddress().toLowerCase().contains(q) ||
                                       a.getThreatType().toLowerCase().contains(q) ||
                                       a.getDescription().toLowerCase().contains(q);
                    }

                    boolean matchesSeverity = true;
                    if (severityFilter != null && !severityFilter.equalsIgnoreCase("ALL")) {
                        matchesSeverity = a.getSeverity().equalsIgnoreCase(severityFilter);
                    }

                    return matchesQuery && matchesSeverity;
                })
                .collect(Collectors.toList());

        view.updateAlertsTable(FXCollections.observableArrayList(filtered));
    }

    /**
     * Marks an active alert as resolved by the current administrator.
     */
    public void resolveAlert(Alert alert) {
        if (alert == null || "RESOLVED".equalsIgnoreCase(alert.getStatus())) {
            return;
        }

        String adminName = currentAdmin != null ? currentAdmin.getUsername() : "admin";
        boolean updated = DatabaseManager.updateAlertStatus(alert.getId(), "RESOLVED", adminName);
        if (updated) {
            alert.setStatus("RESOLVED");
            alert.setResolvedBy(adminName);
            refreshAllData();
        }
    }

    /**
     * Simulation Handlers
     */
    public void triggerNormalLoginSimulation() {
        EventSimulationService.simulateSuccessfulLogin();
        refreshAllData();
    }

    public void triggerFailedLoginSimulation() {
        EventSimulationService.simulateFailedLogin();
        refreshAllData();
    }

    public void triggerBruteForceSimulation() {
        EventSimulationService.simulateBruteForceAttack();
        refreshAllData();
    }

    /**
     * Logs out the current admin and returns to Login screen.
     */
    public void handleLogout() {
        ViewNavigator.showLoginView();
    }
}
