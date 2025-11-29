package ru.tenderhack.cte.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация для LLM клиента
 */
@ConfigurationProperties(prefix = "llm")
public record LlmConfigProperties(
        String url,
        String model
) {
}

