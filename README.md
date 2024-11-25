# Database Migration Tool

A Java-based database migration tool that allows for applying, rolling back, and tracking migrations on a PostgreSQL database. It includes features like migration locking, error handling, and detailed reporting.

## Features
**Migration Process:** `Applies database migrations stored in SQL files.`

**Rollback Process:** `Rolls back to a specified version.`

**Locking:** `Prevents concurrent migration execution using a migration lock.`

**Reporting:** `Generates detailed migration reports in JSON format.`

**Version Tracking:** `Tracks the applied migrations in a schema_version table.`


## Requirements

- Java 21
- PostgreSQL
- Maven 3.9.9

## Detailed information about core classes

**MigrationTool** - `Handles the migration process. Contains core logic.`

**MigrationLock** - `Service for acquiring and releasing a lock for the migration process.`

**MigrationExecutor** - `Responsible for executing database migrations.`

**ReportWriter** - `Utility class to write reports to a file in JSON format.`

**MigrationFileReader** - `Responsible for reading migration and rollback SQL files from the filesystem.`

**MigrateCommand** - `CLI Command to apply all available migrations.`

**RollbackCommand** - `CLI Command to rollback db state to a specified version.`

**StatusCommand** - `CLI Command to display the current status of the database migrations.`

## Setup

1. Clone the repository:
    ```bash
    git clone <repository-url>
    cd <repository-directory>
    ```
2. Configure the `application.properties` file:
   
The paths to migration/rollback files and reports are also customizable, but you can leave the defaults
   
```properties
    db.url=jdbc:postgresql://<your-database-host>:<port>/<database-name>
    db.username=<your-database-username>
    db.password=<your-database-password>
   
    migrations.path=src/main/resources/migrations
    rollbacks.path=src/main/resources/rollbacks
    reports.path=reports
   ```
3. Build the project:
    ```bash
    mvn clean package
    ```

## Usage
```bash
java -jar <your-path-to-the-jar-file>
```
**Migration Tool provides:**

**migrate** - `Apply all migrations`

**rollback -v "target_version"** - `Rollbacks to the specified version`

**status** - `Displays the current status of migrations`

## Examples
### Migration
To apply migration files:
```bash
java -jar target\MigrationManager-1.0-SNAPSHOT.jar migrate

..........................................................
2024-11-25 15:30:38 [main] INFO - Migration starts
2024-11-25 15:30:38 [main] INFO - Current database version: 1
2024-11-25 15:30:38 [main] INFO - Migrating to version: 2
2024-11-25 15:30:38 [main] INFO - Migration applied successfully for version: 2
2024-11-25 15:30:38 [main] INFO - Migrating to version: 3
2024-11-25 15:30:38 [main] INFO - Migration applied successfully for version: 3
2024-11-25 15:30:38 [main] INFO - All migrations applied successfully
2024-11-25 15:30:38 [main] INFO - Migration process ends

```
### Rollback
To apply rollback files:
```bash
java -jar target\MigrationManager-1.0-SNAPSHOT.jar rollback -v 1

................................................................
2024-11-25 15:45:47 [main] INFO - Current database version: 3
2024-11-25 15:45:47 [main] INFO - Rolling back version: 2
2024-11-25 15:45:47 [main] INFO - Rollback SQL applied for version: 2
2024-11-25 15:45:47 [main] INFO - Rolling back version: 1
2024-11-25 15:45:47 [main] INFO - Rollback SQL applied for version: 1
2024-11-25 15:45:47 [main] INFO - Rollback completed successfully to version: 1

```
### Status
To check applied migrations and the db state:
```bash
java -jar target\MigrationManager-1.0-SNAPSHOT.jar status

.........................................................
Current database version: 1
Applied migrations:
- 1
```