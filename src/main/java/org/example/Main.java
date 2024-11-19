package org.example;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "admin";

        try {
            DatabaseConnection dbConnection = new DatabaseConnection(url, user, password);
            Connection connection = dbConnection.connect();

            MigrationService migrationService = new MigrationService(connection);
            MigrateCommand migrateCommand = new MigrateCommand(migrationService);

            migrateCommand.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}