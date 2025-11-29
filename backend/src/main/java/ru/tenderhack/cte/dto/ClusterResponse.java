package ru.tenderhack.cte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ответ с кластерами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterResponse {

    private List<List<String>> clusters;
}

