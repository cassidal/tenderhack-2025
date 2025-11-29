package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tenderhack.cte.dto.ParsingRule;
import ru.tenderhack.cte.dto.SchemaConfig;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Движок динамического парсинга значений на основе схемы
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicParserEngine {

    /**
     * Парсит сырое значение используя конфигурацию схемы
     *
     * @param rawValue исходное значение из CSV
     * @param config конфигурация схемы с правилами парсинга
     * @return распарсенное значение (Number, Boolean, или String)
     */
    public Object parse(String rawValue, SchemaConfig config) {
        if (rawValue == null || rawValue.trim().isEmpty() || config == null) {
            return null;
        }

        try {
            ParsingRule rule = config.getParsingRule();
            if (rule == null || rule.getRegex() == null) {
                // Если нет правила, возвращаем как строку
                return rawValue.trim();
            }

            // Компилируем regex (конвертируем Python named groups в Java)
            String javaRegex = convertPythonRegexToJava(rule.getRegex());
            Pattern pattern = Pattern.compile(javaRegex);
            Matcher matcher = pattern.matcher(rawValue.trim());

            if (!matcher.find()) {
                log.debug("No match for value '{}' with regex '{}'", rawValue, javaRegex);
                return rawValue.trim(); // Возвращаем исходное значение если не совпало
            }

            // Извлекаем значение из именованной группы
            String valueStr = extractValue(matcher, rule.getRegex());

            // Парсим в зависимости от типа данных
            return parseByDataType(valueStr, config.getDataType(), rule, matcher);

        } catch (Exception e) {
            log.warn("Error parsing value '{}' with config {}: {}", rawValue, config.getUnifiedName(), e.getMessage());
            return rawValue.trim(); // Возвращаем исходное значение при ошибке
        }
    }

    /**
     * Конвертирует Python regex с именованными группами в Java regex
     * Python: (?<value>...) -> Java: (?<value>...)
     * Java поддерживает именованные группы, но нужно убедиться в правильности синтаксиса
     */
    private String convertPythonRegexToJava(String pythonRegex) {
        // Java поддерживает именованные группы в том же формате
        // Но нужно экранировать некоторые символы
        return pythonRegex
                .replace("(?<", "(?<") // Именованные группы одинаковы
                .replace("(?P<", "(?<"); // Python альтернативный синтаксис
    }

    /**
     * Извлекает значение из matcher используя именованную группу
     */
    private String extractValue(Matcher matcher, String regex) {
        // Пытаемся найти именованную группу "value"
        try {
            String value = matcher.group("value");
            if (value != null && !value.isEmpty()) {
                return value;
            }
        } catch (IllegalArgumentException e) {
            // Группа "value" не найдена, пробуем другие варианты
        }

        // Если нет именованной группы, берем первую группу
        if (matcher.groupCount() > 0) {
            return matcher.group(1);
        }

        // Если нет групп, возвращаем весь match
        return matcher.group(0);
    }

    /**
     * Парсит значение в зависимости от типа данных
     */
    private Object parseByDataType(String valueStr, String dataType, ParsingRule rule, Matcher matcher) {
        if (valueStr == null || valueStr.isEmpty()) {
            return null;
        }

        switch (dataType != null ? dataType : "String") {
            case "Numeric":
                return parseNumeric(valueStr, rule, matcher);

            case "Boolean":
                return parseBoolean(valueStr);

            case "String":
            default:
                return valueStr.trim();
        }
    }

    /**
     * Парсит числовое значение с учетом единиц измерения
     */
    private Double parseNumeric(String valueStr, ParsingRule rule, Matcher matcher) {
        try {
            double value = Double.parseDouble(valueStr.replace(",", "."));

            // Проверяем наличие единицы измерения
            String unit = extractUnit(matcher);
            if (unit != null && rule.getUnitMultipliers() != null) {
                Double multiplier = rule.getUnitMultipliers().get(unit);
                if (multiplier != null) {
                    value = value * multiplier;
                }
            }

            return value;

        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric value: {}", valueStr);
            return null;
        }
    }

    /**
     * Извлекает единицу измерения из matcher
     */
    private String extractUnit(Matcher matcher) {
        try {
            return matcher.group("unit");
        } catch (IllegalArgumentException e) {
            // Группа "unit" не найдена
            return null;
        }
    }

    /**
     * Парсит булево значение
     */
    private Boolean parseBoolean(String valueStr) {
        String lower = valueStr.toLowerCase().trim();
        
        // Русские варианты
        if (lower.equals("да") || lower.equals("yes") || lower.equals("true") || lower.equals("1")) {
            return true;
        }
        if (lower.equals("нет") || lower.equals("no") || lower.equals("false") || lower.equals("0")) {
            return false;
        }

        // Если не распознано, возвращаем null
        return null;
    }
}

