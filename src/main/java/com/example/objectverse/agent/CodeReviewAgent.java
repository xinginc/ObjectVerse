package com.example.objectverse.agent;

public class CodeReviewAgent extends AiAgent {

    @Override
    public String getAgentName() {
        return "CodeReviewAgent";
    }

    @Override
    public String getAgentRole() {
        return "代码审查 Agent";
    }

    @Override
    public String execute(String input) {
        return "【代码审查模拟结果】\n"
                + "输入内容：" + input + "\n"
                + "审查重点：检查封装是否合理、继承是否必要、多态扩展点是否清晰，以及策略、工厂、模板方法等设计模式是否服务于真实业务。";
    }
}
