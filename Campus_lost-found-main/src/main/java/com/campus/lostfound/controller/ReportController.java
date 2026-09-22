package com.campus.lostfound.controller;

import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ReportService;
import com.campus.lostfound.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Note: Category and Location dropdowns are populated from small lookup
 * lists here for simplicity. In a full build these come from the
 * categories/locations tables via a small CategoryDAO/LocationDAO -
 * swap loadLookupLists() to call those once the team's DAO for them is ready.
 */
public class ReportController extends NavigationController implements Initializable {

    @FXML private ToggleGroup itemTypeGroup;
    @FXML private RadioButton lostRadio;
    @FXML private RadioButton foundRadio;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> locationCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private Label feedbackLabel;

    private final ReportService reportService = new ReportService();

    // Simple id-to-name lookups matching db/schema.sql seed data.
    private static final List<String> CATEGORIES = List.of(
            "Electronics", "ID Card", "Bag", "Book", "Keys", "Clothing", "Wallet", "Other");
    private static final List<String> LOCATIONS = List.of(
            "Library", "Main Cafeteria", "Sports Complex", "Computer Science Block",
            "Auditorium", "Hostel Block A", "Hostel Block B", "Main Gate", "Parking Lot", "Other");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryCombo.setItems(FXCollections.observableArrayList(CATEGORIES));
        locationCombo.setItems(FXCollections.observableArrayList(LOCATIONS));
        datePicker.setValue(java.time.LocalDate.now());
    }

    @FXML
    private void handleSubmit() {
        hideFeedback();
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showFeedback("Your session has expired. Please log in again.", false);
            return;
        }

        String categoryName = categoryCombo.getValue();
        String locationName = locationCombo.getValue();
        if (categoryName == null || locationName == null) {
            showFeedback("Please select a category and a location.", false);
            return;
        }

        ItemType type = lostRadio.isSelected() ? ItemType.LOST : ItemType.FOUND;
        int categoryId = CATEGORIES.indexOf(categoryName) + 1; // matches seeded auto-increment order
        int locationId = LOCATIONS.indexOf(locationName) + 1;

        try {
            var result = reportService.reportItem(
                    currentUser.getId(), type, categoryId, locationId,
                    datePicker.getValue(), descriptionArea.getText());

            if (!result.matches.isEmpty()) {
                showFeedback("Report submitted! We found " + result.matches.size() +
                        " possible match(es) - check Search & Matches.", true);
            } else {
                showFeedback("Report submitted. We'll notify you if a match comes in.", true);
            }
            clearForm();
        } catch (IllegalArgumentException e) {
            showFeedback(e.getMessage(), false);
        } catch (SQLException e) {
            showFeedback("Could not reach the database. Please try again.", false);
        }
    }

    private void clearForm() {
        descriptionArea.clear();
        categoryCombo.getSelectionModel().clearSelection();
        locationCombo.getSelectionModel().clearSelection();
        datePicker.setValue(java.time.LocalDate.now());
    }

    private void showFeedback(String message, boolean success) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll("status-label", "error-label");
        feedbackLabel.getStyleClass().add(success ? "status-label" : "error-label");
        feedbackLabel.setManaged(true);
        feedbackLabel.setVisible(true);
    }

    private void hideFeedback() {
        feedbackLabel.setManaged(false);
        feedbackLabel.setVisible(false);
    }
}
