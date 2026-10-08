package com.example.objectverse.ai;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import com.example.objectverse.entity.OopRelation;
import com.example.objectverse.entity.Project;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AiPromptBuilder {

    public String buildRequirementAnalysisPrompt(
            Project project,
            List<OopClass> classes,
            Map<Long, List<OopAttribute>> attributesByClassId,
            Map<Long, List<OopMethod>> methodsByClassId,
            List<OopRelation> relations,
            String requirementText
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 ObjectVerse 的 AI 需求分析 Agent。\n")
                .append("请基于当前项目上下文和用户新需求，输出面向对象建模建议。\n\n")
                .append("当前项目 ID：").append(project.getId()).append("\n")
                .append("当前项目名称：").append(project.getName()).append("\n\n")
                .append("当前已有类、属性、方法：\n");
        for (OopClass oopClass : classes) {
            prompt.append("- 类：").append(oopClass.getClassName())
                    .append("，包名：").append(oopClass.getPackageName())
                    .append("，说明：").append(oopClass.getDescription()).append("\n");
            for (OopAttribute attribute : attributesByClassId.getOrDefault(oopClass.getId(), List.of())) {
                prompt.append("  - 属性：").append(attribute.getAttributeName())
                        .append(": ").append(attribute.getAttributeType())
                        .append("，可见性：").append(attribute.getVisibility()).append("\n");
            }
            for (OopMethod method : methodsByClassId.getOrDefault(oopClass.getId(), List.of())) {
                prompt.append("  - 方法：").append(method.getMethodName())
                        .append("(").append(method.getParameters()).append("): ")
                        .append(method.getReturnType())
                        .append("，可见性：").append(method.getVisibility()).append("\n");
            }
        }
        prompt.append("\n当前已有关系：\n");
        for (OopRelation relation : relations) {
            prompt.append("- sourceId=").append(relation.getSourceId())
                    .append(", targetId=").append(relation.getTargetId())
                    .append(", relationType=").append(relation.getRelationType())
                    .append(", description=").append(relation.getDescription()).append("\n");
        }
        prompt.append("\n用户输入的新需求：\n").append(requirementText).append("\n\n")
                .append("请只返回严格 JSON，不要返回 Markdown，不要添加解释文字。JSON 格式如下：\n")
                .append("""
                        {
                          "summary": "需求分析摘要",
                          "candidateClasses": [
                            {
                              "className": "Pet",
                              "packageName": "com.objectverse.demo.pet",
                              "description": "宠物对象",
                              "attributes": [
                                {
                                  "attributeName": "name",
                                  "attributeType": "String",
                                  "visibility": "PRIVATE",
                                  "description": "宠物名称"
                                }
                              ],
                              "methods": [
                                {
                                  "methodName": "showBasicInfo",
                                  "returnType": "String",
                                  "parameters": "",
                                  "visibility": "PUBLIC",
                                  "description": "展示宠物基础信息",
                                  "methodBody": "return name;"
                                }
                              ]
                            }
                          ],
                          "candidateRelations": [
                            {
                              "sourceClassName": "PetOwner",
                              "targetClassName": "Pet",
                              "relationType": "COMPOSITION",
                              "description": "宠物主人拥有宠物"
                            }
                          ],
                          "suggestions": [
                            "建议补充预约类",
                            "建议为支付功能创建 Payment 类"
                          ]
                        }
                        """);
        return prompt.toString();
    }
}
