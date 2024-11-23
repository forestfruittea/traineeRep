package org.example;

import java.io.File;

public class FileUtils {
    public static void ensureDirectoryExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
