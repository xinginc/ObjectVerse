package com.example.objectverse.template;

import com.example.objectverse.agent.AiAgent;

public abstract class AbstractAiWorkflow {

    public final String executeWorkflow(Long projectId, String userInput) {
        String prompt = buildPrompt(projectId, userInput);
        AiAgent agent = selectAgent();
        String response = callAgent(agent, prompt);
        return formatResult(response);
    }

    protected abstract String buildPrompt(Long projectId, String userInput);

    protected abstract AiAgent selectAgent();

    protected String callAgent(AiAgent agent, String prompt) {
        return agent.execute(prompt);
    }

    protected String formatResult(String response) {
        return response;
    }
}
