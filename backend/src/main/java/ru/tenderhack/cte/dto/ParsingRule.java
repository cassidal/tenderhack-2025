package ru.tenderhack.cte.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Правило парсинга для атрибута
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsingRule {

    /**
     * Python-style Regex с именованными группами (?<value>...)
     */
    private String regex;

    /**
     * Базовая единица измерения (mm, kg, etc.) или null
     */
    @JsonProperty("base_unit")
    private String baseUnit;

    /**
     * Множители для конвертации единиц измерения
     * Например: {"cm": 10.0} означает, что 1 см = 10 базовых единиц
     */
    @JsonProperty("unit_multipliers")
    private Map<String, Double> unitMultipliers;
}

