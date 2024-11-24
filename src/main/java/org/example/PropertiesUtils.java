package org.example;



import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesUtils {
    private final Properties properties = new Properties();

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

    public String getUrl() {
        return properties.getProperty("db.url");
    }

    public String getUsername() {
        return properties.getProperty("db.username");
    }

    public String getPassword() {
        return properties.getProperty("db.password");
    }
    public Path getMigrationsPath() {
        String migrationsPath = properties.getProperty("migrations.path");
        if (migrationsPath == null || migrationsPath.isEmpty()) {
            throw new IllegalStateException("migrations.path is not configured in application.properties");
        }
        return Paths.get(migrationsPath);
    }

    public Path getRollbacksPath() {
        String rollbacksPath = properties.getProperty("rollbacks.path");
        if (rollbacksPath == null || rollbacksPath.isEmpty()) {
            throw new IllegalStateException("rollbacks.path is not configured in application.properties");
        }
        return Paths.get(rollbacksPath);
    }
    public String getReportsPath() {
        String reportsPath = properties.getProperty("reports.path");
        if (reportsPath == null || reportsPath.isEmpty()) {
            throw new IllegalStateException("rollbacks.path is not configured in application.properties");
        }
        return reportsPath;
    }
}