-- ============================================================
-- Campus Lost & Found - SQLite Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name       TEXT            NOT NULL,
    email           TEXT            NOT NULL UNIQUE,
    password_hash   TEXT            NOT NULL,
    role            TEXT            DEFAULT 'STUDENT',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT            NOT NULL UNIQUE
);

INSERT OR IGNORE INTO categories (id, name) VALUES
    (1, 'Electronics'),
    (2, 'ID Card'),
    (3, 'Bag'),
    (4, 'Book'),
    (5, 'Keys'),
    (6, 'Clothing'),
    (7, 'Wallet'),
    (8, 'Other');

CREATE TABLE IF NOT EXISTS locations (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT            NOT NULL UNIQUE
);

INSERT OR IGNORE INTO locations (id, name) VALUES
    (1, 'Library'),
    (2, 'Main Cafeteria'),
    (3, 'Sports Complex'),
    (4, 'Computer Science Block'),
    (5, 'Auditorium'),
    (6, 'Hostel Block A'),
    (7, 'Hostel Block B'),
    (8, 'Main Gate'),
    (9, 'Parking Lot'),
    (10, 'Other');

CREATE TABLE IF NOT EXISTS items (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_id     INTEGER         NOT NULL,
    item_type       TEXT            NOT NULL,
    category_id     INTEGER         NOT NULL,
    location_id     INTEGER         NOT NULL,
    item_date       TEXT            NOT NULL,
    description     TEXT            NOT NULL,
    status          TEXT            DEFAULT 'PENDING',
    reference_code  TEXT            UNIQUE,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS matches (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    lost_item_id    INTEGER         NOT NULL,
    found_item_id   INTEGER         NOT NULL,
    score           REAL            NOT NULL,
    status          TEXT            DEFAULT 'SUGGESTED',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lost_item_id)  REFERENCES items(id),
    FOREIGN KEY (found_item_id) REFERENCES items(id),
    UNIQUE (lost_item_id, found_item_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER         NOT NULL,
    match_id    INTEGER         NULL,
    message     TEXT            NOT NULL,
    is_read     INTEGER         DEFAULT 0,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)  REFERENCES users(id),
    FOREIGN KEY (match_id) REFERENCES matches(id)
);

CREATE INDEX IF NOT EXISTS idx_items_type_category_location ON items (item_type, category_id, location_id);
CREATE INDEX IF NOT EXISTS idx_items_status ON items (status);
