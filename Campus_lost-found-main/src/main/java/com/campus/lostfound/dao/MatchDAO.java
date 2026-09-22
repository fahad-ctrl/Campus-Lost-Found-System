package com.campus.lostfound.dao;

import com.campus.lostfound.model.MatchRecord;
import com.campus.lostfound.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDAO {

    private static final String SELECT_BASE =
            "SELECT m.id, m.lost_item_id, m.found_item_id, m.score, m.status, " +
            "       li.description AS lost_desc, fi.description AS found_desc " +
            "FROM matches m " +
            "JOIN items li ON li.id = m.lost_item_id " +
            "JOIN items fi ON fi.id = m.found_item_id ";

    public MatchRecord insert(MatchRecord match) throws SQLException {
        String sql = "INSERT INTO matches (lost_item_id, found_item_id, score, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, match.getLostItemId());
            ps.setInt(2, match.getFoundItemId());
            ps.setDouble(3, match.getScore());
            ps.setString(4, match.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    match.setId(keys.getInt(1));
                }
            }
            return match;
        }
    }

    /** Prevents duplicate match rows from being created for the same pair. */
    public boolean existsForPair(int lostItemId, int foundItemId) throws SQLException {
        String sql = "SELECT 1 FROM matches WHERE lost_item_id = ? AND found_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lostItemId);
            ps.setInt(2, foundItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<MatchRecord> findByUserId(int userId) throws SQLException {
        String sql = SELECT_BASE +
                "WHERE li.reporter_id = ? OR fi.reporter_id = ? " +
                "ORDER BY m.created_at DESC";
        List<MatchRecord> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public Optional<MatchRecord> findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE m.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updateStatus(int matchId, MatchRecord.Status status) throws SQLException {
        String sql = "UPDATE matches SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, matchId);
            ps.executeUpdate();
        }
    }

    private MatchRecord mapRow(ResultSet rs) throws SQLException {
        MatchRecord match = new MatchRecord();
        match.setId(rs.getInt("id"));
        match.setLostItemId(rs.getInt("lost_item_id"));
        match.setFoundItemId(rs.getInt("found_item_id"));
        match.setScore(rs.getDouble("score"));
        match.setStatus(MatchRecord.Status.valueOf(rs.getString("status")));
        match.setLostItemDescription(rs.getString("lost_desc"));
        match.setFoundItemDescription(rs.getString("found_desc"));
        return match;
    }
}
