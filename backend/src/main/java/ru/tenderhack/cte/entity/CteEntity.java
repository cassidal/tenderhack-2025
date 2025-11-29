package ru.tenderhack.cte.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cte_entities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "image_url")
    private String imageUrl;

    @Type(JsonType.class)
    @Column(name = "important_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private List<AttributeJson> importantAttributes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "secondary_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private List<AttributeJson> secondaryAttributes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "product_ids", columnDefinition = "jsonb")
    @Builder.Default
    private List<Long> productIds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private GroupingTaskEntity task;
}

