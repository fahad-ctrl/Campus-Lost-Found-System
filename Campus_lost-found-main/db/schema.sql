-- ============================================================
-- Campus Lost & Found - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_lost_found
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_lost_found;

-- ---------------------------------------------------------
-- USERS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            ENUM('STUDENT','FACULTY','STAFF','ADMIN') DEFAULT 'STUDENT',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------
-- CATEGORIES
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(80) NOT NULL UNIQUE
);

INSERT IGNORE INTO categories (name) VALUES
    ('Electronics'), ('ID Card'), ('Bag'), ('Book'),
    ('Keys'), ('Clothing'), ('Wallet'), ('Other');

-- ---------------------------------------------------------
-- LOCATIONS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS locations (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(120) NOT NULL UNIQUE
);

INSERT IGNORE INTO locations (name) VALUES
    ('Library'), ('Main Cafeteria'), ('Sports Complex'),
    ('Computer Science Block'), ('Auditorium'), ('Hostel Block A'),
    ('Hostel Block B'), ('Main Gate'), ('Parking Lot'), ('Other');

-- ---------------------------------------------------------
-- ITEMS  (lost or found reports)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS items (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    reporter_id     INT             NOT NULL,
    item_type       ENUM('LOST','FOUND') NOT NULL,
    category_id     INT             NOT NULL,
    location_id     INT             NOT NULL,
    item_date       DATE            NOT NULL,
    description     VARCHAR(500)    NOT NULL,
    status          ENUM('PENDING','MATCHED','CLAIMED','CLOSED') DEFAULT 'PENDING',
    reference_code  VARCHAR(12)     UNIQUE,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_reporter  FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_items_category  FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_items_location  FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- ---------------------------------------------------------
-- MATCHES  (candidate pairing between a LOST and a FOUND item)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS matches (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id    INT             NOT NULL,
    found_item_id   INT             NOT NULL,
    score           DECIMAL(5,2)    NOT NULL,
    status          ENUM('SUGGESTED','CONFIRMED','REJECTED') DEFAULT 'SUGGESTED',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_lost   FOREIGN KEY (lost_item_id)  REFERENCES items(id),
    CONSTRAINT fk_matches_found  FOREIGN KEY (found_item_id) REFERENCES items(id),
    CONSTRAINT uq_match_pair UNIQUE (lost_item_id, found_item_id)
);

-- ---------------------------------------------------------
-- NOTIFICATIONS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT             NOT NULL,
    match_id    INT             NULL,
    message     VARCHAR(255)    NOT NULL,
    is_read     BOOLEAN         DEFAULT FALSE,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    CONSTRAINT fk_notifications_match FOREIGN KEY (match_id) REFERENCES matches(id)
);

-- Helpful indexes for the matching engine's lookups
CREATE INDEX idx_items_type_category_location ON items (item_type, category_id, location_id);
CREATE INDEX idx_items_status ON items (status);
