package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tenderhack.cte.config.MlServiceConfigProperties;
import ru.tenderhack.cte.dto.ClusterRequest;
import ru.tenderhack.cte.dto.ClusterResponse;

import java.util.List;
import java.util.Map;

/**
 * Сервис для взаимодействия с Python ML микросервисом
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PythonClientService {

    private final MlServiceConfigProperties mlServiceConfig;
    private final RestTemplate restTemplate;

    /**
     * Кластеризует ключи используя семантические эмбеддинги
     *
     * @param keys список ключей для кластеризации
     * @param threshold порог схожести (0.0 - 1.0)
     * @return список кластеров (каждый кластер - список синонимов)
     */
    public List<List<String>> clusterKeys(List<String> keys, double threshold) {
        log.info("Clustering {} keys with threshold {}", keys.size(), threshold);

        try {
            ClusterRequest request = ClusterRequest.builder()
                    .keys(keys)
                    .threshold(threshold)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ClusterRequest> entity = new HttpEntity<>(request, headers);

            ClusterResponse response = restTemplate.postForObject(
                    mlServiceConfig.url() + "/cluster_keys",
                    entity,
                    ClusterResponse.class
            );

            if (response == null || response.getClusters() == null) {
                log.warn("Empty response from ML service");
                return List.of();
            }

            log.info("Received {} clusters from ML service", response.getClusters().size());
            return response.getClusters();

        } catch (Exception e) {
            log.error("Error calling ML service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cluster keys: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет доступность ML сервиса
     *
     * @return true если сервис доступен
     */
    public boolean isServiceAvailable() {
        try {
            restTemplate.getForObject(mlServiceConfig.url() + "/health", Map.class);
            return true;
        } catch (Exception e) {
            log.warn("ML service is not available: {}", e.getMessage());
            return false;
        }
    }
}

