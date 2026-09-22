# Campus Lost & Found

A centralized JavaFX desktop application to report, match, and recover lost campus belongings.

**Stack:** Java 17 &middot; JavaFX &middot; JDBC &middot; MySQL &middot; Maven

## Architecture

This project follows a strict layered architecture — each layer only talks to the one directly next to it:

```
Presentation (JavaFX/FXML)  →  Controllers  →  Business Logic (Services)  →  Data Access (DAO/JDBC)  →  MySQL
```

| Layer | Package | Contents |
|---|---|---|
| Presentation | `resources/.../fxml`, `.../css` | Login, Dashboard, Report Wizard, Search & Matches views |
| Controllers | `controller` | `LoginController`, `ReportController`, `MatchController`, `DashboardController` |
| Business Logic | `service` | `ReportService`, `MatchEngine`, `NotificationService`, `ClaimService`, `AuthService` |
| Data Access | `dao` | `ItemDAO`, `UserDAO`, `MatchDAO`, `NotificationDAO` — all using `PreparedStatement` |
| Database | `db/schema.sql` | `users`, `items`, `matches`, `notifications`, `categories`, `locations` |

## Getting Started

### 1. Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8+

### 2. Set up the database
```bash
mysql -u root -p < db/schema.sql
```
This creates the `campus_lost_found` database with all tables and seed data for categories/locations.

### 3. Configure your database credentials
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```
Edit `db.properties` and set your MySQL username/password. This file is git-ignored so credentials are never committed.

### 4. Run the application
```bash
mvn clean javafx:run
```

## How It Works

1. **Report** — A user logs a lost or found item (category, location, date, description) through the Report Wizard.
2. **Match** — `MatchEngine` scores the new item against opposite-type items in the same category, based on location and date proximity.
3. **Notify** — Any match scoring above the threshold (60/100) is saved and both users get an in-app notification.
4. **Claim** — Confirming a match generates a unique reference code; the claimant must present it to complete handover.

## Project Structure

```
campus-lost-and-found/
├── pom.xml
├── db/
│   └── schema.sql
└── src/main/
    ├── java/com/campus/lostfound/
    │   ├── Main.java
    │   ├── model/          # User, Item, MatchRecord, Notification, enums
    │   ├── dao/             # JDBC data access (PreparedStatement only)
    │   ├── service/         # Business logic (ReportService, MatchEngine, etc.)
    │   ├── controller/      # JavaFX FXML controllers
    │   └── util/            # DBConnection, SceneManager, SessionManager
    └── resources/
        ├── db.properties.example
        └── com/campus/lostfound/
            ├── fxml/         # login, register, dashboard, report_wizard, search_matches
            └── css/          # style.css
```

## Notes for Contributors

- Category/location dropdown lists in `ReportController` and `MatchController` currently use hard-coded lists that match the seed data order in `db/schema.sql`. If you add a `CategoryDAO`/`LocationDAO`, swap these out to load dynamically.
- All SQL goes through `PreparedStatement` — never concatenate user input into a query string.
- Passwords are hashed with bcrypt (`jbcrypt`) before storage; the app never stores or logs plain-text passwords.
- This project was built and syntax-checked in a sandboxed environment without access to Maven Central, so a full `mvn compile` has not been run end-to-end. Run `mvn clean javafx:run` locally as the first step and open an issue/PR if anything needs adjusting.

## License

Academic group project — Department of Computer Science.
