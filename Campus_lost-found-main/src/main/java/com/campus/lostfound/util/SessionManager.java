package com.campus.lostfound.util;

import com.campus.lostfound.model.User;

/**
 * Holds the currently logged-in user for the lifetime of the application.
 * Simple singleton - fine for a single-user desktop session.
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
