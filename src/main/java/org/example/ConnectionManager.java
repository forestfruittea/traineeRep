package org.example;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
@RequiredArgsConstructor
public class ConnectionManager {
    private final PropertiesUtils config;
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }
}
