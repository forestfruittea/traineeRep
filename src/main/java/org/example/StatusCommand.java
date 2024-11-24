package org.example;

import picocli.CommandLine.Command;

import java.sql.SQLException;

@Command(name = "status", description = "Show current database status")
public class StatusCommand implements Runnable {

    private final MigrationTool migrationTool;

    public StatusCommand(MigrationTool migrationTool) {
        this.migrationTool=migrationTool;
    }

    @Override
    public void run() {
        try {
            migrationTool.status();
        } catch (SQLException e) {
            System.err.println("Error fetching status: " + e.getMessage());
        }
    }
}
