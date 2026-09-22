package com.campus.lostfound.controller;

import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.util.SceneManager;
import com.campus.lostfound.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleCombo.setItems(FXCollections.observableArrayList("STUDENT", "FACULTY", "STAFF"));
        roleCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister() {
        hideError();
        String name = safeText(nameField);
        String email = safeText(emailField);
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String role = roleCombo.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in every field.");
            return;
        }
        if (password.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }

        try {
            User user = authService.register(name, email, password, role);
            SessionManager.setCurrentUser(user);
            SceneManager.switchTo("dashboard.fxml", "Dashboard");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Could not reach the database. Please try again.");
        } catch (Exception e) {
            showError("Something went wrong while creating your account.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        try {
            SceneManager.switchTo("login.fxml", "Login");
        } catch (Exception e) {
            showError("Could not open the login screen.");
        }
    }

    private String safeText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
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
