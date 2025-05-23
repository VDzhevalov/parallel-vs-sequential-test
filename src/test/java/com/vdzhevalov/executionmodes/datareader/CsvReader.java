package com.vdzhevalov.executionmodes.datareader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class CsvReader {


    private Path getPathToCategoriesCsv(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException(format("Resource 'test-data/%s' not found",resourcePath));
            }

            // Копіюємо в тимчасовий файл
            Path tempFile = Files.createTempFile("categories", ".csv");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit(); // Авто-очистка після завершення JVM

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource '" + resourcePath + "'", e);
        }
    }

    private String getPath(String resourcePath) {
        return getPathToCategoriesCsv(resourcePath).toString();
    }

    private static List<String> readCsv(String filePath)  {
       try {
           return Files.readAllLines(Paths.get(filePath));
       } catch (IOException e) {}
       return null;
    }

    public static String[] getDataFromCsv(String filePath){
        return readCsv(new CsvReader().getPath(filePath)).stream()
                .flatMap(s -> Arrays.stream(s.split(";\\s*")))
                .toArray(String[]::new);
    }

    public static int getDataCountFromCsv(String filePath){
        return getDataFromCsv(filePath).length;
    }
}