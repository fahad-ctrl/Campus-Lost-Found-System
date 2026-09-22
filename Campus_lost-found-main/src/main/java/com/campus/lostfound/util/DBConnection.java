package com.campus.lostfound.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central point for obtaining a JDBC connection to the MySQL database.
 * <p>
 * Configuration is read from src/main/resources/db.properties so credentials
 * are never hard-coded in source files. Copy db.properties.example to
 * db.properties and fill in your local MySQL details.
 */
public final class DBConnection {

    private static final String CONFIG_FILE = "/db.properties";
    private static Properties properties;

    private DBConnection() {
        // utility class - no instances
    }

    private static synchronized Properties loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_FILE)) {
                if (in == null) {
                    throw new IllegalStateException(
                            "db.properties not found on classpath. Copy db.properties.example to " +
                            "src/main/resources/db.properties and fill in your MySQL credentials.");
                }
                properties.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load db.properties", e);
            }
        }
        return properties;
    }

    /**
     * Opens a new JDBC connection. Caller is responsible for closing it
     * (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        Properties props = loadProperties();
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
