package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        PropertiesUtils config = new PropertiesUtils();
        ConnectionManager connectionManager = new ConnectionManager(config);

        try {

            Connection connection = connectionManager.connect();
            logger.debug("db is connected");
            MigrationExecutor migrationExecutor = new MigrationExecutor(connection, new MigrationFileReader());
            MigrationTool migrationTool = new MigrationTool(migrationExecutor, connection);

            migrationTool.executeMigration();
            connection.close();
            logger.debug("connection is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}