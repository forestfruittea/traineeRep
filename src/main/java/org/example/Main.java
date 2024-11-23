package org.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.sql.Connection;
@Slf4j
@RequiredArgsConstructor
public class Main {

    public static void main(String[] args) {
        PropertiesUtils config = new PropertiesUtils();
        ConnectionManager connectionManager = new ConnectionManager(config);
        try {

            Connection connection = connectionManager.connect();
            log.debug("db is connected");
            MigrationExecutor migrationExecutor = new MigrationExecutor(connection);
            MigrationTool migrationTool = new MigrationTool(migrationExecutor, connection, config);
            migrationExecutor.initializeSchemaTable();
            migrationExecutor.initializeLockTable();
            FileUtils.ensureDirectoryExists("reports");

            migrationTool.executeMigration();


            connection.close();
            log.debug("connection is closed");
        } catch (Exception e) {
            log.error("An error occured: ", e);
        }
    }
}