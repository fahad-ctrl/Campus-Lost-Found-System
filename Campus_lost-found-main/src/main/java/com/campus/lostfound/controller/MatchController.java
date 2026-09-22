package com.campus.lostfound.controller;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.MatchDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchRecord;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MatchController extends NavigationController implements Initializable {

    // --- Browse tab ---
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterLocationCombo;
    @FXML private TextField keywordField;

    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, String> categoryColumn;
    @FXML private TableColumn<Item, String> locationColumn;
    @FXML private TableColumn<Item, String> dateColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, String> statusColumn;

    // --- My Matches tab ---
    @FXML private TableView<MatchRecord> matchesTable;
    @FXML private TableColumn<MatchRecord, String> matchLostColumn;
    @FXML private TableColumn<MatchRecord, String> matchFoundColumn;
    @FXML private TableColumn<MatchRecord, String> matchScoreColumn;
    @FXML private TableColumn<MatchRecord, String> matchStatusColumn;
    @FXML private Label claimResultLabel;

    private final ItemDAO itemDAO = new ItemDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final ClaimService claimService = new ClaimService();

    private static final List<String> CATEGORIES = List.of(
            "Electronics", "ID Card", "Bag", "Book", "Keys", "Clothing", "Wallet", "Other");
    private static final List<String> LOCATIONS = List.of(
            "Library", "Main Cafeteria", "Sports Complex", "Computer Science Block",
            "Auditorium", "Hostel Block A", "Hostel Block B", "Main Gate", "Parking Lot", "Other");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterTypeCombo.setItems(FXCollections.observableArrayList("LOST", "FOUND"));
        filterCategoryCombo.setItems(FXCollections.observableArrayList(CATEGORIES));
        filterLocationCombo.setItems(FXCollections.observableArrayList(LOCATIONS));

        typeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItemType().toString()));
        categoryColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoryName()));
        locationColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocationName()));
        dateColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItemDate().toString()));
        descriptionColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));

        matchLostColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLostItemDescription()));
        matchFoundColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFoundItemDescription()));
        matchScoreColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getScore() + "%"));
        matchStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));

        runSearch();
        loadMatches();
    }

    @FXML
    private void handleSearch() {
        runSearch();
    }

    private void runSearch() {
        try {
            ItemType type = filterTypeCombo.getValue() == null ? null : ItemType.valueOf(filterTypeCombo.getValue());
            Integer categoryId = filterCategoryCombo.getValue() == null ? null :
                    CATEGORIES.indexOf(filterCategoryCombo.getValue()) + 1;
            Integer locationId = filterLocationCombo.getValue() == null ? null :
                    LOCATIONS.indexOf(filterLocationCombo.getValue()) + 1;
            String keyword = keywordField.getText();

            List<Item> results = itemDAO.search(type, categoryId, locationId, keyword);
            itemsTable.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMatches() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        try {
            List<MatchRecord> matches = matchDAO.findByUserId(currentUser.getId());
            matchesTable.setItems(FXCollections.observableArrayList(matches));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirmMatch() {
        MatchRecord selected = matchesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            claimResultLabel.setText("Select a match from the table first.");
            return;
        }
        try {
            String referenceCode = claimService.confirmMatch(selected.getId());
            claimResultLabel.setText("Match confirmed. Reference code for handover: " + referenceCode);
            loadMatches();
        } catch (Exception e) {
            claimResultLabel.setText("Could not confirm this match: " + e.getMessage());
        }
    }
}
