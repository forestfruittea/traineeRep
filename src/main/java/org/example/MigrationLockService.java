package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class MigrationLockService {
    private static final Logger logger = LoggerFactory.getLogger(MigrationLockService.class);
    private final Connection connection;

    public MigrationLockService(Connection connection) {
        this.connection = connection;
    }

    public void lock(String lockedBy) throws SQLException {
        logger.info("Attempting to acquire lock...");

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
            logger.info("Lock acquired successfully by: " + lockedBy);
        } catch (SQLException | IllegalStateException e) {
            connection.rollback();
            logger.error("Failed to acquire lock: {}", e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void unlock() throws SQLException {
        logger.info("Releasing lock...");

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
                logger.info("Lock released successfully.");
            } else {
                logger.warn("Lock was not held.");
            }
        }
    }

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
                logger.info("Lock status: " + (locked ? "Locked" : "Unlocked"));
                return locked;
            }

            // If no row exists, assume unlocked
            logger.info("No lock record found. Assuming unlocked.");
            return false;
        }
    }
}
