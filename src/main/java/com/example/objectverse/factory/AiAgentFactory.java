package com.example.objectverse.factory;

import com.example.objectverse.agent.AiAgent;
import com.example.objectverse.agent.ClassDesignAgent;
import com.example.objectverse.agent.CodeReviewAgent;
import com.example.objectverse.agent.RequirementAnalysisAgent;
import org.springframework.stereotype.Component;

@Component
public class AiAgentFactory {

    public AiAgent createAgent(String taskType) {
        if ("REQUIREMENT_ANALYSIS".equals(taskType)) {
            return new RequirementAnalysisAgent();
        }
        if ("CLASS_DESIGN".equals(taskType)) {
            return new ClassDesignAgent();
        }
        if ("CODE_REVIEW".equals(taskType)) {
            return new CodeReviewAgent();
        }
        throw new IllegalArgumentException("Unsupported AI task type: " + taskType);
    }
}
