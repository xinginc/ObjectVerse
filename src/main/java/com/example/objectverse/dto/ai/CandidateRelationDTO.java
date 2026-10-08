package com.example.objectverse.dto.ai;

import lombok.Data;

@Data
public class CandidateRelationDTO {

    private String sourceClassName;

    private String targetClassName;

    private String relationType;

    private String description;
}
