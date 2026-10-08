package com.example.objectverse.service.impl;

import com.example.objectverse.service.AiDemoService;
import com.example.objectverse.template.CodeReviewWorkflow;
import com.example.objectverse.template.RequirementAnalysisWorkflow;
import org.springframework.stereotype.Service;

@Service
public class AiDemoServiceImpl implements AiDemoService {

    private final RequirementAnalysisWorkflow requirementAnalysisWorkflow;
    private final CodeReviewWorkflow codeReviewWorkflow;

    public AiDemoServiceImpl(
            RequirementAnalysisWorkflow requirementAnalysisWorkflow,
            CodeReviewWorkflow codeReviewWorkflow
    ) {
        this.requirementAnalysisWorkflow = requirementAnalysisWorkflow;
        this.codeReviewWorkflow = codeReviewWorkflow;
    }

    @Override
    public String runRequirementAnalysis(Long projectId, String userInput) {
        return requirementAnalysisWorkflow.executeWorkflow(projectId, userInput);
    }

    @Override
    public String runCodeReview(Long projectId, String userInput) {
        return codeReviewWorkflow.executeWorkflow(projectId, userInput);
    }
}
