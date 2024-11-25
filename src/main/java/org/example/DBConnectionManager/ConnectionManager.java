package org.example.DBConnectionManager;

import lombok.RequiredArgsConstructor;
import org.example.Utils.PropertiesUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * Manages the database connection.
 */
@RequiredArgsConstructor
public class ConnectionManager {
    private final PropertiesUtils config;
    /**
     * Establishes a connection to the database using the provided configuration.
     *
     * @return a {@link Connection} to the database
     * @throws SQLException if a database access error occurs
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }
}
