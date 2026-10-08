package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiTask extends BaseEntity {

    private Long projectId;

    private String taskType;

    private String taskStatus;

    private String userInput;

    private String resultSummary;
}
