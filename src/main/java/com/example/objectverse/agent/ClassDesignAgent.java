package com.example.objectverse.agent;

public class ClassDesignAgent extends AiAgent {

    @Override
    public String getAgentName() {
        return "ClassDesignAgent";
    }

    @Override
    public String getAgentRole() {
        return "类设计 Agent";
    }

    @Override
    public String execute(String input) {
        return "【类设计模拟结果】\n"
                + "输入内容：" + input + "\n"
                + "优化建议：类应保持单一职责，属性默认 private，通过方法暴露对象行为，避免一个类承担过多业务。";
    }
}
