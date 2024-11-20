package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MigrationFileReader {
    private final Path migrationDir = Paths.get("src/main/resources/migrations");

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

    private String extractVersion(String fileName) {
        return fileName.split("__")[0].replace("V", "");
    }

    private String extractDescription(String fileName) {
        return fileName.split("__")[1].replace(".sql", "");
    }
}
