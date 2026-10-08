package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopMethod extends BaseEntity {

    private Long projectId;

    private Long classId;

    private String methodName;

    private String returnType;

    private String parameters;

    private String visibility;

    private String description;

    private String methodBody;
}
