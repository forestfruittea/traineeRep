package org.example.MigrationReport;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Represents a report generated for a migration process.
 */

@Data
@Builder
public class MigrationReport {
    private String version;
    private String description;
    private String status; // "SUCCESS" or "FAILED"
    private Timestamp timestamp;
    private String errorMessage; // Optional for failures
}
