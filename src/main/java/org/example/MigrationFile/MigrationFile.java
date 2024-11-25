package org.example.MigrationFile;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a migration file containing version, description, and SQL script.
 */

@Data
@Builder
public class MigrationFile {
    private final String version;
    private final String description;
    private final String sql;
}
