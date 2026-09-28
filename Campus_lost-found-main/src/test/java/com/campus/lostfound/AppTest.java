package com.campus.lostfound;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.MatchDAO;
import com.campus.lostfound.dao.NotificationDAO;
import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.*;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.MatchEngine;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.service.ReportService;
import com.campus.lostfound.util.DBConnection;
import com.campus.lostfound.util.ItemJsonHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @BeforeAll
    public static void setUp() throws Exception {
        // Ensure database connection and tables are initialized
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn);
        }
    }

    @Test
    public void testDatabaseConnection() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    public void testAuthServiceRegistrationAndLogin() throws Exception {
        AuthService authService = new AuthService();
        String email = "testuser_" + System.currentTimeMillis() + "@campus.edu";
        User user = authService.register("Test Student", email, "secret123", "STUDENT");

        assertNotNull(user);
        assertTrue(user.getId() > 0);
        assertEquals("Test Student", user.getFullName());

        // Test login success
        Optional<User> loggedIn = authService.login(email, "secret123");
        assertTrue(loggedIn.isPresent());
        assertEquals(user.getId(), loggedIn.get().getId());

        // Test login failure with wrong password
        Optional<User> failed = authService.login(email, "wrongpassword");
        assertTrue(failed.isEmpty());
    }

    @Test
    public void testItemDAOAndReportService() throws Exception {
        AuthService authService = new AuthService();
        String email = "reporter_" + System.currentTimeMillis() + "@campus.edu";
        User user = authService.register("Reporter User", email, "pass12345", "STUDENT");

        ReportService reportService = new ReportService();
        ReportService.ReportResult result = reportService.reportItem(
                user.getId(),
                ItemType.LOST,
                1, // Electronics
                1, // Library
                LocalDate.now(),
                "Lost a blue Dell laptop with stickers"
        );

        assertNotNull(result);
        assertNotNull(result.item);
        assertTrue(result.item.getId() > 0);
        assertEquals(ItemType.LOST, result.item.getItemType());
        assertEquals("Electronics", result.item.getCategoryName());
        assertEquals("Library", result.item.getLocationName());

        ItemDAO itemDAO = new ItemDAO();
        List<Item> myItems = itemDAO.findByReporter(user.getId());
        assertFalse(myItems.isEmpty());
        assertEquals("Lost a blue Dell laptop with stickers", myItems.get(0).getDescription());

        // Test search
        List<Item> searchResults = itemDAO.search(ItemType.LOST, 1, 1, "laptop");
        assertFalse(searchResults.isEmpty());
    }

    @Test
    public void testMatchEngineAndClaimService() throws Exception {
        AuthService authService = new AuthService();
        String user1Email = "loser_" + System.currentTimeMillis() + "@campus.edu";
        String user2Email = "finder_" + System.currentTimeMillis() + "@campus.edu";
        User user1 = authService.register("Lost Owner", user1Email, "password123", "STUDENT");
        User user2 = authService.register("Kind Finder", user2Email, "password123", "STAFF");

        ReportService reportService = new ReportService();

        // 1. Report lost item
        reportService.reportItem(
                user1.getId(),
                ItemType.LOST,
                2, // ID Card
                2, // Main Cafeteria
                LocalDate.now(),
                "Lost my student ID card near cafeteria counter"
        );

        // 2. Report found item (same category, same location, same date) -> score should trigger match
        ReportService.ReportResult foundResult = reportService.reportItem(
                user2.getId(),
                ItemType.FOUND,
                2, // ID Card
                2, // Main Cafeteria
                LocalDate.now(),
                "Found student ID card on table 4"
        );

        assertFalse(foundResult.matches.isEmpty(), "MatchEngine should produce a match for same category/location/date");
        MatchRecord match = foundResult.matches.get(0);
        assertTrue(match.getScore() >= 60.0);

        // 3. Confirm match and verify reference code
        ClaimService claimService = new ClaimService();
        String refCode = claimService.confirmMatch(match.getId());
        assertNotNull(refCode);
        assertEquals(8, refCode.length());

        // 4. Verify claim with code
        boolean claimSuccess = claimService.claimWithCode(match.getFoundItemId(), refCode);
        assertTrue(claimSuccess);

        // 5. Verify notification was created
        NotificationService notificationService = new NotificationService();
        List<Notification> user1Notifications = notificationService.getNotificationsForUser(user1.getId());
        assertFalse(user1Notifications.isEmpty());
        assertTrue(user1Notifications.get(0).getMessage().contains("ID Card"));
    }

    @Test
    public void testItemJsonHandler() throws Exception {
        Item item = new Item(1, ItemType.LOST, 1, 1, LocalDate.of(2026, 9, 26), "Test Item for JSON");
        item.setId(999);
        item.setCategoryName("Electronics");
        item.setLocationName("Library");

        ItemJsonHandler.writeItems(List.of(item));

        File file = new File("items.json");
        assertTrue(file.exists());

        List<Item> readItems = ItemJsonHandler.readItems();
        assertFalse(readItems.isEmpty());
        assertEquals(999, readItems.get(0).getId());
        assertEquals(LocalDate.of(2026, 9, 26), readItems.get(0).getItemDate());

        // Clean up
        file.delete();
    }
}
