package ru.tenderhack.cte.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация для ML сервиса
 */
@ConfigurationProperties(prefix = "ml.service")
public record MlServiceConfigProperties(
        String url
) {
}

