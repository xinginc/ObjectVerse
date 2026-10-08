package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UmlDiagram extends BaseEntity {

    private Long projectId;

    private String diagramType;

    private String diagramData;

    private String description;
}
