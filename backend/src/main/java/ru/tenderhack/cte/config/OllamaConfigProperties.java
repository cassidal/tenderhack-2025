package ru.tenderhack.cte.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация для Ollama
 */
@ConfigurationProperties(prefix = "ollama")
public record OllamaConfigProperties(
        String url,
        String model
) {
}

