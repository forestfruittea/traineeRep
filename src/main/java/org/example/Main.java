package org.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.CLICommands.MigrateCommand;
import org.example.CLICommands.RollbackCommand;
import org.example.CLICommands.StatusCommand;
import org.example.MigrationTool.MigrationExecutor;
import org.example.MigrationTool.MigrationTool;
import org.example.DBConnectionManager.ConnectionManager;
import org.example.Utils.PropertiesUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.sql.Connection;
@Slf4j
@RequiredArgsConstructor
@Command(name = "flywayLib", mixinStandardHelpOptions = true, version = "1.0-SNAPSHOT", description = "Flyway migration CLI")
public class Main {
    /**
     * The main method that initializes the connection manager, migration executor,
     * and sets up the CLI subcommands for migration operations.
     */
    public static void main(String[] args) {
        PropertiesUtils config = new PropertiesUtils();
        ConnectionManager connectionManager = new ConnectionManager(config);
        CommandLine cmd = new CommandLine(new Main());

        try {

            Connection connection = connectionManager.connect();

            MigrationExecutor migrationExecutor = new MigrationExecutor(connection);
            MigrationTool migrationTool = new MigrationTool(migrationExecutor, connection, config);
            migrationExecutor.initializeSchemaTable();
            migrationExecutor.initializeLockTable();


            cmd.addSubcommand("migrate", new MigrateCommand(migrationTool));
            cmd.addSubcommand("rollback", new RollbackCommand(migrationTool));
            cmd.addSubcommand("status", new StatusCommand(migrationTool));

        } catch (Exception e) {
            log.error("An error occured: ", e);
        } finally {
            int exitCode = cmd.execute(args);
            System.exit(exitCode);
        }
    }
}