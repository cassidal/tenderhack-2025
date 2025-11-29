package ru.tenderhack.cte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Запрос на кластеризацию ключей
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterRequest {

    private List<String> keys;
    private Double threshold;
}

