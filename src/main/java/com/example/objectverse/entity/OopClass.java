package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopClass extends BaseEntity {

    private Long projectId;

    private String className;

    private String packageName;

    private String description;

    private Boolean isAbstract;

    private String visibility;
}
