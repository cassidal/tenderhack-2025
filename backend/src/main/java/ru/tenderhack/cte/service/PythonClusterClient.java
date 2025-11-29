package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.tenderhack.cte.dto.ClusterRequest;
import ru.tenderhack.cte.dto.ClusterResponse;

import java.net.ConnectException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST клиент для взаимодействия с Python ML сервисом кластеризации
 * Обеспечивает fallback логику при недоступности сервиса
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PythonClusterClient {

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    @Value("${ml.service.clustering.threshold:0.4}")
    private double defaultThreshold;

    private final RestTemplate restTemplate;

    /**
     * Получает кластеры синонимов из Python ML сервиса
     *
     * @param rawHeaders список исходных заголовков CSV
     * @return список кластеров (каждый кластер - список синонимов)
     */
    public List<List<String>> getClusters(List<String> rawHeaders) {
        log.info("Requesting clusters for {} raw headers from Python ML service", rawHeaders.size());

        try {
            ClusterRequest request = ClusterRequest.builder()
                    .keys(rawHeaders)
                    .threshold(defaultThreshold)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ClusterRequest> entity = new HttpEntity<>(request, headers);

            String url = mlServiceUrl.endsWith("/cluster_keys") 
                    ? mlServiceUrl 
                    : mlServiceUrl + "/cluster_keys";

            ClusterResponse response = restTemplate.postForObject(
                    url,
                    entity,
                    ClusterResponse.class
            );

            if (response == null || response.getClusters() == null || response.getClusters().isEmpty()) {
                log.warn("Empty response from Python ML service, falling back to single-element clusters");
                return createFallbackClusters(rawHeaders);
            }

            int clusterCount = response.getClusters().size();
            log.info("Received {} clusters from Python ML service (reduced from {} headers)", 
                    clusterCount, rawHeaders.size());

            return response.getClusters();

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof ConnectException) {
                log.warn("Python ML service is not available (Connection refused). " +
                        "Falling back to single-element clusters. Pipeline will continue but without semantic clustering.");
                return createFallbackClusters(rawHeaders);
            }
            log.error("Network error calling Python ML service: {}", e.getMessage(), e);
            return createFallbackClusters(rawHeaders);

        } catch (Exception e) {
            log.error("Error calling Python ML service: {}", e.getMessage(), e);
            log.warn("Falling back to single-element clusters to keep pipeline running");
            return createFallbackClusters(rawHeaders);
        }
    }

    /**
     * Создает fallback кластеры - каждый заголовок становится отдельным кластером
     * Это позволяет pipeline продолжить работу даже если Python сервис недоступен
     *
     * @param rawHeaders исходные заголовки
     * @return список кластеров, где каждый кластер содержит один заголовок
     */
    private List<List<String>> createFallbackClusters(List<String> rawHeaders) {
        log.info("Creating fallback clusters: {} single-element clusters", rawHeaders.size());
        return rawHeaders.stream()
                .map(List::of)
                .collect(Collectors.toList());
    }

    /**
     * Проверяет доступность Python ML сервиса
     *
     * @return true если сервис доступен
     */
    public boolean isServiceAvailable() {
        try {
            String healthUrl = mlServiceUrl.endsWith("/health") 
                    ? mlServiceUrl 
                    : mlServiceUrl + "/health";
            restTemplate.getForObject(healthUrl, java.util.Map.class);
            return true;
        } catch (Exception e) {
            log.debug("Python ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
}

