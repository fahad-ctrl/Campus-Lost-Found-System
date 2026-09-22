package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.MatchDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.MatchRecord;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Generates the reference code that verifies a claim, and confirms
 * handovers once both parties agree a match is correct.
 */
public class ClaimService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no ambiguous chars
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ItemDAO itemDAO = new ItemDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    /**
     * Confirms a suggested match: generates a reference code on the found
     * item and marks both items MATCHED so they can proceed to handover.
     */
    public String confirmMatch(int matchId) throws SQLException {
        MatchRecord match = matchDAO.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found."));

        matchDAO.updateStatus(matchId, MatchRecord.Status.CONFIRMED);

        String referenceCode = generateReferenceCode();
        itemDAO.setReferenceCode(match.getFoundItemId(), referenceCode);
        itemDAO.updateStatus(match.getLostItemId(), ItemStatus.MATCHED);
        itemDAO.updateStatus(match.getFoundItemId(), ItemStatus.MATCHED);

        return referenceCode;
    }

    /**
     * Verifies a claimant's code against the found item's stored reference
     * code. On success, both items are marked CLAIMED.
     */
    public boolean claimWithCode(int foundItemId, String enteredCode) throws SQLException {
        Optional<Item> foundItemOpt = itemDAO.findById(foundItemId);
        if (foundItemOpt.isEmpty()) {
            return false;
        }
        Item foundItem = foundItemOpt.get();
        if (foundItem.getReferenceCode() == null || enteredCode == null) {
            return false;
        }
        boolean matches = foundItem.getReferenceCode().equalsIgnoreCase(enteredCode.trim());
        if (matches) {
            itemDAO.updateStatus(foundItemId, ItemStatus.CLAIMED);
        }
        return matches;
    }

    private String generateReferenceCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
