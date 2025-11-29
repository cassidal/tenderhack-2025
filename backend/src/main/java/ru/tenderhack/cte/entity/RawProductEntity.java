package ru.tenderhack.cte.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ste_id", nullable = false)
    private Long steId;

    @Column(length = 1000)
    private String title;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String model;

    @Column(length = 255)
    private String country;

    @Column(length = 500)
    private String manufacturer;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 500)
    private String categoryName;

    @Column(columnDefinition = "TEXT")
    private String characteristics;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

