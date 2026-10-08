package com.example.objectverse.service.impl;

import com.example.objectverse.ai.AiClient;
import com.example.objectverse.ai.SimpleJsonParser;
import com.example.objectverse.dto.ai.CodeReviewResultDTO;
import com.example.objectverse.dto.ai.KnowledgePointDTO;
import com.example.objectverse.service.GlobalCodeReviewService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class GlobalCodeReviewServiceImpl implements GlobalCodeReviewService {

    private final AiClient aiClient;

    public GlobalCodeReviewServiceImpl(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public CodeReviewResultDTO analyze(String codeText) {
        String prompt = buildPrompt(codeText);
        String response = aiClient.chat(prompt);
        CodeReviewResultDTO result = parseResult(response);
        result.setRawResponse(response);
        return result;
    }

    private String buildPrompt(String codeText) {
        return """
                你是 ObjectVerse 的 Java 面向对象代码审查 Agent。
                请重点检查：封装、继承、多态、类职责、方法职责、访问修饰符、设计模式使用、代码可读性。
                请归纳涉及的知识点，例如封装、getter/setter、单一职责原则、组合关系、策略模式、工厂模式。
                请严格返回 JSON，不要 Markdown。
                JSON 结构：
                {
                  "summary": "代码总体评价",
                  "issues": ["发现的问题"],
                  "suggestions": ["面向对象改进建议"],
                  "knowledgePoints": [
                    {"title": "知识点标题", "category": "分类", "explanation": "解释"}
                  ],
                  "improvedCode": "推荐修改后的 Java 代码片段",
                  "noteContent": "适合记录到知识点笔记的一段总结"
                }
                待审查 Java 代码：
                """ + codeText;
    }

    private CodeReviewResultDTO parseResult(String response) {
        try {
            return toCodeReviewResult(SimpleJsonParser.parseObject(extractJson(response)));
        } catch (Exception ex) {
            CodeReviewResultDTO fallback = new CodeReviewResultDTO();
            fallback.setSummary("AI 返回内容解析失败，已生成基础审查结果。");
            fallback.getIssues().add("返回结果不是预期 JSON 格式，建议重新提交更完整的 Java 类代码。");
            fallback.getSuggestions().add("请提供包含字段、构造方法、业务方法和类关系的完整代码片段。");
            fallback.setImprovedCode("");
            fallback.setNoteContent("代码审查需要关注封装、职责划分、访问修饰符和类之间关系。");
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private CodeReviewResultDTO toCodeReviewResult(Map<String, Object> map) {
        CodeReviewResultDTO result = new CodeReviewResultDTO();
        result.setSummary(asString(map.get("summary")));
        addStrings(result.getIssues(), map.get("issues"));
        addStrings(result.getSuggestions(), map.get("suggestions"));
        Object knowledgePoints = map.get("knowledgePoints");
        if (knowledgePoints instanceof List<?> pointList) {
            for (Object item : pointList) {
                if (item instanceof Map<?, ?> pointMap) {
                    result.getKnowledgePoints().add(toKnowledgePoint((Map<String, Object>) pointMap));
                }
            }
        }
        result.setImprovedCode(asString(map.get("improvedCode")));
        result.setNoteContent(asString(map.get("noteContent")));
        return result;
    }

    private KnowledgePointDTO toKnowledgePoint(Map<String, Object> map) {
        KnowledgePointDTO point = new KnowledgePointDTO();
        point.setTitle(asString(map.get("title")));
        point.setCategory(asString(map.get("category")));
        point.setExplanation(asString(map.get("explanation")));
        return point;
    }

    private void addStrings(List<String> target, Object value) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                target.add(asString(item));
            }
        }
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
