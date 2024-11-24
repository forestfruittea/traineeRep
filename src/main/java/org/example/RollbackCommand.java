
package org.example;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.sql.SQLException;

@Command(name = "rollback", description = "Rollback last applied migration")
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
            System.err.println("Error during rollback: " + e.getMessage());
        }
    }
}

