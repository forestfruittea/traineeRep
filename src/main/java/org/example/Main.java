package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        PropertiesUtils config = new PropertiesUtils();
        ConnectionManager dbConnection = new ConnectionManager(config);

        try {

            Connection connection = dbConnection.connect();
            logger.debug("db is connected");
            MigrationExecutor migrationExecutor = new MigrationExecutor(connection);
            MigrationTool migrationTool = new MigrationTool(migrationExecutor, connection);

            migrationTool.execute();
            connection.close();
            logger.debug("connection is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}