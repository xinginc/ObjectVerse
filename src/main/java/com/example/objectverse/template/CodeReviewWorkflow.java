package com.example.objectverse.template;

import com.example.objectverse.agent.AiAgent;
import com.example.objectverse.factory.AiAgentFactory;
import org.springframework.stereotype.Component;

@Component
public class CodeReviewWorkflow extends AbstractAiWorkflow {

    private final AiAgentFactory aiAgentFactory;

    public CodeReviewWorkflow(AiAgentFactory aiAgentFactory) {
        this.aiAgentFactory = aiAgentFactory;
    }

    @Override
    protected String buildPrompt(Long projectId, String userInput) {
        return "项目ID：" + projectId + "\n"
                + "请审查以下代码或设计片段中的封装、继承、多态和设计模式使用：\n"
                + userInput;
    }

    @Override
    protected AiAgent selectAgent() {
        return aiAgentFactory.createAgent("CODE_REVIEW");
    }

    @Override
    protected String formatResult(String response) {
        return "代码审查工作流执行完成\n\n" + response;
    }
}
