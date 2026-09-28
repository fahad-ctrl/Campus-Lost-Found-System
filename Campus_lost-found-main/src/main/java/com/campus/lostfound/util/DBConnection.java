package com.campus.lostfound.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central point for obtaining a JDBC connection to the SQLite database.
 * Configuration is read from src/main/resources/com/campus/lostfound/db.properties.
 */
public final class DBConnection {

    private static final String CONFIG_FILE_PRIMARY = "/com/campus/lostfound/db.properties";
    private static final String CONFIG_FILE_FALLBACK = "/db.properties";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC driver not found", e);
        }
    }

    private static Properties properties;

    private DBConnection() {
        // utility class – no instances
    }

    private static synchronized Properties loadProperties() {
        if (properties == null) {
            properties = new Properties();
            InputStream in = DBConnection.class.getResourceAsStream(CONFIG_FILE_PRIMARY);
            if (in == null) {
                in = DBConnection.class.getResourceAsStream(CONFIG_FILE_FALLBACK);
            }
            if (in == null) {
                throw new IllegalStateException(
                        "db.properties not found on classpath. Ensure db.properties exists in src/main/resources.");
            }
            try (InputStream is = in) {
                properties.load(is);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load db.properties", e);
            }
        }
        return properties;
    }

    /**
     * Opens a new JDBC connection. Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        Properties props = loadProperties();
        String url = props.getProperty("db.url");
        if (url == null || url.isBlank()) {
            url = "jdbc:sqlite:database.db";
        }
        Connection conn = DriverManager.getConnection(url);
        // Initialise SQLite schema if needed
        initializeDatabase(conn);
        return conn;
    }

    /**
     * Creates the SQLite schema on first run by executing schema_sqlite.sql.
     */
    private static void initializeDatabase(Connection conn) {
        try {
            var meta = conn.getMetaData();
            try (var rs = meta.getTables(null, null, "users", null)) {
                if (rs.next()) {
                    return; // tables already exist
                }
            }

            // Load SQLite schema from classpath resource
            String sql;
            try (InputStream in = DBConnection.class.getResourceAsStream("/com/campus/lostfound/schema_sqlite.sql")) {
                if (in != null) {
                    sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                } else {
                    // Fallback to checking disk paths
                    Path path = Paths.get("db", "schema_sqlite.sql");
                    if (!Files.exists(path)) {
                        path = Paths.get("Campus_lost-found-main", "db", "schema_sqlite.sql");
                    }
                    if (!Files.exists(path)) {
                        throw new IllegalStateException("schema_sqlite.sql not found in resources or filesystem.");
                    }
                    sql = Files.readString(path, StandardCharsets.UTF_8);
                }
            }

            // Execute each statement separated by ';'
            for (String stmt : sql.split(";")) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    try (var statement = conn.createStatement()) {
                        statement.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise SQLite database", e);
        }
    }
}
