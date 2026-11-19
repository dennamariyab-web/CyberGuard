package com.cyberguard.view;

import com.cyberguard.controller.LoginController;
import com.cyberguard.util.IconUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;

/**
 * Modern JavaFX Login View for the CyberGuard SOC Monitoring System.
 * Matches the enterprise dark-mode cybersecurity theme inspired by professional SIEMs.
 */
public class LoginView {

    private final StackPane root;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final TextField passwordTextField;
    private final Button togglePasswordBtn;
    private final Button loginBtn;
    private final HBox errorBanner;
    private final Label errorLabel;
    private final LoginController controller;
    private boolean isPasswordVisible = false;

    public LoginView() {
        this.controller = new LoginController(this);

        root = new StackPane();
        root.getStyleClass().add("login-root");

        // Main Login Card
        VBox card = new VBox(18);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);

        // 1. Brand Logo & Header
        VBox brandBox = createBrandHeader();

        // 2. Headings
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);
        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.getStyleClass().add("login-heading");
        Label subLabel = new Label("Access your security analytics dashboard");
        subLabel.getStyleClass().add("login-subheading");
        titleBox.getChildren().addAll(welcomeLabel, subLabel);

        // 3. Error Banner (Hidden by default)
        errorBanner = new HBox(10);
        errorBanner.getStyleClass().add("error-banner");
        errorBanner.setAlignment(Pos.CENTER_LEFT);
        errorBanner.setVisible(false);
        errorBanner.managedProperty().bind(errorBanner.visibleProperty());

        SVGPath alertIcon = IconUtil.createIcon(IconUtil.ALERT_PATH, "#ef4444", 0.9);
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-banner-text");
        errorBanner.getChildren().addAll(alertIcon, errorLabel);

        // 4. Form Fields
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER_LEFT);

        // Username Field
        VBox userBox = new VBox(6);
        Label userLabel = new Label("Administrator Username");
        userLabel.getStyleClass().add("field-label");

        usernameField = new TextField();
        usernameField.setPromptText("Enter admin username (e.g. admin)");
        usernameField.getStyleClass().add("custom-text-field");
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                controller.handleLogin();
            }
        });
        userBox.getChildren().addAll(userLabel, usernameField);

        // Password Field with Show/Hide Toggle
        VBox passBox = new VBox(6);
        Label passLabel = new Label("Security Access Password");
        passLabel.getStyleClass().add("field-label");

        StackPane passInputStack = new StackPane();
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("custom-text-field");

        passwordTextField = new TextField();
        passwordTextField.setPromptText("Enter password");
        passwordTextField.getStyleClass().add("custom-text-field");
        passwordTextField.setVisible(false);
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());

        // Keep masked and unmasked fields synchronized
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        // Eye toggle button
        togglePasswordBtn = new Button("Show");
        togglePasswordBtn.getStyleClass().add("password-toggle-btn");
        StackPane.setAlignment(togglePasswordBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordBtn, new Insets(0, 8, 0, 0));

        togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());

        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                controller.handleLogin();
            }
        });
        passwordTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                controller.handleLogin();
            }
        });

        passInputStack.getChildren().addAll(passwordField, passwordTextField, togglePasswordBtn);
        passBox.getChildren().addAll(passLabel, passInputStack);

        // 5. Sign In Action Button
        loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary-gradient");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> controller.handleLogin());

        // 6. Demo Credentials Hint
        VBox demoBox = new VBox(3);
        demoBox.getStyleClass().add("demo-credentials-box");
        Label demoTitle = new Label("Default SOC Administrator Credential:");
        demoTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label demoCreds = new Label("Username: admin   |   Password: password123");
        demoCreds.getStyleClass().add("demo-credentials-text");
        demoBox.getChildren().addAll(demoTitle, demoCreds);

        // 7. Security Compliance Tag
        Label footerTag = new Label("Secure • Compliant • Professional Security Analytics");
        footerTag.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");

        // Assemble Form
        form.getChildren().addAll(userBox, passBox, loginBtn, demoBox);

        // Assemble Card
        card.getChildren().addAll(brandBox, titleBox, errorBanner, form, footerTag);

        root.getChildren().add(card);
    }

    private VBox createBrandHeader() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        HBox iconBadge = new HBox();
        iconBadge.getStyleClass().add("brand-icon-container");
        SVGPath shield = IconUtil.createIcon(IconUtil.SHIELD_PATH, "#00d2ff", 1.8);
        iconBadge.getChildren().add(shield);
        iconBadge.setMaxSize(48, 48);
        iconBadge.setAlignment(Pos.CENTER);

        Label title = new Label("CYBERGUARD");
        title.getStyleClass().add("brand-title");

        Label sub = new Label("SECURITY OPERATIONS CENTER");
        sub.getStyleClass().add("brand-subtitle");

        box.getChildren().addAll(iconBadge, title, sub);
        return box;
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("Hide");
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            togglePasswordBtn.setText("Show");
        }
    }

    public String getUsernameInput() {
        return usernameField.getText() != null ? usernameField.getText().trim() : "";
    }

    public String getPasswordInput() {
        return isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorBanner.setVisible(true);
    }

    public void clearError() {
        errorBanner.setVisible(false);
        errorLabel.setText("");
    }

    public void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        loginBtn.setText(loading ? "Authenticating..." : "Sign In");
    }

    public Parent getView() {
        return root;
    }
}
