package com.campus.lostfound.service;

import com.campus.lostfound.dao.NotificationDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchRecord;
import com.campus.lostfound.model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    /** Alerts both the lost-item reporter and the found-item reporter of a new match. */
    public void notifyMatch(Item lostItem, Item foundItem, MatchRecord match) throws SQLException {
        String messageToLostReporter = String.format(
                "A possible match was found for your lost %s near %s.",
                lostItem.getCategoryName(), lostItem.getLocationName());
        notificationDAO.insert(new Notification(lostItem.getReporterId(), match.getId(), messageToLostReporter));

        String messageToFoundReporter = String.format(
                "Someone may be looking for the %s you found near %s.",
                foundItem.getCategoryName(), foundItem.getLocationName());
        notificationDAO.insert(new Notification(foundItem.getReporterId(), match.getId(), messageToFoundReporter));
    }

    public List<Notification> getNotificationsForUser(int userId) throws SQLException {
        return notificationDAO.findByUserId(userId);
    }

    public void markAsRead(int notificationId) throws SQLException {
        notificationDAO.markRead(notificationId);
    }
}
