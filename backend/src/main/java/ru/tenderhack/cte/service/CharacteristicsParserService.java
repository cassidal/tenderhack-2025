package ru.tenderhack.cte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для парсинга характеристик из текстового формата в JSONB
 */
@Slf4j
@Service
public class CharacteristicsParserService {

    /**
     * Парсит строку характеристик в формате "Ключ:Значение;Ключ:Значение"
     * в Map для сохранения в JSONB
     *
     * @param characteristicsStr строка с характеристиками
     * @return Map с характеристиками, где ключ - название характеристики, значение - её значение
     */
    public Map<String, String> parseCharacteristics(String characteristicsStr) {
        Map<String, String> result = new HashMap<>();

        if (characteristicsStr == null || characteristicsStr.trim().isEmpty() || 
            characteristicsStr.trim().equalsIgnoreCase("NULL")) {
            return result;
        }

        // Убираем кавычки в начале и конце, если есть
        String cleaned = characteristicsStr.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // Разделяем по точке с запятой
        String[] pairs = cleaned.split(";");

        for (String pair : pairs) {
            pair = pair.trim();
            if (pair.isEmpty()) {
                continue;
            }

            // Разделяем на ключ и значение (только первое двоеточие)
            int colonIndex = pair.indexOf(':');
            if (colonIndex > 0 && colonIndex < pair.length() - 1) {
                String key = pair.substring(0, colonIndex).trim();
                String value = pair.substring(colonIndex + 1).trim();

                // Убираем кавычки из значения, если есть
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                if (!key.isEmpty() && !value.isEmpty()) {
                    result.put(key, value);
                }
            }
        }

        return result;
    }
}

