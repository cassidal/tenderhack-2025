package ru.tenderhack.cte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Сервис для парсинга CSV файлов
 */
@Slf4j
@Service
public class CsvParserService {

    /**
     * Парсит CSV файл и возвращает заголовки и строки
     *
     * @param file CSV файл
     * @param delimiter разделитель (по умолчанию ';')
     * @return распарсенные данные
     */
    public CsvParseResult parseCsv(File file, String delimiter) {
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ";";
        }

        List<String> headers = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    headers = parseLine(line, delimiter);
                    isFirstLine = false;
                    log.debug("Parsed {} headers: {}", headers.size(), headers);
                } else {
                    List<String> values = parseLine(line, delimiter);
                    if (values.size() == headers.size()) {
                        Map<String, String> row = new LinkedHashMap<>();
                        for (int i = 0; i < headers.size(); i++) {
                            row.put(headers.get(i), values.get(i));
                        }
                        rows.add(row);
                    } else {
                        log.warn("Skipping row with {} values (expected {}): {}", 
                                values.size(), headers.size(), line.substring(0, Math.min(50, line.length())));
                    }
                }
            }

            log.info("Parsed CSV: {} headers, {} rows", headers.size(), rows.size());
            return CsvParseResult.builder()
                    .headers(headers)
                    .rows(rows)
                    .build();

        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }

    /**
     * Парсит строку CSV с учетом кавычек
     */
    private List<String> parseLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && line.substring(i).startsWith(delimiter)) {
                result.add(current.toString().trim());
                current = new StringBuilder();
                i += delimiter.length() - 1;
            } else {
                current.append(c);
            }
        }

        result.add(current.toString().trim());
        return result;
    }

    /**
     * Результат парсинга CSV
     */
    @lombok.Data
    @lombok.Builder
    public static class CsvParseResult {
        private List<String> headers;
        private List<Map<String, String>> rows;
    }
}

