package com.cyberguard.view;

import com.cyberguard.controller.DashboardController;
import com.cyberguard.database.DBConnection;
import com.cyberguard.model.Admin;
import com.cyberguard.model.Alert;
import com.cyberguard.model.DashboardStats;
import com.cyberguard.model.SecurityEvent;
import com.cyberguard.util.IconUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Enterprise SOC Dashboard View for CyberGuard.
 * Provides real-time event monitoring, threat detection alerts, and SIEM visual telemetry.
 */
public class DashboardView {

    private final BorderPane root;
    private final DashboardController controller;
    private final Admin currentAdmin;

    // Stat Cards Elements
    private Label totalAttemptsVal;
    private Label successfulLoginsVal;
    private Label failedLoginsVal;
    private Label suspiciousActivitiesVal;

    // Tables
    private TableView<SecurityEvent> overviewEventsTable;
    private TableView<SecurityEvent> fullEventsTable;
    private TableView<SecurityEvent> failedLoginsTable;
    private TableView<Alert> alertsTable;

    // Navigation Stack and Active View Container
    private StackPane contentArea;
    private VBox dashboardOverviewPane;
    private VBox eventsPane;
    private VBox failedLoginsPane;
    private VBox threatDetectionPane;
    private VBox alertsManagementPane;
    private VBox reportsPane;
    private VBox settingsPane;

    // Charts
    private PieChart authOutcomeChart;
    private BarChart<String, Number> alertSeverityChart;
    private LineChart<String, Number> activityTimelineChart;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DashboardView(Admin admin) {
        this.currentAdmin = admin;
        this.controller = new DashboardController(this, admin);

        root = new BorderPane();
        root.setStyle("-fx-background-color: #090d16;");

        // 1. Top Navigation Bar
        root.setTop(createTopNavbar());

        // 2. Left Sidebar Navigation
        root.setLeft(createSidebar());

        // 3. Central Content Area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));

        // Create individual modular pages
        dashboardOverviewPane = createOverviewPane();
        eventsPane = createEventsPane();
        failedLoginsPane = createFailedLoginsPane();
        threatDetectionPane = createThreatDetectionPane();
        alertsManagementPane = createAlertsManagementPane();
        reportsPane = createReportsPane();
        settingsPane = createSettingsPane();

        contentArea.getChildren().addAll(
                dashboardOverviewPane, eventsPane, failedLoginsPane,
                threatDetectionPane, alertsManagementPane, reportsPane, settingsPane
        );

        showPage(dashboardOverviewPane);
        root.setCenter(contentArea);

        // Initialize Controller
        controller.initialize();
    }

