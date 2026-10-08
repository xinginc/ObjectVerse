package com.example.objectverse.service.impl;

import com.example.objectverse.ai.AiClient;
import com.example.objectverse.ai.MockAiClient;
import com.example.objectverse.ai.SimpleJsonParser;
import com.example.objectverse.dto.ai.CandidateAttributeDTO;
import com.example.objectverse.dto.ai.CandidateClassDTO;
import com.example.objectverse.dto.ai.CandidateMethodDTO;
import com.example.objectverse.dto.ai.CandidateRelationDTO;
import com.example.objectverse.dto.ai.RequirementAnalysisResult;
import com.example.objectverse.service.GlobalRequirementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class GlobalRequirementServiceImpl implements GlobalRequirementService {

    private final AiClient aiClient;
    private final MockAiClient mockAiClient;

    public GlobalRequirementServiceImpl(AiClient aiClient, MockAiClient mockAiClient) {
        this.aiClient = aiClient;
        this.mockAiClient = mockAiClient;
    }

    @Override
    public String chat(String requirementText) {
        return aiClient.chat(buildChatPrompt(requirementText));
    }

    @Override
    public RequirementAnalysisResult analyze(String requirementText) {
        String prompt = buildPrompt(requirementText);
        String response = aiClient.chat(prompt);
        RequirementAnalysisResult result = parseResult(response);
        result.setRawResponse(response);
        return result;
    }

    @Override
    public String buildAssistantDisplayText(RequirementAnalysisResult result) {
        if (result == null) {
            return "本轮没有获得可展示的分析结果。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("我已经根据当前构思生成一版面向对象建模草案。\n\n");
        builder.append("【构思摘要】\n");
        builder.append(StringUtils.hasText(result.getSummary()) ? result.getSummary() : "暂无摘要。");
        builder.append("\n\n【候选类】\n");
        if (result.getCandidateClasses().isEmpty()) {
            builder.append("暂无明确候选类。\n");
        } else {
            int index = 1;
            for (CandidateClassDTO candidateClass : result.getCandidateClasses()) {
                builder.append(index++)
                        .append(". ")
                        .append(candidateClass.getClassName())
                        .append("：")
                        .append(candidateClass.getDescription())
                        .append("\n");
            }
        }
        builder.append("\n【候选关系】\n");
        if (result.getCandidateRelations().isEmpty()) {
            builder.append("暂无明确候选关系。\n");
        } else {
            int index = 1;
            for (CandidateRelationDTO relation : result.getCandidateRelations()) {
                builder.append(index++)
                        .append(". ")
                        .append(relation.getSourceClassName())
                        .append(" -> ")
                        .append(relation.getTargetClassName())
                        .append("：")
                        .append(relation.getDescription())
                        .append("\n");
            }
        }
        if (!result.getSuggestions().isEmpty()) {
            builder.append("\n【下一步建议】\n");
            int index = 1;
            for (String suggestion : result.getSuggestions()) {
                builder.append(index++).append(". ").append(suggestion).append("\n");
            }
        }
        return builder.toString();
    }

    private String buildChatPrompt(String requirementText) {
        return """
                你是 ObjectVerse 的项目构思助手，正在和用户进行普通对话。
                你的任务是帮助用户澄清项目想法、业务对象、用户角色、业务流程、对象状态和对象关系。
                这一步只是交流想法，不要生成结构化建模方案，不要返回 JSON，不要列出完整候选类清单。
                如果信息不足，请自然地追问 1 到 3 个关键问题；如果信息已经比较清楚，请给出简短整理和下一步可补充的方向。

                当前对话中用户已经表达的想法：
                """ + requirementText;
    }

    private String buildPrompt(String requirementText) {
        return """
                你是 ObjectVerse 的项目构思 Agent。
                当前任务是全局项目构思，没有现成项目上下文，也没有 projectId。
                请根据用户连续描述的系统想法，给出 Java 面向对象建模建议。
                如果用户输入仍然模糊，请在 suggestions 中提示补充业务对象、对象状态、对象行为和对象关系。
                请严格返回 JSON，不要 Markdown，不要解释性废话。
                JSON 字段必须兼容：
                {
                  "summary": "系统功能摘要",
                  "candidateClasses": [
                    {
                      "className": "类名",
                      "packageName": "建议包名",
                      "description": "类职责",
                      "attributes": [
                        {"attributeName": "属性名", "attributeType": "类型", "visibility": "PRIVATE", "description": "说明"}
                      ],
                      "methods": [
                        {"methodName": "方法名", "returnType": "返回类型", "parameters": "参数", "visibility": "PUBLIC", "description": "说明", "methodBody": "示例方法体"}
                      ]
                    }
                  ],
                  "candidateRelations": [
                    {"sourceClassName": "源类", "targetClassName": "目标类", "relationType": "ASSOCIATION|DEPENDENCY|AGGREGATION|COMPOSITION|INHERITANCE|REALIZATION", "description": "关系说明"}
                  ],
                  "suggestions": ["建模建议"]
                }
                用户输入：
                """ + requirementText;
    }

    private RequirementAnalysisResult parseResult(String response) {
        try {
            return toRequirementAnalysisResult(SimpleJsonParser.parseObject(extractJson(response)));
        } catch (Exception ex) {
            try {
                return toRequirementAnalysisResult(SimpleJsonParser.parseObject(mockAiClient.defaultJson()));
            } catch (Exception ignored) {
                RequirementAnalysisResult fallback = new RequirementAnalysisResult();
                fallback.setSummary("AI 返回内容解析失败，请补充更明确的业务对象、状态、行为和关系。");
                return fallback;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private RequirementAnalysisResult toRequirementAnalysisResult(Map<String, Object> map) {
        RequirementAnalysisResult result = new RequirementAnalysisResult();
        result.setSummary(asString(map.get("summary")));
        Object classes = map.get("candidateClasses");
        if (classes instanceof List<?> classList) {
            for (Object item : classList) {
                if (item instanceof Map<?, ?> classMap) {
                    result.getCandidateClasses().add(toCandidateClass((Map<String, Object>) classMap));
                }
            }
        }
        Object relations = map.get("candidateRelations");
        if (relations instanceof List<?> relationList) {
            for (Object item : relationList) {
                if (item instanceof Map<?, ?> relationMap) {
                    result.getCandidateRelations().add(toCandidateRelation((Map<String, Object>) relationMap));
                }
            }
        }
        Object suggestions = map.get("suggestions");
        if (suggestions instanceof List<?> suggestionList) {
            for (Object suggestion : suggestionList) {
                result.getSuggestions().add(asString(suggestion));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private CandidateClassDTO toCandidateClass(Map<String, Object> map) {
        CandidateClassDTO candidateClass = new CandidateClassDTO();
        candidateClass.setClassName(asString(map.get("className")));
        candidateClass.setPackageName(asString(map.get("packageName")));
        candidateClass.setDescription(asString(map.get("description")));
        Object attributes = map.get("attributes");
        if (attributes instanceof List<?> attributeList) {
            for (Object item : attributeList) {
                if (item instanceof Map<?, ?> attributeMap) {
                    candidateClass.getAttributes().add(toCandidateAttribute((Map<String, Object>) attributeMap));
                }
            }
        }
        Object methods = map.get("methods");
        if (methods instanceof List<?> methodList) {
            for (Object item : methodList) {
                if (item instanceof Map<?, ?> methodMap) {
                    candidateClass.getMethods().add(toCandidateMethod((Map<String, Object>) methodMap));
                }
            }
        }
        return candidateClass;
    }

    private CandidateAttributeDTO toCandidateAttribute(Map<String, Object> map) {
        CandidateAttributeDTO attribute = new CandidateAttributeDTO();
        attribute.setAttributeName(asString(map.get("attributeName")));
        attribute.setAttributeType(asString(map.get("attributeType")));
        attribute.setVisibility(asString(map.get("visibility")));
        attribute.setDescription(asString(map.get("description")));
        return attribute;
    }

    private CandidateMethodDTO toCandidateMethod(Map<String, Object> map) {
        CandidateMethodDTO method = new CandidateMethodDTO();
        method.setMethodName(asString(map.get("methodName")));
        method.setReturnType(asString(map.get("returnType")));
        method.setParameters(asString(map.get("parameters")));
        method.setVisibility(asString(map.get("visibility")));
        method.setDescription(asString(map.get("description")));
        method.setMethodBody(asString(map.get("methodBody")));
        return method;
    }

    private CandidateRelationDTO toCandidateRelation(Map<String, Object> map) {
        CandidateRelationDTO relation = new CandidateRelationDTO();
        relation.setSourceClassName(asString(map.get("sourceClassName")));
        relation.setTargetClassName(asString(map.get("targetClassName")));
        relation.setRelationType(asString(map.get("relationType")));
        relation.setDescription(asString(map.get("description")));
        return relation;
    }

    private String extractJson(String response) {
        if (!StringUtils.hasText(response)) {
            return "{}";
        }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
