package com.cyberguard;

import com.cyberguard.database.DBConnection;
import com.cyberguard.view.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Application Entry Point for CyberGuard SOC Monitoring System.
 * Initializes database connectivity, registers primary stage, and presents
 * the initial authentication gateway.
 */
public class App extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("=================================================");
        System.out.println("  CYBERGUARD - SOC MONITORING SYSTEM STARTING    ");
        System.out.println("=================================================");

        // Attempt automatic database schema discovery and bootstrap
        try {
            DBConnection.initializeDatabase();
        } catch (Exception e) {
            System.err.println("[CyberGuard] DB initialization standby notice: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        ViewNavigator.setPrimaryStage(primaryStage);

        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);

        // Show Admin Login Gateway initially
        ViewNavigator.showLoginView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
