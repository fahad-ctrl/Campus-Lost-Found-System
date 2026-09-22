package com.campus.lostfound.dao;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the items table (both LOST and FOUND reports).
 * All queries use PreparedStatement.
 */
public class ItemDAO {

    private static final String SELECT_BASE =
            "SELECT i.id, i.reporter_id, i.item_type, i.category_id, c.name AS category_name, " +
            "       i.location_id, l.name AS location_name, i.item_date, i.description, " +
            "       i.status, i.reference_code " +
            "FROM items i " +
            "JOIN categories c ON c.id = i.category_id " +
            "JOIN locations l ON l.id = i.location_id ";

    public Item insert(Item item) throws SQLException {
        String sql = "INSERT INTO items (reporter_id, item_type, category_id, location_id, item_date, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getReporterId());
            ps.setString(2, item.getItemType().name());
            ps.setInt(3, item.getCategoryId());
            ps.setInt(4, item.getLocationId());
            ps.setDate(5, Date.valueOf(item.getItemDate()));
            ps.setString(6, item.getDescription());
            ps.setString(7, item.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
            return item;
        }
    }

    public Optional<Item> findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.id = ?";
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

    /** Returns every item reported by a specific user, most recent first. */
    public List<Item> findByReporter(int reporterId) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.reporter_id = ? ORDER BY i.created_at DESC";
        List<Item> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Free-text + filter search used by the Search & Matches screen.
     * Any parameter may be null/blank to mean "no filter on this field".
     */
    public List<Item> search(ItemType type, Integer categoryId, Integer locationId, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append("AND i.item_type = ? ");
            params.add(type.name());
        }
        if (categoryId != null) {
            sql.append("AND i.category_id = ? ");
            params.add(categoryId);
        }
        if (locationId != null) {
            sql.append("AND i.location_id = ? ");
            params.add(locationId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND i.description LIKE ? ");
            params.add("%" + keyword + "%");
        }
        sql.append("ORDER BY i.created_at DESC");

        List<Item> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Finds candidate items of the opposite type that are still PENDING,
     * for the MatchEngine to score. Narrowing by category here keeps the
     * comparison set small; the engine applies finer-grained scoring itself.
     */
    public List<Item> findCandidatesForMatching(ItemType oppositeType, int categoryId) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.item_type = ? AND i.category_id = ? AND i.status = 'PENDING'";
        List<Item> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, oppositeType.name());
            ps.setInt(2, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public void updateStatus(int itemId, ItemStatus status) throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    public void setReferenceCode(int itemId, String referenceCode) throws SQLException {
        String sql = "UPDATE items SET reference_code = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceCode);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setReporterId(rs.getInt("reporter_id"));
        item.setItemType(ItemType.valueOf(rs.getString("item_type")));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setLocationId(rs.getInt("location_id"));
        item.setLocationName(rs.getString("location_name"));
        LocalDate date = rs.getDate("item_date").toLocalDate();
        item.setItemDate(date);
        item.setDescription(rs.getString("description"));
        item.setStatus(ItemStatus.valueOf(rs.getString("status")));
        item.setReferenceCode(rs.getString("reference_code"));
        return item;
    }
}
