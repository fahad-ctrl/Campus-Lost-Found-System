package com.campus.lostfound.controller;

import com.campus.lostfound.util.SceneManager;
import com.campus.lostfound.util.SessionManager;
import javafx.fxml.FXML;

/**
 * Shared sidebar navigation actions used by every logged-in screen
 * (Dashboard, Report Wizard, Search & Matches).
 */
public abstract class NavigationController {

    @FXML
    protected void handleGoToDashboard() {
        navigate("dashboard.fxml", "Dashboard");
    }

    @FXML
    protected void handleGoToReport() {
        navigate("report_wizard.fxml", "Report an Item");
    }

    @FXML
    protected void handleGoToSearch() {
        navigate("search_matches.fxml", "Search & Matches");
    }

    @FXML
    protected void handleLogout() {
        SessionManager.clear();
        navigate("login.fxml", "Login");
    }

    private void navigate(String fxml, String title) {
        try {
            SceneManager.switchTo(fxml, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
