package org.example.MigrationTool;

import lombok.extern.slf4j.Slf4j;
import org.example.MigrationFile.MigrationFile;
import org.example.MigrationFile.MigrationFileReader;
import org.example.Utils.PropertiesUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
/**
 * Handles the migration process. Contains core logic
 */
@Slf4j
public class MigrationTool {
    private final MigrationExecutor migrationExecutor;
    private final Connection connection;
    private final PropertiesUtils propertiesUtils;
    private final MigrationFileReader migrationFileReader = new MigrationFileReader();


    public MigrationTool(MigrationExecutor migrationExecutor, Connection connection, PropertiesUtils propertiesUtils) {
        this.migrationExecutor = migrationExecutor;
        this.connection = connection;
        this.propertiesUtils = propertiesUtils;
    }
    //Executes the migration process in a single transaction,
    // checks if the db is locked or not, rollbacks the transaction in case of any error
    public void migrate() throws SQLException {
        MigrationLock migrationLock = new MigrationLock(connection);

        if (migrationLock.isLocked()) {
            log.error("Cannot start migration. Database is locked.");
            throw new IllegalStateException("Database is locked by another process.");
        }

        migrationLock.lock(propertiesUtils.getUsername());

        log.info("Migration starts");

        try {
            connection.setAutoCommit(false);

            String currentVersion = migrationExecutor.getCurrentVersion();
            log.info("Current database version: "+(currentVersion != null ? currentVersion : "None"));

            List<MigrationFile> migrationFiles = migrationFileReader.getMigrationFiles();
            for (MigrationFile migrationFile : migrationFiles) {
                if (currentVersion == null || migrationFile.getVersion().compareTo(currentVersion) > 0) {
                    migrationExecutor.applyMigration(
                            migrationFile.getVersion(),
                            migrationFile.getDescription(),
                            migrationFile.getSql()
                    );
                }}
            connection.commit();
            log.info("All migrations applied successfully");


        }catch (SQLException | IOException e) {

            connection.rollback();
            log.error("Migration process failed: {}", e.getMessage(), e);
            throw new SQLException("Migration process failed", e);
        } finally {
            migrationLock.unlock();
            connection.setAutoCommit(true);
        }
        log.info("Migration process ends");
    }
    //Executes the rollback process in a single transaction,
    // checks if the db is locked or not, rollbacks the transaction in case of any error
    public void rollback(String targetVersion) throws SQLException {
        MigrationLock migrationLock = new MigrationLock(connection);
        if (migrationLock.isLocked()) {
            log.error("Cannot start rollback. Database is locked.");
            throw new IllegalStateException("Database is locked by another process.");
        }

        migrationLock.lock(propertiesUtils.getUsername());


        try {
            connection.setAutoCommit(false);
            String currentVersion = migrationExecutor.getCurrentVersion();
            if (currentVersion == null || targetVersion.compareTo(currentVersion) >= 0) {
                log.info("No rollback needed. Target version: " + targetVersion + ", Current version: " + currentVersion);
                return;
            }

            log.info("Current database version: " + currentVersion);

            // Fetch rollback files for the range (targetVersion, currentVersion]
            List<MigrationFile> rollbackFiles = migrationFileReader.getRollbackFiles(targetVersion, currentVersion);
            for (MigrationFile rollbackFile : rollbackFiles) {
                migrationExecutor.rollbackMigration(
                        rollbackFile.getVersion(),
                        rollbackFile.getDescription(),
                        rollbackFile.getSql()
                );
            }

            // Remove schema_version entries for versions higher than the target version
            String deleteVersionsSql = "DELETE FROM schema_version WHERE version > ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteVersionsSql)) {
                preparedStatement.setString(1, targetVersion);
                preparedStatement.executeUpdate();
            }
            log.debug("just before the commit");

            connection.commit();
            log.info("Rollback completed successfully to version: " + targetVersion);
        } catch (SQLException | IOException e) {

            connection.rollback();
            log.error("Rollback process failed: {}", e.getMessage(), e);
            throw new SQLException("Rollback process failed", e);
        } finally {
            migrationLock.unlock();
            connection.setAutoCommit(true);
            log.debug("set autocommit true");

        }
        log.debug("Rollback process ends");
    }
    /**
     * Displays the current status of migrations, including the current version and applied migrations.
     *
     * @throws SQLException if a database error occurs
     */
    public void status() throws SQLException{
        try {
            List<String> appliedMigrations = migrationExecutor.getAppliedMigrations();
            String currentVersion = migrationExecutor.getCurrentVersion();

            if (currentVersion != null) {
                System.out.println("Current database version: " + currentVersion);
            } else {
                System.out.println("No migrations have been applied yet.");
            }

            System.out.println("Applied migrations:");
            if (appliedMigrations.isEmpty()) {
                System.out.println("No migrations applied.");
            } else {
                for (String version : appliedMigrations) {
                    System.out.println("- " + version);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching status: " + e.getMessage());
        }
    }
}


