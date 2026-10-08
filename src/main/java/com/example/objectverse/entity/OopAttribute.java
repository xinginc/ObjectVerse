package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopAttribute extends BaseEntity {

    private Long projectId;

    private Long classId;

    private String attributeName;

    private String attributeType;

    private String visibility;

    private String defaultValue;

    private String description;
}
