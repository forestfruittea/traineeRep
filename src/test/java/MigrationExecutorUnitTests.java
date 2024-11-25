package org.example;

import org.example.MigrationTool.MigrationExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MigrationExecutorUnitTests {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    private MigrationExecutor migrationExecutor;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        migrationExecutor = new MigrationExecutor(connection);
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    void testInitializeSchemaTable() throws Exception {
        migrationExecutor.initializeSchemaTable();
        verify(statement).execute(anyString());
    }

    @Test
    void testApplyMigrationSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        migrationExecutor.applyMigration("1", "Initial TEST Migration", "CREATE TABLE test (id INT)");

        verify(statement).execute("CREATE TABLE test (id INT)");
        verify(preparedStatement).setString(1, "1");
        verify(preparedStatement).setString(2, "Initial TEST Migration");
        verify(preparedStatement).setTimestamp(eq(3), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
    }
    @Test
    void testApplyMigrationFailure() throws Exception {
        when(statement.execute(anyString())).thenThrow(new RuntimeException("Test Exception"));

        assertThrows(Exception.class, () -> migrationExecutor.applyMigration("1", "Faulty Migration", "INVALID SQL"));
    }
}