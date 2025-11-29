package ru.tenderhack.cte.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "task_processing_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProcessingItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "raw_product_id")
    private Long rawProductId;

    @Column(length = 1000)
    private String title;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String model;

    @Column(length = 500)
    private String manufacturer;

    @Column(length = 255)
    private String country;

    @Column(name = "category_name", length = 500)
    private String categoryName;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> characteristics = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private GroupingTaskEntity task;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (characteristics == null) {
            characteristics = new HashMap<>();
        }
    }
}

