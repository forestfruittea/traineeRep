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
    private static final Logger logger = LoggerFactory.getLogger(MigrationTool.class);
    private final MigrationFileReader migrationFileReader = new MigrationFileReader();

    public MigrationTool(MigrationExecutor migrationExecutor, Connection connection) {
        this.migrationExecutor = migrationExecutor;
        this.connection = connection;
    }

    public void executeMigration() throws SQLException {
        migrationExecutor.initializeSchemaTable();
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
            connection.setAutoCommit(true);
        }
        logger.debug("Migration process ends");
    }
    public void executeRollback(String version) throws SQLException {
        logger.info("Rollback starts for version: " + version);

        try {
            connection.setAutoCommit(false);

            String currentVersion = migrationExecutor.getCurrentVersion();
            logger.info("Current database version: " + (currentVersion != null ? currentVersion : "None"));

            // Fetch the rollback files for the specific version
            List<MigrationFile> rollbackFiles = migrationFileReader.getRollbackFiles(version);
            for (MigrationFile rollbackFile : rollbackFiles) {
                migrationExecutor.rollbackMigration(
                        rollbackFile.getVersion(),
                        rollbackFile.getDescription(),
                        rollbackFile.getSql()
                );
            }

            // Remove the specific entry from the schema_version table (this version)
            String deleteVersionSql = "DELETE FROM schema_version WHERE version = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteVersionSql)) {
                preparedStatement.setString(1, version);
                preparedStatement.executeUpdate();
            }

            connection.commit();
            logger.info("Rollback applied successfully for version: " + version);
        } catch (SQLException | IOException e) {
            connection.rollback();
            logger.error("Rollback process failed: {}", e.getMessage(), e);
            throw new SQLException("Rollback process failed", e);
        } finally {
            connection.setAutoCommit(true);
        }
        logger.debug("Rollback process ends");
    }
}