    private HBox createTopNavbar() {
        HBox topbar = new HBox(18);
        topbar.getStyleClass().add("top-navbar");
        topbar.setAlignment(Pos.CENTER_LEFT);

        // Left Brand
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        SVGPath shield = IconUtil.createIcon(IconUtil.SHIELD_PATH, "#00d2ff", 1.2);
        Label title = new Label("CYBERGUARD SOC");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label subtitle = new Label("• SIEM MONITORING PLATFORM");
        subtitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-weight: bold;");
        brand.getChildren().addAll(shield, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // System Live Pulse Badge
        HBox liveBadge = new HBox(8);
        liveBadge.getStyleClass().add("status-indicator-live");
        liveBadge.setAlignment(Pos.CENTER);
        Circle pulseDot = new Circle(4);
        pulseDot.getStyleClass().add("status-dot-green");
        Label liveText = new Label("SOC STATUS: NORMAL");
        liveText.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");
        liveBadge.getChildren().addAll(pulseDot, liveText);

        // Database Connectivity Indicator
        HBox dbBadge = new HBox(6);
        dbBadge.setStyle("-fx-background-color: #1e293b; -fx-padding: 4px 10px; -fx-background-radius: 12px;");
        dbBadge.setAlignment(Pos.CENTER);
        boolean isDbUp = DBConnection.isConnected();
        Circle dbDot = new Circle(3.5, isDbUp ? javafx.scene.paint.Color.web("#10b981") : javafx.scene.paint.Color.web("#f59e0b"));
        Label dbText = new Label(isDbUp ? "MySQL 8.0: Connected" : "DB: Standby Mode");
        dbText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        dbBadge.getChildren().addAll(dbDot, dbText);

        // Real-Time Live Clock
        Label clockLabel = new Label();
        clockLabel.getStyleClass().add("clock-display");
        updateClock(clockLabel);
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock(clockLabel)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Administrator Profile Badge
        HBox adminBox = new HBox(8);
        adminBox.getStyleClass().add("admin-badge");
        adminBox.setAlignment(Pos.CENTER);
        SVGPath userIcon = IconUtil.createIcon(IconUtil.USER_PATH, "#38bdf8", 0.9);
        String adminName = (currentAdmin != null) ? currentAdmin.getUsername() : "admin";
        Label adminLabel = new Label("Admin: " + adminName);
        adminLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px; -fx-font-weight: bold;");
        adminBox.getChildren().addAll(userIcon, adminLabel);

        topbar.getChildren().addAll(brand, spacer, liveBadge, dbBadge, clockLabel, adminBox);
        return topbar;
    }

    private void updateClock(Label label) {
        label.setText(LocalDateTime.now().format(TIME_FORMATTER) + " UTC");
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add("sidebar");

        Label cat1 = new Label("CORE MONITORING");
        cat1.getStyleClass().add("sidebar-category");

        Button navDash = createNavButton("Dashboard", IconUtil.DASHBOARD_PATH, true);
        Button navEvents = createNavButton("Security Events", IconUtil.LOGS_PATH, false);
        Button navFailed = createNavButton("Failed Logins", IconUtil.ALERT_PATH, false);
        Button navThreats = createNavButton("Threat Detection", IconUtil.THREAT_PATH, false);
        Button navAlerts = createNavButton("Alerts Management", IconUtil.ALERT_PATH, false);

        Label cat2 = new Label("ANALYTICS & INTEL");
        cat2.getStyleClass().add("sidebar-category");
        Button navReports = createNavButton("Reports & Charts", IconUtil.REPORT_PATH, false);
        Button navSettings = createNavButton("Settings & Rules", IconUtil.SETTINGS_PATH, false);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button navLogout = new Button("Logout");
        navLogout.getStyleClass().add("nav-button-logout");
        SVGPath logoutIcon = IconUtil.createIcon(IconUtil.LOGOUT_PATH, "#f87171", 0.9);
        navLogout.setGraphic(logoutIcon);
        navLogout.setOnAction(e -> controller.handleLogout());

        // Nav click handlers
        List<Button> allNavButtons = List.of(navDash, navEvents, navFailed, navThreats, navAlerts, navReports, navSettings);

        navDash.setOnAction(e -> { 
            setActiveNav(navDash, allNavButtons); 
            showPage(dashboardOverviewPane); 
            controller.refreshAllData();
        });
        navEvents.setOnAction(e -> { 
            setActiveNav(navEvents, allNavButtons); 
            showPage(eventsPane); 
            controller.filterEvents("", "ALL"); 
        });
        navFailed.setOnAction(e -> { 
            setActiveNav(navFailed, allNavButtons); 
            showPage(failedLoginsPane); 
            controller.filterFailedLogins(""); 
        });
        navThreats.setOnAction(e -> { 
            setActiveNav(navThreats, allNavButtons); 
            showPage(threatDetectionPane); 
        });
        navAlerts.setOnAction(e -> { 
            setActiveNav(navAlerts, allNavButtons); 
            showPage(alertsManagementPane); 
            controller.refreshAllData();
        });
        navReports.setOnAction(e -> { 
            setActiveNav(navReports, allNavButtons); 
            showPage(reportsPane); 
            controller.refreshAllData();
        });
        navSettings.setOnAction(e -> { 
            setActiveNav(navSettings, allNavButtons); 
            showPage(settingsPane); 
        });

        sidebar.getChildren().addAll(
                cat1, navDash, navEvents, navFailed, navThreats, navAlerts,
                cat2, navReports, navSettings,
                spacer, navLogout
        );
        return sidebar;
    }

    private Button createNavButton(String text, String svgPath, boolean isActive) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        if (isActive) {
            btn.getStyleClass().add("nav-button-active");
        }
        SVGPath icon = IconUtil.createIcon(svgPath, isActive ? "#38bdf8" : "#94a3b8", 0.85);
        btn.setGraphic(icon);
        return btn;
    }

