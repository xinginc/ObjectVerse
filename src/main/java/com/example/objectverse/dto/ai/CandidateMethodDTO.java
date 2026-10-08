package com.example.objectverse.dto.ai;

import lombok.Data;

@Data
public class CandidateMethodDTO {

    private String methodName;

    private String returnType;

    private String parameters;

    private String visibility;

    private String description;

    private String methodBody;
}
