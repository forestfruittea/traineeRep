package org.example.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
/**
 * Utility class for loading and accessing properties from the `application.properties` file.
 */
public class PropertiesUtils {
    private final Properties properties = new Properties();
    /**
     * Loads the properties from the `application.properties` file.
     *
     * @throws RuntimeException if the properties file cannot be loaded
     */

    public PropertiesUtils() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties file not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }
    /**
     * Retrieves the database URL from the properties file.
     *
     * @return the database URL
     */
    public String getUrl() {
        return properties.getProperty("db.url");
    }
    /**
     * Retrieves the database username from the properties file.
     *
     * @return the database username
     */

    public String getUsername() {
        return properties.getProperty("db.username");
    }

    /**
     * Retrieves the database password from the properties file.
     *
     * @return the database password
     */
    public String getPassword() {
        return properties.getProperty("db.password");
    }
    /**
     * Retrieves the path for migration files from the properties file.
     *
     * @return the path for migration files
     * @throws IllegalStateException if the migrations path is not configured
     */
    public Path getMigrationsPath() {
        String migrationsPath = properties.getProperty("migrations.path");
        if (migrationsPath == null || migrationsPath.isEmpty()) {
            throw new IllegalStateException("migrations.path is not configured in application.properties");
        }
        return Paths.get(migrationsPath);
    }
    /**
     * Retrieves the path for rollback files from the properties file.
     *
     * @return the path for rollback files
     * @throws IllegalStateException if the rollbacks path is not configured
     */
    public Path getRollbacksPath() {
        String rollbacksPath = properties.getProperty("rollbacks.path");
        if (rollbacksPath == null || rollbacksPath.isEmpty()) {
            throw new IllegalStateException("rollbacks.path is not configured in application.properties");
        }
        return Paths.get(rollbacksPath);
    }
    /**
     * Retrieves the path for report files from the properties file.
     *
     * @return the path for report files
     * @throws IllegalStateException if the reports path is not configured
     */
    public String getReportsPath() {
        String reportsPath = properties.getProperty("reports.path");
        if (reportsPath == null || reportsPath.isEmpty()) {
            throw new IllegalStateException("rollbacks.path is not configured in application.properties");
        }
        return reportsPath;
    }
}