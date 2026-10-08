package com.example.objectverse.service.impl;

import com.example.objectverse.ai.AiClient;
import com.example.objectverse.ai.AiPromptBuilder;
import com.example.objectverse.ai.MockAiClient;
import com.example.objectverse.ai.SimpleJsonParser;
import com.example.objectverse.dto.ai.CandidateAttributeDTO;
import com.example.objectverse.dto.ai.CandidateClassDTO;
import com.example.objectverse.dto.ai.CandidateMethodDTO;
import com.example.objectverse.dto.ai.CandidateRelationDTO;
import com.example.objectverse.dto.ai.RequirementAnalysisResult;
import com.example.objectverse.entity.AiConversation;
import com.example.objectverse.entity.AiTask;
import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import com.example.objectverse.entity.OopRelation;
import com.example.objectverse.entity.Project;
import com.example.objectverse.mapper.AiConversationMapper;
import com.example.objectverse.mapper.AiTaskMapper;
import com.example.objectverse.service.OopAttributeService;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopMethodService;
import com.example.objectverse.service.OopRelationService;
import com.example.objectverse.service.ProjectService;
import com.example.objectverse.service.RequirementAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RequirementAiServiceImpl implements RequirementAiService {

    private static final String TASK_TYPE = "REQUIREMENT_ANALYSIS";
    private static final String TASK_STATUS_SUCCESS = "SUCCESS";
    private static final String AGENT_NAME = "RequirementAnalysisAgent";

    private final ProjectService projectService;
    private final OopClassService oopClassService;
    private final OopAttributeService oopAttributeService;
    private final OopMethodService oopMethodService;
    private final OopRelationService oopRelationService;
    private final AiPromptBuilder aiPromptBuilder;
    private final AiClient aiClient;
    private final MockAiClient mockAiClient;
    private final AiTaskMapper aiTaskMapper;
    private final AiConversationMapper aiConversationMapper;
    private final String modelName;

    public RequirementAiServiceImpl(
            ProjectService projectService,
            OopClassService oopClassService,
            OopAttributeService oopAttributeService,
            OopMethodService oopMethodService,
            OopRelationService oopRelationService,
            AiPromptBuilder aiPromptBuilder,
            AiClient aiClient,
            MockAiClient mockAiClient,
            AiTaskMapper aiTaskMapper,
            AiConversationMapper aiConversationMapper,
            @Value("${ai.model:mock}") String modelName
    ) {
        this.projectService = projectService;
        this.oopClassService = oopClassService;
        this.oopAttributeService = oopAttributeService;
        this.oopMethodService = oopMethodService;
        this.oopRelationService = oopRelationService;
        this.aiPromptBuilder = aiPromptBuilder;
        this.aiClient = aiClient;
        this.mockAiClient = mockAiClient;
        this.aiTaskMapper = aiTaskMapper;
        this.aiConversationMapper = aiConversationMapper;
        this.modelName = StringUtils.hasText(modelName) ? modelName : "mock";
    }

    @Override
    public RequirementAnalysisResult analyzeRequirement(Long projectId, String requirementText) {
        Project project = projectService.findById(projectId);
        List<OopClass> classes = oopClassService.findByProjectId(projectId);
        Map<Long, List<OopAttribute>> attributesByClassId = new HashMap<>();
        Map<Long, List<OopMethod>> methodsByClassId = new HashMap<>();
        for (OopClass oopClass : classes) {
            attributesByClassId.put(oopClass.getId(), oopAttributeService.findByClassId(oopClass.getId()));
            methodsByClassId.put(oopClass.getId(), oopMethodService.findByClassId(oopClass.getId()));
        }
        List<OopRelation> relations = oopRelationService.findByProjectId(projectId);
        String prompt = aiPromptBuilder.buildRequirementAnalysisPrompt(
                project,
                classes,
                attributesByClassId,
                methodsByClassId,
                relations,
                requirementText
        );
        String response = aiClient.chat(prompt);
        RequirementAnalysisResult result = parseResult(response);
        result.setRawResponse(response);
        AiTask task = saveTask(projectId, requirementText, result.getSummary());
        saveConversation(projectId, task.getId(), prompt, response);
        return result;
    }

    @Override
    public void applyAnalysisResult(Long projectId, RequirementAnalysisResult result) {
        if (result == null) {
            return;
        }
        Map<String, OopClass> classMap = loadClassMap(projectId);
        for (CandidateClassDTO candidateClass : result.getCandidateClasses()) {
            if (!StringUtils.hasText(candidateClass.getClassName())) {
                continue;
            }
            OopClass oopClass = classMap.get(candidateClass.getClassName());
            if (oopClass == null) {
                oopClass = createClass(projectId, candidateClass);
                classMap.put(oopClass.getClassName(), oopClass);
                System.out.println("[AI-APPLY] Add class: " + oopClass.getClassName());
            } else {
                System.out.println("[AI-APPLY] Skip duplicated class: " + candidateClass.getClassName());
            }
            applyAttributes(projectId, oopClass, candidateClass.getAttributes());
            applyMethods(projectId, oopClass, candidateClass.getMethods());
        }
        applyRelations(projectId, result.getCandidateRelations(), classMap);
    }

    @Override
    public String buildAssistantDisplayText(RequirementAnalysisResult result) {
        if (result == null) {
            return "本轮没有获得可展示的分析结果。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("我已结合当前项目模型完成需求分析。\n\n");
        builder.append("【分析摘要】\n");
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
        builder.append("\n你可以点击下方“应用到当前项目模型”，将这些建议写入类设计器、属性设计器、方法设计器和关系设计器。");
        return builder.toString();
    }

    @Override
    public List<AiTask> findRequirementHistory(Long projectId) {
        return aiTaskMapper.findByProjectId(projectId);
    }

    private RequirementAnalysisResult parseResult(String response) {
        try {
            return toRequirementAnalysisResult(SimpleJsonParser.parseObject(extractJson(response)));
        } catch (Exception ex) {
            try {
                RequirementAnalysisResult fallback = toRequirementAnalysisResult(SimpleJsonParser.parseObject(mockAiClient.defaultJson()));
                fallback.setRawResponse(response);
                return fallback;
            } catch (Exception ignored) {
                RequirementAnalysisResult fallback = new RequirementAnalysisResult();
                fallback.setSummary("AI 返回内容解析失败，已使用空结果兜底。");
                fallback.setRawResponse(response);
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

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private AiTask saveTask(Long projectId, String requirementText, String summary) {
        LocalDateTime now = LocalDateTime.now();
        AiTask task = new AiTask();
        task.setProjectId(projectId);
        task.setTaskType(TASK_TYPE);
        task.setTaskStatus(TASK_STATUS_SUCCESS);
        task.setUserInput(requirementText);
        task.setResultSummary(summary);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        task.setDeleted(0);
        aiTaskMapper.insert(task);
        return task;
    }

    private void saveConversation(Long projectId, Long taskId, String prompt, String response) {
        LocalDateTime now = LocalDateTime.now();
        AiConversation conversation = new AiConversation();
        conversation.setProjectId(projectId);
        conversation.setTaskId(taskId);
        conversation.setAgentName(AGENT_NAME);
        conversation.setPromptContent(prompt);
        conversation.setResponseContent(response);
        conversation.setModelName(modelName);
        conversation.setCreateTime(now);
        conversation.setUpdateTime(now);
        conversation.setDeleted(0);
        aiConversationMapper.insert(conversation);
    }

    private Map<String, OopClass> loadClassMap(Long projectId) {
        Map<String, OopClass> classMap = new HashMap<>();
        for (OopClass oopClass : oopClassService.findByProjectId(projectId)) {
            classMap.put(oopClass.getClassName(), oopClass);
        }
        return classMap;
    }

    private OopClass createClass(Long projectId, CandidateClassDTO candidateClass) {
        OopClass oopClass = new OopClass();
        oopClass.setProjectId(projectId);
        oopClass.setClassName(candidateClass.getClassName());
        oopClass.setPackageName(StringUtils.hasText(candidateClass.getPackageName())
                ? candidateClass.getPackageName()
                : "com.objectverse.demo");
        oopClass.setDescription(candidateClass.getDescription());
        oopClass.setVisibility("PUBLIC");
        oopClass.setIsAbstract(false);
        oopClassService.create(oopClass);
        return oopClass;
    }

    private void applyAttributes(Long projectId, OopClass oopClass, List<CandidateAttributeDTO> candidates) {
        Map<String, OopAttribute> existing = new HashMap<>();
        for (OopAttribute attribute : oopAttributeService.findByClassId(oopClass.getId())) {
            existing.put(attribute.getAttributeName(), attribute);
        }
        for (CandidateAttributeDTO candidate : candidates) {
            if (!StringUtils.hasText(candidate.getAttributeName())) {
                continue;
            }
            if (existing.containsKey(candidate.getAttributeName())) {
                System.out.println("[AI-APPLY] Skip duplicated attribute: "
                        + oopClass.getClassName() + "." + candidate.getAttributeName());
                continue;
            }
            OopAttribute attribute = new OopAttribute();
            attribute.setProjectId(projectId);
            attribute.setClassId(oopClass.getId());
            attribute.setAttributeName(candidate.getAttributeName());
            attribute.setAttributeType(candidate.getAttributeType());
            attribute.setVisibility(candidate.getVisibility());
            attribute.setDescription(candidate.getDescription());
            oopAttributeService.create(attribute);
            System.out.println("[AI-APPLY] Add attribute: "
                    + oopClass.getClassName() + "." + candidate.getAttributeName());
        }
    }

    private void applyMethods(Long projectId, OopClass oopClass, List<CandidateMethodDTO> candidates) {
        Map<String, OopMethod> existing = new HashMap<>();
        for (OopMethod method : oopMethodService.findByClassId(oopClass.getId())) {
            existing.put(method.getMethodName(), method);
        }
        for (CandidateMethodDTO candidate : candidates) {
            if (!StringUtils.hasText(candidate.getMethodName())) {
                continue;
            }
            if (existing.containsKey(candidate.getMethodName())) {
                System.out.println("[AI-APPLY] Skip duplicated method: "
                        + oopClass.getClassName() + "." + candidate.getMethodName());
                continue;
            }
            OopMethod method = new OopMethod();
            method.setProjectId(projectId);
            method.setClassId(oopClass.getId());
            method.setMethodName(candidate.getMethodName());
            method.setReturnType(candidate.getReturnType());
            method.setParameters(candidate.getParameters());
            method.setVisibility(candidate.getVisibility());
            method.setDescription(candidate.getDescription());
            method.setMethodBody(candidate.getMethodBody());
            oopMethodService.create(method);
            System.out.println("[AI-APPLY] Add method: "
                    + oopClass.getClassName() + "." + candidate.getMethodName());
        }
    }

    private void applyRelations(Long projectId, List<CandidateRelationDTO> candidates, Map<String, OopClass> classMap) {
        List<OopRelation> existingRelations = oopRelationService.findByProjectId(projectId);
        for (CandidateRelationDTO candidate : candidates) {
            if (!StringUtils.hasText(candidate.getSourceClassName())
                    || !StringUtils.hasText(candidate.getTargetClassName())
                    || !StringUtils.hasText(candidate.getRelationType())) {
                continue;
            }
            OopClass sourceClass = classMap.get(candidate.getSourceClassName());
            OopClass targetClass = classMap.get(candidate.getTargetClassName());
            if (sourceClass == null || targetClass == null) {
                continue;
            }
            if (relationExists(existingRelations, sourceClass.getId(), targetClass.getId(), candidate.getRelationType())) {
                System.out.println("[AI-APPLY] Skip duplicated relation: "
                        + sourceClass.getClassName() + " -> " + targetClass.getClassName()
                        + " / " + candidate.getRelationType());
                continue;
            }
            OopRelation relation = new OopRelation();
            relation.setProjectId(projectId);
            relation.setSourceType("CLASS");
            relation.setSourceId(sourceClass.getId());
            relation.setTargetType("CLASS");
            relation.setTargetId(targetClass.getId());
            relation.setRelationType(candidate.getRelationType());
            relation.setDescription(candidate.getDescription());
            oopRelationService.create(relation);
            System.out.println("[AI-APPLY] Add relation: "
                    + sourceClass.getClassName() + " -> " + targetClass.getClassName()
                    + " / " + candidate.getRelationType());
        }
    }

    private boolean relationExists(List<OopRelation> relations, Long sourceId, Long targetId, String relationType) {
        for (OopRelation relation : relations) {
            if (sourceId.equals(relation.getSourceId())
                    && targetId.equals(relation.getTargetId())
                    && relationType.equals(relation.getRelationType())) {
                return true;
            }
        }
        return false;
    }
}
