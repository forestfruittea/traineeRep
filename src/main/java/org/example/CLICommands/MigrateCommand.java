
package org.example.CLICommands;
import lombok.extern.slf4j.Slf4j;
import org.example.MigrationTool.MigrationTool;
import picocli.CommandLine.Command;

import java.sql.SQLException;
/**
 * A CLI Command to apply all available migrations.
 */
@Slf4j
@Command(name = "migrate", description = "Apply all available migrations")
public class MigrateCommand implements Runnable{
    private final MigrationTool migrationTool;

    /**
     * Executes the migration process.
     *
     * @throws SQLException if a database access error occurs during migration
     */
    public MigrateCommand(MigrationTool migrationTool) {
        this.migrationTool = migrationTool;
    }
    @Override
    public void run() {
        try {
            migrationTool.migrate();
        } catch (SQLException e){
            log.error("Error during migration: " + e.getMessage());
        }
    }
}

