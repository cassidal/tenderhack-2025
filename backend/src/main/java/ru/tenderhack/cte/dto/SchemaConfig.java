package ru.tenderhack.cte.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Конфигурация схемы атрибута
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaConfig {

    /**
     * Внутреннее системное имя (например, "width")
     */
    @JsonProperty("unified_name")
    private String unifiedName;

    /**
     * Русское название для UI (например, "Ширина")
     */
    @JsonProperty("display_name_ru")
    private String displayNameRu;

    /**
     * Тип данных: "Numeric", "Boolean", "String"
     */
    @JsonProperty("data_type")
    private String dataType;

    /**
     * Правило парсинга
     */
    @JsonProperty("parsing_rule")
    private ParsingRule parsingRule;
}