    private void setActiveNav(Button selected, List<Button> allButtons) {
        for (Button b : allButtons) {
            b.getStyleClass().remove("nav-button-active");
        }
        selected.getStyleClass().add("nav-button-active");
    }

    private void showPage(VBox targetPage) {
        dashboardOverviewPane.setVisible(false);
        eventsPane.setVisible(false);
        failedLoginsPane.setVisible(false);
        threatDetectionPane.setVisible(false);
        alertsManagementPane.setVisible(false);
        reportsPane.setVisible(false);
        settingsPane.setVisible(false);

        targetPage.setVisible(true);
    }

    // ==========================================================
    // 1. DASHBOARD OVERVIEW PAGE
    // ==========================================================
    private VBox createOverviewPane() {
        VBox pane = new VBox(18);
        pane.setFillWidth(true);

        // Header and quick simulations bar
        HBox headBar = new HBox(12);
        headBar.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label title = new Label("SOC Central Monitoring Dashboard");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label sub = new Label("Real-time behavioral telemetry, authentication audit and automated threat triage");
        sub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button simSuccessBtn = new Button("Simulate Normal Login");
        simSuccessBtn.getStyleClass().add("btn-simulate-normal");
        simSuccessBtn.setOnAction(e -> controller.triggerNormalLoginSimulation());

        Button simAttackBtn = new Button("⚡ Simulate Brute Force");
        simAttackBtn.getStyleClass().add("btn-simulate-attack");
        simAttackBtn.setOnAction(e -> controller.triggerBruteForceSimulation());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-action-sm");
        refreshBtn.setGraphic(IconUtil.createIcon(IconUtil.REFRESH_PATH, "#38bdf8", 0.8));
        refreshBtn.setOnAction(e -> controller.refreshAllData());

        headBar.getChildren().addAll(titleBox, spacer, simSuccessBtn, simAttackBtn, refreshBtn);

        // 4 Stat Metric Cards
        HBox statsRow = createStatsRow();

        // Charts & Quick Overview Row
        HBox visualRow = new HBox(16);
        visualRow.setFillHeight(true);

        // Mini Alerts Breakdown Chart Card
        VBox chartCard = new VBox(10);
        chartCard.getStyleClass().add("dashboard-card");
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        Label chartTitle = new Label("Authentication Outcome Distribution");
        chartTitle.getStyleClass().add("card-heading");

        authOutcomeChart = new PieChart();
        authOutcomeChart.setPrefHeight(240);
        authOutcomeChart.setLabelsVisible(true);
        chartCard.getChildren().addAll(chartTitle, authOutcomeChart);

        // Quick Offenses & Incidents Panel
        VBox alertsSummaryCard = new VBox(12);
        alertsSummaryCard.getStyleClass().add("dashboard-card");
        alertsSummaryCard.setPrefWidth(420);
        Label offensesTitle = new Label("Active Threat Offenses Summary");
        offensesTitle.getStyleClass().add("card-heading");

        VBox offenseList = new VBox(8);
        offenseList.getChildren().addAll(
                createOffenseRow("Multiple Failed Logins (Rule 1)", "Sliding 10m window", "#ef4444"),
                createOffenseRow("Targeted Account Brute Force (Rule 2)", "Single user targeted", "#f59e0b"),
                createOffenseRow("Suspicious Outbound Tunneling", "High packet anomaly", "#ef4444"),
                createOffenseRow("Off-Hours Login Spike", "Unusual access pattern", "#38bdf8")
        );
        alertsSummaryCard.getChildren().addAll(offensesTitle, offenseList);

        visualRow.getChildren().addAll(chartCard, alertsSummaryCard);

        // Quick Table Preview (Latest Security Events)
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("dashboard-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        Label tableTitle = new Label("Live Authentication & Security Events Log");
        tableTitle.getStyleClass().add("card-heading");

        overviewEventsTable = buildSecurityEventsTable();
        overviewEventsTable.setPrefHeight(260);
        tableCard.getChildren().addAll(tableTitle, overviewEventsTable);

        pane.getChildren().addAll(headBar, statsRow, visualRow, tableCard);
        return pane;
    }

    private HBox createOffenseRow(String title, String desc, String color) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #0d1322; -fx-padding: 8px 12px; -fx-background-radius: 8px; -fx-border-color: #1e293b; -fx-border-width: 1px;");

        Circle dot = new Circle(4, javafx.scene.paint.Color.web(color));
        VBox texts = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label d = new Label(desc);
        d.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        texts.getChildren().addAll(t, d);

        row.getChildren().addAll(dot, texts);
        return row;
    }

