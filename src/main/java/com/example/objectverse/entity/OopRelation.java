package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopRelation extends BaseEntity {

    private Long projectId;

    private String sourceType;

    private Long sourceId;

    private String targetType;

    private Long targetId;

    private String relationType;

    private String description;
}
