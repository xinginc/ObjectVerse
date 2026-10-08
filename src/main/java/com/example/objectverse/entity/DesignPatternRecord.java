package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DesignPatternRecord extends BaseEntity {

    private Long projectId;

    private String patternName;

    private String patternType;

    private String reason;

    private String implementationAdvice;

    private String relatedClassIds;
}
