package com.campus.lostfound.controller;

import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.util.SceneManager;
import com.campus.lostfound.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        hideError();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        try {
            Optional<User> user = authService.login(email, password);
            if (user.isEmpty()) {
                showError("Incorrect email or password.");
                return;
            }
            SessionManager.setCurrentUser(user.get());
            SceneManager.switchTo("dashboard.fxml", "Dashboard");
        } catch (SQLException e) {
            showError("Could not reach the database. Please try again.");
        } catch (Exception e) {
            showError("Something went wrong while loading the dashboard.");
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            SceneManager.switchTo("register.fxml", "Create Account");
        } catch (Exception e) {
            showError("Could not open the registration screen.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
}
