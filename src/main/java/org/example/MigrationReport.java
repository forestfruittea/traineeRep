package org.example;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
public class MigrationReport {
    private String version;
    private String description;
    private String status; // "SUCCESS" or "FAILED"
    private Timestamp timestamp;
    private String errorMessage; // Optional for failures
}
