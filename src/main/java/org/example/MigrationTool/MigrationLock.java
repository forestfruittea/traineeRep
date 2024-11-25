package org.example.MigrationTool;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
/**
 * Service for acquiring and releasing a lock for the migration process.
 */
@Slf4j
public class MigrationLock {

    private final Connection connection;

    public MigrationLock(Connection connection) {
        this.connection = connection;
    }
    /**
     * Acquires a lock for the migration process. Throws an exception if the lock is already acquired.
     *
     * @param lockedBy the user or process requesting the lock
     * @throws SQLException if a database error occurs
     */

    public void lock(String lockedBy) throws SQLException {
        log.debug("Attempting to acquire lock...");

        String checkLockSql = """
                SELECT is_locked 
                FROM migration_lock 
                WHERE id = 1
                """;

        String lockSql = """
                INSERT INTO migration_lock (id, is_locked, locked_at, locked_by)
                VALUES (1, TRUE, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET is_locked = EXCLUDED.is_locked, 
                    locked_at = EXCLUDED.locked_at, 
                    locked_by = EXCLUDED.locked_by
                """;

        try {
            connection.setAutoCommit(false);

            // Check if the lock is already acquired
            try (PreparedStatement checkLockStmt = connection.prepareStatement(checkLockSql)) {
                ResultSet resultSet = checkLockStmt.executeQuery();
                if (resultSet.next() && resultSet.getBoolean("is_locked")) {
                    throw new IllegalStateException("Migration is already locked by another process.");
                }
            }

            // Acquire the lock
            try (PreparedStatement lockStmt = connection.prepareStatement(lockSql)) {
                lockStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                lockStmt.setString(2, lockedBy);
                lockStmt.executeUpdate();
            }

            connection.commit();

            log.debug("Lock acquired successfully by: " + lockedBy);
        } catch (SQLException | IllegalStateException e) {
            connection.rollback();
            log.error("Failed to acquire lock: {}", e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    /**
     * Releases the lock held by the current process.
     *
     * @throws SQLException if a database error occurs
     */
    public void unlock() throws SQLException {
        log.debug("Releasing lock...");

        String unlockSql = """
                UPDATE migration_lock 
                SET is_locked = FALSE, 
                    locked_at = NULL, 
                    locked_by = NULL 
                WHERE id = 1
                """;

        try (PreparedStatement unlockStmt = connection.prepareStatement(unlockSql)) {
            int rowsUpdated = unlockStmt.executeUpdate();
            if (rowsUpdated > 0) {
                log.debug("Lock released successfully.");
            } else {
                log.warn("Lock was not held.");
            }
        }
    }
    /**
     * Checks if the migration process is currently locked.
     *
     * @return true if the migration is locked, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isLocked() throws SQLException {
        String checkLockSql = """
                SELECT is_locked 
                FROM migration_lock 
                WHERE id = 1
                """;

        try (PreparedStatement checkLockStmt = connection.prepareStatement(checkLockSql)) {
            ResultSet resultSet = checkLockStmt.executeQuery();
            if (resultSet.next()) {
                boolean locked = resultSet.getBoolean("is_locked");
                log.debug("Lock status: " + (locked ? "Locked" : "Unlocked"));
                return locked;
            }

            // If no row exists, assume unlocked
            log.debug("No lock record found. Assuming unlocked.");
            return false;
        }
    }
}
