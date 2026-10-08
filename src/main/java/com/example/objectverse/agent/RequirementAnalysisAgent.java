package com.example.objectverse.agent;

public class RequirementAnalysisAgent extends AiAgent {

    @Override
    public String getAgentName() {
        return "RequirementAnalysisAgent";
    }

    @Override
    public String getAgentRole() {
        return "需求分析 Agent";
    }

    @Override
    public String execute(String input) {
        return "【需求分析模拟结果】\n"
                + "输入内容：" + input + "\n"
                + "识别结果：可以从需求中提取候选类、属性和方法，例如核心业务对象、对象状态和对象行为。\n"
                + "建议下一步：进入类设计器补充类名、属性、方法和类关系。";
    }
}
