package com.campus.lostfound.service;

import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles registration and login. Passwords are always hashed with
 * bcrypt before hitting the database - never stored or compared in plain text.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User register(String fullName, String email, String rawPassword, String role) throws SQLException {
        if (userDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        User user = new User(0, fullName, email, hash, role);
        return userDAO.insert(user);
    }

    /**
     * Returns the authenticated user, or empty if the email/password
     * combination is invalid.
     */
    public Optional<User> login(String email, String rawPassword) throws SQLException {
        Optional<User> found = userDAO.findByEmail(email);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        User user = found.get();
        if (BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
