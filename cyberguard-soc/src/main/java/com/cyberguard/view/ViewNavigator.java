package com.cyberguard.view;

import com.cyberguard.model.Admin;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Screen transition and scene navigator for the CyberGuard application.
 */
public class ViewNavigator {

    private static Stage primaryStage;
    private static Admin currentAdmin;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }

    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * Shows the Login screen.
     */
    public static void showLoginView() {
        currentAdmin = null;
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 1100, 750);
        applyStylesheet(scene);

        primaryStage.setTitle("CyberGuard - Security Operations Center (SOC) Monitoring System | Authentication");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Shows the Main SOC Dashboard after successful authentication.
     */
    public static void showDashboardView() {
        DashboardView dashboardView = new DashboardView(currentAdmin);
        Scene scene = new Scene(dashboardView.getView(), 1280, 820);
        applyStylesheet(scene);

        primaryStage.setTitle("CyberGuard SOC | Central Monitoring Console - " + (currentAdmin != null ? currentAdmin.getFullName() : "Administrator"));
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void applyStylesheet(Scene scene) {
        try {
            String cssPath = ViewNavigator.class.getResource("/css/cyberguard.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("[CyberGuard] CSS Load Warning: " + e.getMessage());
        }
    }
}
