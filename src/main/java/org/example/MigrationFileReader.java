package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MigrationFileReader {
    private final Path migrationDir = Paths.get("src/main/resources/migrations");
    private final Path rollbackDir = Paths.get("src/main/resources/rollbacks");

    public MigrationFileReader() {

    }

    public List<MigrationFile> getMigrationFiles() throws IOException {
        DirectoryStream<Path> migrationFiles = Files.newDirectoryStream(migrationDir, "*.sql");
        List<MigrationFile> sortedMigrations = new ArrayList<>();

        for (Path file : migrationFiles) {
            String fileName = file.getFileName().toString();
            String version = extractVersion(fileName);
            String description = extractDescription(fileName);
            String sqlContent = Files.readString(file);

            sortedMigrations.add(new MigrationFile(version, description, sqlContent));
        }

        sortedMigrations.sort(Comparator.comparing(MigrationFile::getVersion));
        return sortedMigrations;
    }
    public List<MigrationFile> getRollbackFiles(String version) throws IOException {
        // Find the rollback file for a specific version
        DirectoryStream<Path> rollbackFiles = Files.newDirectoryStream(rollbackDir, version + "__*.sql");
        List<MigrationFile> sortedRollbacks = new ArrayList<>();

        for (Path file : rollbackFiles) {
            String description = "rollback";
            String sqlContent = Files.readString(file);

            sortedRollbacks.add(new MigrationFile(version, description, sqlContent));
        }

        return sortedRollbacks;
    }

    private String extractVersion(String fileName) {
        return fileName.split("__")[0].replace("V", "");
    }

    private String extractDescription(String fileName) {
        return fileName.split("__")[1].replace(".sql", "");
    }
}
