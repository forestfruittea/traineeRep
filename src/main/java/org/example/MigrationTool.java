package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MigrationTool {
    private final MigrationExecutor migrationExecutor;
    private final Connection connection;
    private final PropertiesUtils propertiesUtils;
    private static final Logger logger = LoggerFactory.getLogger(MigrationTool.class);
    private final MigrationFileReader migrationFileReader = new MigrationFileReader();


    public MigrationTool(MigrationExecutor migrationExecutor, Connection connection, PropertiesUtils propertiesUtils) {
        this.migrationExecutor = migrationExecutor;
        this.connection = connection;
        this.propertiesUtils = propertiesUtils;
    }

    public void executeMigration() throws SQLException {
        MigrationLockService migrationLockService = new MigrationLockService(connection);

        if (migrationLockService.isLocked()) {
            logger.error("Cannot start migration. Database is locked.");
            throw new IllegalStateException("Database is locked by another process.");
        }

        migrationLockService.lock(propertiesUtils.getUsername());

        logger.info("Migration starts");

        try {
            connection.setAutoCommit(false);

            String currentVersion = migrationExecutor.getCurrentVersion();
            logger.info("Current database version: "+(currentVersion != null ? currentVersion : "None"));

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
                logger.info("All migrations applied successfully");


        }catch (SQLException | IOException e) {

            connection.rollback();
            logger.error("Migration process failed: {}", e.getMessage(), e);
            throw new SQLException("Migration process failed", e);
        } finally {
            migrationLockService.unlock();
            connection.setAutoCommit(true);
        }
        logger.debug("Migration process ends");
    }
    public void executeRollback(String targetVersion) throws SQLException {
        MigrationLockService migrationLockService = new MigrationLockService(connection);
        if (migrationLockService.isLocked()) {
            logger.error("Cannot start rollback. Database is locked.");
            throw new IllegalStateException("Database is locked by another process.");
        }

        logger.info("Rollback starts for target version: " + targetVersion);
        migrationLockService.lock(propertiesUtils.getUsername());


        try {
            connection.setAutoCommit(false);
            String currentVersion = migrationExecutor.getCurrentVersion();
            if (currentVersion == null || targetVersion.compareTo(currentVersion) >= 0) {
                logger.info("No rollback needed. Target version: " + targetVersion + ", Current version: " + currentVersion);
                return;
            }

            logger.info("Current database version: " + currentVersion);

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
            logger.debug("just before the commit");

            connection.commit();
            logger.info("Rollback completed successfully to version: " + targetVersion);
        } catch (SQLException | IOException e) {

            connection.rollback();
            logger.error("Rollback process failed: {}", e.getMessage(), e);
            throw new SQLException("Rollback process failed", e);
        } finally {
            migrationLockService.unlock();
            connection.setAutoCommit(true);
            logger.debug("set autocommit true");

        }
        logger.debug("Rollback process ends");
    }
}
