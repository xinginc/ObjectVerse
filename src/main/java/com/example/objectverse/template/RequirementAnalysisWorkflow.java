package com.example.objectverse.template;

import com.example.objectverse.agent.AiAgent;
import com.example.objectverse.factory.AiAgentFactory;
import org.springframework.stereotype.Component;

@Component
public class RequirementAnalysisWorkflow extends AbstractAiWorkflow {

    private final AiAgentFactory aiAgentFactory;

    public RequirementAnalysisWorkflow(AiAgentFactory aiAgentFactory) {
        this.aiAgentFactory = aiAgentFactory;
    }

    @Override
    protected String buildPrompt(Long projectId, String userInput) {
        return "项目ID：" + projectId + "\n"
                + "请从以下需求中识别类、属性、方法和对象协作线索：\n"
                + userInput;
    }

    @Override
    protected AiAgent selectAgent() {
        return aiAgentFactory.createAgent("REQUIREMENT_ANALYSIS");
    }

    @Override
    protected String formatResult(String response) {
        return "需求分析工作流执行完成\n\n" + response;
    }
}
