package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {

    private Long userId;

    private String name;

    private String description;

    private String domainType;

    private String status;

    private String visibility;
}
