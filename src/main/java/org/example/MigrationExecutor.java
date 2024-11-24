package org.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class MigrationExecutor {
    private final Connection connection;
    private final PropertiesUtils config = new PropertiesUtils();
    private final String filePath = config.getReportsPath();
    public void initializeSchemaTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version VARCHAR(50) PRIMARY KEY,
                    description VARCHAR(255),
                    applied_at TIMESTAMP
                );
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    public void initializeLockTable() throws SQLException{
        String sql = """
                CREATE TABLE IF NOT EXISTS migration_lock (
                    id INT PRIMARY KEY CHECK (id = 1),
                    is_locked BOOLEAN NOT NULL,
                    locked_at TIMESTAMP,
                    locked_by VARCHAR(255)
                );
                """;
        try (Statement statement = connection.createStatement()){
            statement.execute(sql);
        }
    }

    public List<String> getAppliedMigrations() throws SQLException {
        String sql = "SELECT version FROM schema_version ORDER BY version";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            List<String> appliedMigrations = new java.util.ArrayList<>();
            while (resultSet.next()) {
                appliedMigrations.add(resultSet.getString("version"));
            }
            return appliedMigrations;
        }
    }
    public String getCurrentVersion() throws SQLException{
        String sql = "SELECT version FROM schema_version ORDER BY version DESC LIMIT 1";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getString("version");
            }
            return null;
        }
    }

    public void applyMigration(String version, String description, String sql) throws SQLException {
        MigrationReport migrationReport = null;
        log.debug("Migrating to version: {}", version);
        try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            String insertVersionSql = "INSERT INTO schema_version (version, description, applied_at) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertVersionSql)) {
                preparedStatement.setString(1, version);
                preparedStatement.setString(2, description);
                preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.executeUpdate();
            }
            migrationReport = MigrationReport.builder()
                    .version(version)
                    .description(description)
                    .status("SUCCESS")
                    .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            log.info("Migration applied successfully for version: {}", version);
        } catch (SQLException e) {
            migrationReport = MigrationReport.builder()
                    .version(version)
                    .description(description)
                    .status("FAILED")
                    .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .errorMessage(e.getMessage())
                    .build();

            log.error("Migration failed for version: {}", version, e);
            throw e;
        } finally {
            if(migrationReport != null){
            ReportWriter.writeReport(migrationReport, filePath+"/migration_" + version + ".json");
            }
        }
    }

    public void rollbackMigration(String version, String description, String sql) throws SQLException {
        log.debug("Rolling back version: {}", version);
        MigrationReport migrationReport = null;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);

            migrationReport = MigrationReport.builder()
                    .version(version)
                    .description(description)
                    .status("SUCCESS")
                    .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

        log.info("Rollback SQL applied for version: " + version);
    } catch (SQLException e){
            migrationReport = MigrationReport.builder()
                    .version(version)
                    .description(description)
                    .status("FAILED")
                    .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .errorMessage(e.getMessage())
                    .build();
            log.error("Rollback failed for version: {}", version, e);
            throw e;
        } if(migrationReport != null){
            ReportWriter.writeReport(migrationReport, filePath+"/rollback_" + version + ".json");
        }
    }
}

