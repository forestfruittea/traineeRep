package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MigrateCommand {
    private final MigrationService migrationService;

    public MigrateCommand(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    public void execute() throws SQLException, IOException {
        migrationService.initializeSchemaTable();

        List<String> appliedMigrations = migrationService.getAppliedMigrations();

        String currentVersion = migrationService.getCurrentVersion();
        System.out.println("Current database version: " + (currentVersion != null ? currentVersion : "None"));

        Path migrationDir = Paths.get("src/main/resources/migrations");
        DirectoryStream<Path> migrationFiles = Files.newDirectoryStream(migrationDir, "*.sql");
        List<Path> sortedMigrations = new ArrayList<>();
        for (Path file : migrationFiles) {
            sortedMigrations.add(file);
        }
        sortedMigrations.sort(Comparator.comparing(this::extractVersion));

        for (Path file : migrationFiles) {
            String fileName = file.getFileName().toString();
            String version = fileName.split("__")[0].replace("V", "");
            String description = fileName.split("__")[1].replace(".sql", "");

            if (!appliedMigrations.contains(version)) {
                String sql = Files.readString(file);
                migrationService.applyMigration(version, description, sql);
                System.out.println("Applied migration: " + fileName);
            } else {
                System.out.println("Migration already applied: " + fileName);
        }
    }
    }
}
