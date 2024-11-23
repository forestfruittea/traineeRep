
package org.example;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.sql.SQLException;
@Slf4j
@Command(name = "migrate", description = "Apply all available migrations")
public class MigrateCommand implements Runnable{
    private final MigrationTool migrationTool;

    public MigrateCommand(MigrationTool migrationTool) {
        this.migrationTool = migrationTool;
    }
    @Override
    public void run() {
        try {
            migrationTool.executeMigration();
        } catch (SQLException e){
            log.error("Error during migration: " + e.getMessage());
        }
    }
}

