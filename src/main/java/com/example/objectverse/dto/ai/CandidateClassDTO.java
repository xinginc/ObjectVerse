package com.example.objectverse.dto.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CandidateClassDTO {

    private String className;

    private String packageName;

    private String description;

    private List<CandidateAttributeDTO> attributes = new ArrayList<>();

    private List<CandidateMethodDTO> methods = new ArrayList<>();
}
