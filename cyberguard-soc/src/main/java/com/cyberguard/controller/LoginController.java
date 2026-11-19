package com.cyberguard.controller;

import com.cyberguard.model.Admin;
import com.cyberguard.service.AuthService;
import com.cyberguard.view.LoginView;
import com.cyberguard.view.ViewNavigator;

/**
 * Controller handling user interactions on the CyberGuard Admin Login page.
 */
public class LoginController {

    private final LoginView view;
    private final AuthService authService;

    public LoginController(LoginView view) {
        this.view = view;
        this.authService = new AuthService();
    }

    /**
     * Executes login verification when the user clicks 'Sign In' or presses Enter.
     */
    public void handleLogin() {
        view.clearError();

        String username = view.getUsernameInput();
        String password = view.getPasswordInput();

        // Validation check
        if (username.isEmpty()) {
            view.showError("Please enter your administrator username.");
            return;
        }

        if (password.isEmpty()) {
            view.showError("Please enter your security access password.");
            return;
        }

        // Set loading/processing indicator if desired
        view.setLoading(true);

        try {
            // Validate credentials against AuthService & Database
            AuthService.AuthResult result = authService.login(username, password, "127.0.0.1");

            if (result.isSuccess()) {
                Admin admin = result.getAdmin();
                ViewNavigator.setCurrentAdmin(admin);
                System.out.println("[CyberGuard Auth] Login successful for: " + admin.getUsername());
                // Transition to Main SOC Dashboard
                ViewNavigator.showDashboardView();
            } else {
                view.showError(result.getMessage());
            }
        } finally {
            view.setLoading(false);
        }
    }
}
