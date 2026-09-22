package com.campus.lostfound.controller;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.MatchDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchRecord;
import com.campus.lostfound.model.Notification;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController extends NavigationController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label myReportsCountLabel;
    @FXML private Label matchesCountLabel;
    @FXML private Label notificationsCountLabel;

    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, String> categoryColumn;
    @FXML private TableColumn<Item, String> locationColumn;
    @FXML private TableColumn<Item, String> dateColumn;
    @FXML private TableColumn<Item, String> statusColumn;

    @FXML private ListView<String> notificationsList;

    private final ItemDAO itemDAO = new ItemDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final NotificationService notificationService = new NotificationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return; // guarded navigation should prevent this, but fail safe
        }
        welcomeLabel.setText("Welcome back, " + currentUser.getFullName().split(" ")[0]);

        typeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getItemType().toString()));
        categoryColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCategoryName()));
        locationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getLocationName()));
        dateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getItemDate().toString()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus().toString()));

        loadData(currentUser.getId());
    }

    private void loadData(int userId) {
        try {
            List<Item> items = itemDAO.findByReporter(userId);
            itemsTable.setItems(FXCollections.observableArrayList(items));
            myReportsCountLabel.setText(String.valueOf(items.size()));

            List<MatchRecord> matches = matchDAO.findByUserId(userId);
            long activeMatches = matches.stream()
                    .filter(m -> m.getStatus() != MatchRecord.Status.REJECTED)
                    .count();
            matchesCountLabel.setText(String.valueOf(activeMatches));

            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            long unread = notifications.stream().filter(n -> !n.isRead()).count();
            notificationsCountLabel.setText(String.valueOf(unread));

            notificationsList.setItems(FXCollections.observableArrayList(
                    notifications.stream().map(Notification::getMessage).toList()
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
