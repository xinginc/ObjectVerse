package com.example.objectverse.service;

import com.example.objectverse.dto.ai.RequirementAnalysisResult;
import com.example.objectverse.entity.AiTask;

import java.util.List;

public interface RequirementAiService {

    RequirementAnalysisResult analyzeRequirement(Long projectId, String requirementText);

    void applyAnalysisResult(Long projectId, RequirementAnalysisResult result);

    String buildAssistantDisplayText(RequirementAnalysisResult result);

    List<AiTask> findRequirementHistory(Long projectId);
}
