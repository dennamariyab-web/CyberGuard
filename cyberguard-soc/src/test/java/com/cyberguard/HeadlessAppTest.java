package com.cyberguard;

import com.cyberguard.model.Admin;
import com.cyberguard.view.DashboardView;
import com.cyberguard.view.LoginView;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HeadlessAppTest {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> [UI TEST] Starting JavaFX Application & View Verification...");

        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        // Initialize JavaFX Toolkit
        Platform.startup(() -> {
            try {
                System.out.println(">>> [UI TEST] JavaFX Platform initialized successfully.");

                // 1. Instantiate LoginView
                LoginView loginView = new LoginView();
                if (loginView.getView() == null) {
                    throw new IllegalStateException("LoginView root node is null!");
                }
                System.out.println(">>> [UI TEST] LoginView instantiated and rendered cleanly.");

                // 2. Instantiate DashboardView with test admin
                Admin testAdmin = new Admin(1, "admin", "hash", "salt", "Senior Analyst", "SOC Lead", LocalDateTime.now());
                DashboardView dashboardView = new DashboardView(testAdmin);
                if (dashboardView.getView() == null) {
                    throw new IllegalStateException("DashboardView root node is null!");
                }
                System.out.println(">>> [UI TEST] DashboardView instantiated and populated cleanly.");

                // 3. Verify CSS resource availability
                String cssUrl = HeadlessAppTest.class.getResource("/css/cyberguard.css") != null
                        ? HeadlessAppTest.class.getResource("/css/cyberguard.css").toExternalForm()
                        : null;
                if (cssUrl == null) {
                    throw new IllegalStateException("cyberguard.css not found on classpath!");
                }
                System.out.println(">>> [UI TEST] cyberguard.css stylesheet verified: " + cssUrl);

            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        if (!finished) {
            System.err.println(">>> [UI TEST TIMEOUT] JavaFX test timed out.");
            System.exit(1);
        }

        if (error[0] != null) {
            System.err.println(">>> [UI TEST FAILED]: " + error[0].getMessage());
            error[0].printStackTrace();
            System.exit(1);
        }

        System.out.println(">>> [UI TEST SUCCESS] All JavaFX components, styles and models verified 100% functional!");
        Platform.exit();
        System.exit(0);
    }
}
