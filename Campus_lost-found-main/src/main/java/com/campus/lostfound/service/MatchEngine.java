package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.MatchDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchRecord;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares a newly reported item against existing opposite-type items
 * and produces scored candidate matches.
 * <p>
 * Scoring model (out of 100):
 *  - Category match is a hard requirement (candidates are pre-filtered on it).
 *  - Location match:      50 points if identical, 20 points otherwise.
 *  - Date proximity:      up to 50 points, decreasing the further apart the dates are.
 * A match is only persisted and notified when its score clears MATCH_THRESHOLD.
 */
public class MatchEngine {

    public static final double MATCH_THRESHOLD = 60.0;
    private static final int MAX_DATE_WINDOW_DAYS = 14;

    private final ItemDAO itemDAO = new ItemDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final NotificationService notificationService = new NotificationService();

    /**
     * Runs after a new item is reported. Finds candidates of the opposite
     * type in the same category, scores each one, and persists + notifies
     * for every candidate that clears the threshold.
     *
     * @return the list of newly created match records
     */
    public List<MatchRecord> findAndSaveMatches(Item newItem) throws SQLException {
        ItemType oppositeType = (newItem.getItemType() == ItemType.LOST) ? ItemType.FOUND : ItemType.LOST;
        List<Item> candidates = itemDAO.findCandidatesForMatching(oppositeType, newItem.getCategoryId());

        List<MatchRecord> created = new ArrayList<>();
        for (Item candidate : candidates) {
            double score = score(newItem, candidate);
            if (score >= MATCH_THRESHOLD) {
                Item lostItem = (newItem.getItemType() == ItemType.LOST) ? newItem : candidate;
                Item foundItem = (newItem.getItemType() == ItemType.FOUND) ? newItem : candidate;

                if (matchDAO.existsForPair(lostItem.getId(), foundItem.getId())) {
                    continue; // already suggested previously
                }

                MatchRecord match = new MatchRecord(lostItem.getId(), foundItem.getId(), score);
                matchDAO.insert(match);
                created.add(match);

                notificationService.notifyMatch(lostItem, foundItem, match);
            }
        }
        return created;
    }

    /** Scores similarity between a lost item and a found item, 0-100. */
    public double score(Item a, Item b) {
        double score = 0;

        // Location: exact match scores highest, otherwise a small baseline.
        if (a.getLocationId() == b.getLocationId()) {
            score += 50;
        } else {
            score += 20;
        }

        // Date proximity: full marks for the same day, tapering to zero
        // beyond the matching window.
        long daysApart = Math.abs(ChronoUnit.DAYS.between(a.getItemDate(), b.getItemDate()));
        if (daysApart == 0) {
            score += 50;
        } else if (daysApart <= MAX_DATE_WINDOW_DAYS) {
            double fraction = 1.0 - ((double) daysApart / MAX_DATE_WINDOW_DAYS);
            score += 50 * fraction;
        }

        return Math.round(score * 100.0) / 100.0;
    }
}
