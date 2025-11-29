package ru.tenderhack.cte.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.tenderhack.cte.config.LlmConfigProperties;

import java.util.Map;

/**
 * Сервис для взаимодействия с локальной LLM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmClientService {

    private final RestClient restClient;
    private final LlmConfigProperties llmConfig;
    private final ObjectMapper objectMapper;

    /**
     * Отправляет prompt в LLM и возвращает ответ
     *
     * @param prompt текст запроса к LLM
     * @return строковый ответ от LLM
     */
    public String generate(String prompt) {
        log.debug("Sending prompt to LLM: {}", prompt);

        Map<String, Object> requestBody = Map.of(
                "model", llmConfig.model(),
                "prompt", prompt,
                "stream", false
        );

        try {
            String response = restClient.post()
                    .uri(llmConfig.url())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.debug("Received response from LLM: {}", response);

            // Парсим ответ и извлекаем поле "response"
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("response")) {
                return jsonNode.get("response").asText();
            }

            return response;
        } catch (Exception e) {
            log.error("Error calling LLM: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call LLM service", e);
        }
    }
}

