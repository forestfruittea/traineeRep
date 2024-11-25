
package org.example.CLICommands;
import lombok.extern.slf4j.Slf4j;
import org.example.MigrationTool.MigrationTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.sql.SQLException;
/**
 * CLI Command to rollback db state to a specified version.
 */
@Command(name = "rollback", description = "Rollback last applied migration")
@Slf4j
public class RollbackCommand implements Runnable {

    private final MigrationTool migrationTool;
    @Option(names = {"-v", "--version"}, description = "The version of the rollback to apply", required = true)
    private String targetVersion;

    public RollbackCommand(MigrationTool migrationTool) {
        this.migrationTool = migrationTool;
    }

    @Override
    public void run() {
        try {
            migrationTool.rollback(targetVersion);
        } catch (SQLException e) {
            log.error("Error during rollback: " + e.getMessage());
        }
    }
}