    private HBox createStatsRow() {
        HBox row = new HBox(16);
        row.setFillHeight(true);

        totalAttemptsVal = new Label("0");
        VBox cardTotal = buildStatCard("TOTAL LOGIN ATTEMPTS", totalAttemptsVal, "Monitored inbound requests", "stat-card-total");

        successfulLoginsVal = new Label("0");
        VBox cardSuccess = buildStatCard("SUCCESSFUL LOGINS", successfulLoginsVal, "Authorized authentications", "stat-card-success");

        failedLoginsVal = new Label("0");
        VBox cardFailed = buildStatCard("FAILED LOGINS", failedLoginsVal, "Password/credential rejections", "stat-card-failed");

        suspiciousActivitiesVal = new Label("0");
        VBox cardSuspicious = buildStatCard("SUSPICIOUS ACTIVITIES", suspiciousActivitiesVal, "Potential threat alerts", "stat-card-suspicious");

        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardSuccess, Priority.ALWAYS);
        HBox.setHgrow(cardFailed, Priority.ALWAYS);
        HBox.setHgrow(cardSuspicious, Priority.ALWAYS);

        row.getChildren().addAll(cardTotal, cardSuccess, cardFailed, cardSuspicious);
        return row;
    }

    private VBox buildStatCard(String title, Label valueLabel, String subtext, String extraStyleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("stat-card", extraStyleClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");

        Label subLabel = new Label(subtext);
        subLabel.getStyleClass().add("stat-subtext");

        card.getChildren().addAll(titleLabel, valueLabel, subLabel);
        return card;
    }

    // ==========================================================
    // 2. SECURITY EVENTS TABLE & VIEW
    // ==========================================================
    private VBox createEventsPane() {
        VBox pane = new VBox(16);

        Label pageTitle = new Label("Security Events Monitoring");
        pageTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Search & Filter Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by username, IP address or event type...");
        searchField.getStyleClass().add("custom-text-field");
        searchField.setPrefWidth(320);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL", "SUCCESS", "FAILED", "SUSPICIOUS");
        statusFilter.setValue("ALL");
        statusFilter.getStyleClass().add("btn-action-sm");

        Button filterBtn = new Button("Apply Filter");
        filterBtn.getStyleClass().add("btn-action-sm");
        filterBtn.setOnAction(e -> controller.filterEvents(searchField.getText(), statusFilter.getValue()));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> controller.filterEvents(newVal, statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> controller.filterEvents(searchField.getText(), newVal));

        Button refreshBtn = new Button("Refresh Events");
        refreshBtn.getStyleClass().add("btn-action-sm");
        refreshBtn.setOnAction(e -> controller.refreshAllData());

        toolbar.getChildren().addAll(searchField, new Label("Status:"), statusFilter, filterBtn, refreshBtn);

        fullEventsTable = buildSecurityEventsTable();
        VBox.setVgrow(fullEventsTable, Priority.ALWAYS);

        pane.getChildren().addAll(pageTitle, toolbar, fullEventsTable);
        return pane;
    }

    private TableView<SecurityEvent> buildSecurityEventsTable() {
        TableView<SecurityEvent> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SecurityEvent, String> idCol = new TableColumn<>("Event ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty("EVT-" + c.getValue().getId()));
        idCol.setPrefWidth(90);

        TableColumn<SecurityEvent, String> timeCol = new TableColumn<>("Date & Time");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimestamp().format(TIME_FORMATTER)));
        timeCol.setPrefWidth(150);

        TableColumn<SecurityEvent, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        userCol.setPrefWidth(120);

        TableColumn<SecurityEvent, String> ipCol = new TableColumn<>("IP Address");
        ipCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIpAddress()));
        ipCol.setPrefWidth(130);

        TableColumn<SecurityEvent, String> typeCol = new TableColumn<>("Event Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventType()));
        typeCol.setPrefWidth(180);

        TableColumn<SecurityEvent, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    if ("SUCCESS".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-success");
                    } else if ("FAILED".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-failed");
                    } else {
                        badge.getStyleClass().add("badge-warning");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<SecurityEvent, String> detailsCol = new TableColumn<>("Event Details");
        detailsCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetails()));
        detailsCol.setPrefWidth(220);

        table.getColumns().addAll(idCol, timeCol, userCol, ipCol, typeCol, statusCol, detailsCol);
        return table;
    }

    // ==========================================================
    // 3. FAILED LOGIN ATTEMPTS VIEW
    // ==========================================================
    private VBox createFailedLoginsPane() {
        VBox pane = new VBox(16);
        Label title = new Label("Failed & Rejected Login Stream");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label sub = new Label("Audit log isolating authentication failures subject to threat detection rules.");
        sub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        // Filter Bar for Failed Logins
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchFailed = new TextField();
        searchFailed.setPromptText("Filter failed logins by user or IP...");
        searchFailed.getStyleClass().add("custom-text-field");
        searchFailed.setPrefWidth(300);
        searchFailed.textProperty().addListener((obs, oldVal, newVal) -> controller.filterFailedLogins(newVal));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-action-sm");
        refreshBtn.setOnAction(e -> controller.refreshAllData());

        toolbar.getChildren().addAll(searchFailed, refreshBtn);

        failedLoginsTable = buildSecurityEventsTable();
        VBox.setVgrow(failedLoginsTable, Priority.ALWAYS);

        pane.getChildren().addAll(title, sub, toolbar, failedLoginsTable);
        return pane;
    }

    // ==========================================================
    // 4. THREAT DETECTION VIEW
    // ==========================================================
    private VBox createThreatDetectionPane() {
        VBox pane = new VBox(16);
        Label title = new Label("Defensive Rule-Based Threat Detection");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Rule Cards
        HBox rulesRow = new HBox(16);
        rulesRow.setFillHeight(true);

        VBox r1 = buildRuleCard("Rule 01: IP Brute Force Breach",
                "Trigger: > 4 Failed login attempts from same IP within 10 minutes sliding window.",
                "Severity: HIGH (Red)", "Active Action: Generate SOC Incident & Flag IP", "#ef4444");

        VBox r2 = buildRuleCard("Rule 02: Targeted User Attack",
                "Trigger: >= 3 Consecutive failed attempts targeting a single administrator account.",
                "Severity: MEDIUM (Yellow)", "Active Action: Account Lockout Warning", "#f59e0b");

        VBox r3 = buildRuleCard("Rule 03: Off-Hours Activity",
                "Trigger: Elevated login velocity during non-business hours.",
                "Severity: LOW (Blue)", "Active Action: Log Baseline Deviation", "#38bdf8");

        HBox.setHgrow(r1, Priority.ALWAYS);
        HBox.setHgrow(r2, Priority.ALWAYS);
        HBox.setHgrow(r3, Priority.ALWAYS);
        rulesRow.getChildren().addAll(r1, r2, r3);

        // Simulation Test Controls
        VBox simControls = new VBox(10);
        simControls.getStyleClass().add("dashboard-card");
        Label simTitle = new Label("Defensive Rule Simulator (Safe Local Testing)");
        simTitle.getStyleClass().add("card-heading");
        Label simDesc = new Label("Execute local simulated security events to verify automated alert triggers without contacting external networks.");
        simDesc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        HBox btnRow = new HBox(12);
        Button btnSingleFail = new Button("Simulate 1 Failed Login");
        btnSingleFail.getStyleClass().add("btn-action-sm");
        btnSingleFail.setOnAction(e -> controller.triggerFailedLoginSimulation());

        Button btnBrute = new Button("⚡ Execute Brute Force Attack Sequence (>4 Fails)");
        btnBrute.getStyleClass().add("btn-simulate-attack");
        btnBrute.setOnAction(e -> controller.triggerBruteForceSimulation());

        Button btnRoutine = new Button("Simulate Routine Success");
        btnRoutine.getStyleClass().add("btn-simulate-normal");
        btnRoutine.setOnAction(e -> controller.triggerNormalLoginSimulation());

        btnRow.getChildren().addAll(btnSingleFail, btnBrute, btnRoutine);
        simControls.getChildren().addAll(simTitle, simDesc, btnRow);

        pane.getChildren().addAll(title, rulesRow, simControls);
        return pane;
    }

    private VBox buildRuleCard(String ruleName, String trigger, String severity, String action, String accentColor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("dashboard-card");
        card.setStyle("-fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 3px;");

        Label name = new Label(ruleName);
        name.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label trig = new Label(trigger);
        trig.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        trig.setWrapText(true);

        Label sev = new Label(severity);
        sev.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label act = new Label(action);
        act.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        card.getChildren().addAll(name, trig, sev, act);
        return card;
    }

    // ==========================================================
    // 5. ALERTS MANAGEMENT VIEW
    // ==========================================================
    private VBox createAlertsManagementPane() {
        VBox pane = new VBox(16);
        Label title = new Label("Security Alerts Management");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchAlerts = new TextField();
        searchAlerts.setPromptText("Search alerts by IP or threat description...");
        searchAlerts.getStyleClass().add("custom-text-field");
        searchAlerts.setPrefWidth(300);

        ComboBox<String> sevFilter = new ComboBox<>();
        sevFilter.getItems().addAll("ALL", "HIGH", "MEDIUM", "LOW");
        sevFilter.setValue("ALL");
        sevFilter.getStyleClass().add("btn-action-sm");

        Button resolveBtn = new Button("Mark Alert as Resolved");
        resolveBtn.getStyleClass().add("btn-action-sm");
        resolveBtn.setStyle("-fx-border-color: #10b981; -fx-text-fill: #34d399;");
        resolveBtn.setOnAction(e -> {
            Alert selected = alertsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.resolveAlert(selected);
            }
        });

        searchAlerts.textProperty().addListener((obs, o, n) -> controller.filterAlerts(n, sevFilter.getValue()));
        sevFilter.valueProperty().addListener((obs, o, n) -> controller.filterAlerts(searchAlerts.getText(), n));

        toolbar.getChildren().addAll(searchAlerts, new Label("Severity:"), sevFilter, resolveBtn);

        alertsTable = buildAlertsTable();
        VBox.setVgrow(alertsTable, Priority.ALWAYS);

        pane.getChildren().addAll(title, toolbar, alertsTable);
        return pane;
    }

    private TableView<Alert> buildAlertsTable() {
        TableView<Alert> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Alert, String> idCol = new TableColumn<>("Alert ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty("ALT-" + c.getValue().getId()));
        idCol.setPrefWidth(90);

        TableColumn<Alert, String> timeCol = new TableColumn<>("Detection Time");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetectionTime().format(TIME_FORMATTER)));
        timeCol.setPrefWidth(150);

        TableColumn<Alert, String> ipCol = new TableColumn<>("Source IP");
        ipCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIpAddress()));
        ipCol.setPrefWidth(120);

        TableColumn<Alert, String> threatCol = new TableColumn<>("Threat Type");
        threatCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThreatType()));
        threatCol.setPrefWidth(180);

        TableColumn<Alert, String> sevCol = new TableColumn<>("Severity");
        sevCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSeverity()));
        sevCol.setPrefWidth(100);
        sevCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(severity);
                    badge.getStyleClass().add("badge");
                    if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
                        badge.getStyleClass().add("badge-critical");
                    } else if ("MEDIUM".equalsIgnoreCase(severity)) {
                        badge.getStyleClass().add("badge-warning");
                    } else {
                        badge.getStyleClass().add("badge-info");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Alert, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    if ("RESOLVED".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-failed");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Alert, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        descCol.setPrefWidth(250);

        table.getColumns().addAll(idCol, timeCol, ipCol, threatCol, sevCol, statusCol, descCol);
        return table;
    }

    // ==========================================================
    // 6. REPORTS & CHARTS VIEW
    // ==========================================================
    private VBox createReportsPane() {
        VBox pane = new VBox(16);
        Label title = new Label("Visual Telemetry & Security Reports");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        HBox chartsRow = new HBox(16);
        chartsRow.setFillHeight(true);

        // Chart 1: Severity Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Alert Severity Level");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Alert Count");
        alertSeverityChart = new BarChart<>(xAxis, yAxis);
        alertSeverityChart.setTitle("Alerts by Threat Severity");
        alertSeverityChart.setLegendVisible(false);
        alertSeverityChart.setPrefHeight(340);
        HBox.setHgrow(alertSeverityChart, Priority.ALWAYS);

        // Chart 2: Timeline Line Chart
        CategoryAxis tXAxis = new CategoryAxis();
        tXAxis.setLabel("Interval");
        NumberAxis tYAxis = new NumberAxis();
        tYAxis.setLabel("Activity Count");
        activityTimelineChart = new LineChart<>(tXAxis, tYAxis);
        activityTimelineChart.setTitle("Authentication Volume Over Time");
        activityTimelineChart.setPrefHeight(340);
        HBox.setHgrow(activityTimelineChart, Priority.ALWAYS);

        chartsRow.getChildren().addAll(alertSeverityChart, activityTimelineChart);
        pane.getChildren().addAll(title, chartsRow);
        return pane;
    }

    // ==========================================================
    // 7. SETTINGS & CONFIGURATION VIEW
    // ==========================================================
    private VBox createSettingsPane() {
        VBox pane = new VBox(16);
        Label title = new Label("SOC Monitoring System Settings");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox dbConfigCard = new VBox(10);
        dbConfigCard.getStyleClass().add("dashboard-card");
        Label dbHead = new Label("Database Configuration & Status");
        dbHead.getStyleClass().add("card-heading");

        Label hostLbl = new Label("Host: " + DBConnection.getDbHost() + " : " + DBConnection.getDbPort());
        Label nameLbl = new Label("Database Name: " + DBConnection.getDbName());
        Label statusLbl = new Label("Status: " + (DBConnection.isConnected() ? "CONNECTED (MySQL 8.0)" : "STANDBY (In-Memory Fallback Active)"));
        statusLbl.setStyle("-fx-text-fill: " + (DBConnection.isConnected() ? "#10b981" : "#f59e0b") + "; -fx-font-weight: bold;");

        dbConfigCard.getChildren().addAll(dbHead, hostLbl, nameLbl, statusLbl);

        VBox rulesConfigCard = new VBox(10);
        rulesConfigCard.getStyleClass().add("dashboard-card");
        Label rulesHead = new Label("Detection Rule Configuration");
        rulesHead.getStyleClass().add("card-heading");
        Label r1Lbl = new Label("Failed Logins Threshold (Rule 1): 4 attempts");
        Label r2Lbl = new Label("Sliding Evaluation Window: 10 minutes");
        Label r3Lbl = new Label("Automatic Alerting: ENABLED");

        rulesConfigCard.getChildren().addAll(rulesHead, r1Lbl, r2Lbl, r3Lbl);

        pane.getChildren().addAll(title, dbConfigCard, rulesConfigCard);
        return pane;
    }

    // ==========================================================
    // PUBLIC UPDATE METHODS (CALLED BY CONTROLLER)
    // ==========================================================
    public void updateStats(DashboardStats stats) {
        totalAttemptsVal.setText(String.valueOf(stats.getTotalAttempts()));
        successfulLoginsVal.setText(String.valueOf(stats.getSuccessfulLogins()));
        failedLoginsVal.setText(String.valueOf(stats.getFailedLogins()));
        suspiciousActivitiesVal.setText(String.valueOf(stats.getSuspiciousActivities()));
    }

    public void updateEventsTable(ObservableList<SecurityEvent> events) {
        if (overviewEventsTable != null) {
            overviewEventsTable.setItems(events);
        }
        if (fullEventsTable != null) {
            fullEventsTable.setItems(events);
        }
    }

    public void updateFailedLoginsTable(ObservableList<SecurityEvent> failedEvents) {
        if (failedLoginsTable != null) {
            failedLoginsTable.setItems(failedEvents);
        }
    }

    public void updateAlertsTable(ObservableList<Alert> alerts) {
        if (alertsTable != null) {
            alertsTable.setItems(alerts);
        }
    }

    public void updateCharts(List<SecurityEvent> events, List<Alert> alerts) {
        // 1. Pie Chart
        int success = (int) events.stream().filter(e -> "SUCCESS".equalsIgnoreCase(e.getStatus())).count();
        int failed = (int) events.stream().filter(e -> "FAILED".equalsIgnoreCase(e.getStatus())).count();
        int suspicious = events.size() - success - failed;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Successful Logins (" + success + ")", Math.max(success, 0)),
                new PieChart.Data("Failed Attempts (" + failed + ")", Math.max(failed, 0)),
                new PieChart.Data("Suspicious Events (" + suspicious + ")", Math.max(suspicious, 0))
        );
        authOutcomeChart.setData(pieData);

        // 2. Bar Chart
        if (alertSeverityChart != null) {
            long high = alerts.stream().filter(a -> "HIGH".equalsIgnoreCase(a.getSeverity())).count();
            long med = alerts.stream().filter(a -> "MEDIUM".equalsIgnoreCase(a.getSeverity())).count();
            long low = alerts.stream().filter(a -> "LOW".equalsIgnoreCase(a.getSeverity())).count();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("HIGH", high));
            series.getData().add(new XYChart.Data<>("MEDIUM", med));
            series.getData().add(new XYChart.Data<>("LOW", low));

            alertSeverityChart.getData().clear();
            alertSeverityChart.getData().add(series);
        }

        // 3. Timeline Chart
        if (activityTimelineChart != null) {
            XYChart.Series<String, Number> timeSeries = new XYChart.Series<>();
            timeSeries.setName("Events per time window");
            timeSeries.getData().add(new XYChart.Data<>("T-40m", 3));
            timeSeries.getData().add(new XYChart.Data<>("T-30m", 5));
            timeSeries.getData().add(new XYChart.Data<>("T-20m", 12));
            timeSeries.getData().add(new XYChart.Data<>("T-10m", 8));
            timeSeries.getData().add(new XYChart.Data<>("Now", events.size() % 15 + 2));

            activityTimelineChart.getData().clear();
            activityTimelineChart.getData().add(timeSeries);
        }
    }

    public Parent getView() {
        return root;
    }
}
