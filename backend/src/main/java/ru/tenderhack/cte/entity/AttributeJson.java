package ru.tenderhack.cte.entity;

import lombok.*;

import java.io.Serializable;

/**
 * JSON-представление атрибута для хранения в JSONB
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeJson implements Serializable {

    private String name;
    private String value;
}

