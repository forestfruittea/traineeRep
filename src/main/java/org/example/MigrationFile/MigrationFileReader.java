package org.example.MigrationFile;



import lombok.extern.slf4j.Slf4j;
import org.example.MigrationFile.MigrationFile;
import org.example.Utils.PropertiesUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * The MigrationFileReader class is responsible for reading migration and rollback SQL files from the filesystem.
 * It provides methods to load migration and rollback files and sort them as needed.
 */
@Slf4j
public class MigrationFileReader {
    PropertiesUtils config = new PropertiesUtils();
    private final Path migrationDir = config.getMigrationsPath();
    private final Path rollbackDir = config.getRollbacksPath();


    public MigrationFileReader() {

    }
    /**
     * Retrieves a list of migration files from the configured migrations directory.
     * The files are sorted by their version number.
     *
     * @return a list of MigrationFile objects representing the migration SQL files.
     * @throws IOException if an I/O error occurs while reading the files.
     */

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
    /**
     * Retrieves a list of rollback files for the specified version range.
     *
     * @param targetVersion the target version(db state) to rollback to.
     * @param currentVersion the current version of the database.
     * @return a list of MigrationFile objects representing the rollback SQL files.
     * @throws IOException if an I/O error occurs while reading the files.
     */
    public List<MigrationFile> getRollbackFiles(String targetVersion, String currentVersion) throws IOException {
        log.debug("Fetching rollback files for target version: " + targetVersion + ", current version: " + currentVersion);

        DirectoryStream<Path> rollbackFiles = Files.newDirectoryStream(rollbackDir, "V*__rollback.sql");
        List<MigrationFile> filteredRollbacks = new ArrayList<>();

        for (Path file : rollbackFiles) {
            String fileName = file.getFileName().toString();
            String version = extractVersion(fileName);

            // Include files with version greater than or equal to targetVersion and less than or equal to currentVersion
            if (version.compareTo(targetVersion) >= 0 && version.compareTo(currentVersion) <= 0) {
                log.debug("Including rollback file: " + fileName);
                String sqlContent = Files.readString(file);
                filteredRollbacks.add(new MigrationFile(version, "rollback", sqlContent));
            } else {
                log.debug("Excluding rollback file: " + fileName);
            }
        }

        // Sort rollback files in descending order to apply them in reverse order
        filteredRollbacks.sort(Comparator.comparing(MigrationFile::getVersion).reversed());
        log.debug("Filtered and sorted rollback files: " + filteredRollbacks);
        return filteredRollbacks;
    }


    private String extractVersion(String fileName) {
        return fileName.split("__")[0].replace("V", "");
    }

    private String extractDescription(String fileName) {
        return fileName.split("__")[1].replace(".sql", "");
    }
}
