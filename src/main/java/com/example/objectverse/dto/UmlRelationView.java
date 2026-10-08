package com.example.objectverse.dto;

import lombok.Data;

@Data
public class UmlRelationView {

    private String sourceClassName;

    private String targetClassName;

    private String relationType;

    private String description;
}
