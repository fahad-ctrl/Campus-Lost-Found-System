package com.campus.lostfound.dao;

import com.campus.lostfound.model.Notification;
import com.campus.lostfound.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public Notification insert(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, match_id, message, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notification.getUserId());
            if (notification.getMatchId() != null) {
                ps.setInt(2, notification.getMatchId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, notification.getMessage());
            ps.setBoolean(4, notification.isRead());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setId(keys.getInt(1));
                }
            }
            return notification;
        }
    }

    public List<Notification> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, match_id, message, is_read, created_at " +
                     "FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));
                    int matchId = rs.getInt("match_id");
                    n.setMatchId(rs.wasNull() ? null : matchId);
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    results.add(n);
                }
            }
        }
        return results;
    }

    public void markRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.executeUpdate();
        }
    }
}
