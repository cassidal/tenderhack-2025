package ru.tenderhack.cte.mapper;

import org.springframework.stereotype.Component;
import ru.tenderhack.cte.dto.Attribute;
import ru.tenderhack.cte.dto.CteDetail;
import ru.tenderhack.cte.dto.CteSummary;
import ru.tenderhack.cte.entity.AttributeJson;
import ru.tenderhack.cte.entity.CteEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования CteEntity в DTO
 */
@Component
public class CteMapper {

    public CteSummary toSummary(CteEntity entity) {
        List<Attribute> previewAttributes = entity.getImportantAttributes().stream()
                .limit(3)
                .map(this::toAttribute)
                .collect(Collectors.toList());

        return new CteSummary(
                entity.getId(),
                entity.getImageUrl(),
                previewAttributes
        );
    }

    public CteDetail toDetail(CteEntity entity) {
        return new CteDetail(
                entity.getId(),
                entity.getImageUrl(),
                entity.getImportantAttributes().stream()
                        .map(this::toAttribute)
                        .collect(Collectors.toList()),
                entity.getSecondaryAttributes().stream()
                        .map(this::toAttribute)
                        .collect(Collectors.toList()),
                entity.getProductIds()
        );
    }

    public Attribute toAttribute(AttributeJson json) {
        return new Attribute(json.getName(), json.getValue());
    }

    public AttributeJson toAttributeJson(Attribute dto) {
        return AttributeJson.builder()
                .name(dto.name())
                .value(dto.value())
                .build();
    }
}

