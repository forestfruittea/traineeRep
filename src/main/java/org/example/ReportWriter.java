package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ReportWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeReport(MigrationReport report, String filePath) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to JSON", e);
        }
    }
}
