package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchRecord;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Validates and saves new lost/found reports, then triggers the
 * matching engine so the reporter gets immediate feedback if a
 * candidate match already exists.
 */
public class ReportService {

    private final ItemDAO itemDAO = new ItemDAO();
    private final MatchEngine matchEngine = new MatchEngine();

    public static class ReportResult {
        public final Item item;
        public final List<MatchRecord> matches;

        public ReportResult(Item item, List<MatchRecord> matches) {
            this.item = item;
            this.matches = matches;
        }
    }

    public ReportResult reportItem(int reporterId, ItemType type, int categoryId, int locationId,
                                    LocalDate itemDate, String description) throws SQLException {
        validate(itemDate, description);

        Item item = new Item(reporterId, type, categoryId, locationId, itemDate, description.trim());
        item.setStatus(ItemStatus.PENDING);
        itemDAO.insert(item);

        List<MatchRecord> matches = matchEngine.findAndSaveMatches(item);
        if (!matches.isEmpty()) {
            itemDAO.updateStatus(item.getId(), ItemStatus.MATCHED);
            item.setStatus(ItemStatus.MATCHED);
        }

        return new ReportResult(item, matches);
    }

    private void validate(LocalDate itemDate, String description) {
        if (itemDate == null) {
            throw new IllegalArgumentException("Please select the date the item was lost or found.");
        }
        if (itemDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Please add a short description.");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Description must be under 500 characters.");
        }
    }
}
