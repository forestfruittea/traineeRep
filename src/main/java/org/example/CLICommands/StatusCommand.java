package org.example.CLICommands;

import lombok.extern.slf4j.Slf4j;
import org.example.MigrationTool.MigrationTool;
import picocli.CommandLine.Command;

import java.sql.SQLException;
/**
 * CLI Command to display the current status of the database migrations.
 */
@Command(name = "status", description = "Show current database status")
@Slf4j
public class StatusCommand implements Runnable {

    private final MigrationTool migrationTool;

    public StatusCommand(MigrationTool migrationTool) {
        this.migrationTool = migrationTool;
    }

    @Override
    public void run() {
        try {
            migrationTool.status();
        } catch (SQLException e) {
          log.error("Error fetching status: " + e.getMessage());
        }
    }
}
