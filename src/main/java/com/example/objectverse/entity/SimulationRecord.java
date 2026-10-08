package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimulationRecord extends BaseEntity {

    private Long projectId;

    private String simulationName;

    private String scenarioDescription;

    private String executionSteps;

    private String resultSummary;
}
