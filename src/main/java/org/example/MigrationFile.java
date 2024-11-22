package org.example;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MigrationFile {
    private final String version;
    private final String description;
    private final String sql;
}
