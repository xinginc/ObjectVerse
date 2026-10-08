package com.example.objectverse.dto.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequirementAnalysisResult {

    private String summary;

    private List<CandidateClassDTO> candidateClasses = new ArrayList<>();

    private List<CandidateRelationDTO> candidateRelations = new ArrayList<>();

    private List<String> suggestions = new ArrayList<>();

    private String rawResponse;
}
