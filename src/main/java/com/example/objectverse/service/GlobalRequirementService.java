package com.example.objectverse.service;

import com.example.objectverse.dto.ai.RequirementAnalysisResult;

public interface GlobalRequirementService {

    String chat(String requirementText);

    RequirementAnalysisResult analyze(String requirementText);

    String buildAssistantDisplayText(RequirementAnalysisResult result);
}
