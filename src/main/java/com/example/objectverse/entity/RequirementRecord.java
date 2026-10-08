package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequirementRecord extends BaseEntity {

    private Long projectId;

    private String rawRequirement;

    private String analysisStatus;

    private String summary;
}
