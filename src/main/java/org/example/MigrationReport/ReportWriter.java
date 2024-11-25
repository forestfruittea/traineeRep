package org.example.MigrationReport;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
/**
 * Utility class to write reports to a file in JSON format.
 */
public class ReportWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Writes the given migration report to a specified file path in JSON format.
     *
     * @param report The migration report to be written to the file.
     * @param filePath The path where the report should be written.
     * @throws RuntimeException if there is an error while writing the report to the file.
     */
    public static void writeReport(MigrationReport report, String filePath) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to JSON", e);
        }
    }
}
