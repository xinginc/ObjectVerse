package com.example.objectverse.controller;

import com.example.objectverse.config.AiProperties;
import com.example.objectverse.dto.ai.RequirementAnalysisResult;
import com.example.objectverse.dto.ai.RequirementChatMessageDTO;
import com.example.objectverse.entity.Project;
import com.example.objectverse.service.ProjectService;
import com.example.objectverse.service.RequirementAiService;
import com.example.objectverse.service.UmlService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RequirementAiController {

    private final RequirementAiService requirementAiService;
    private final ProjectService projectService;
    private final UmlService umlService;
    private final AiProperties aiProperties;

    public RequirementAiController(
            RequirementAiService requirementAiService,
            ProjectService projectService,
            UmlService umlService,
            AiProperties aiProperties
    ) {
        this.requirementAiService = requirementAiService;
        this.projectService = projectService;
        this.umlService = umlService;
        this.aiProperties = aiProperties;
    }

    @GetMapping("/projects/{projectId}/ai/requirement")
    public String page(@PathVariable Long projectId, Model model, HttpSession session) {
        addPageContext(projectId, model);
        model.addAttribute("chatMessages", getChatMessages(session, projectId));
        model.addAttribute("analysisResult", session.getAttribute(resultKey(projectId)));
        return "ai/requirement";
    }

    @PostMapping("/projects/{projectId}/ai/requirement/analyze")
    public String analyze(
            @PathVariable Long projectId,
            @RequestParam String requirementText,
            HttpSession session
    ) {
        if (!StringUtils.hasText(requirementText)) {
            return "redirect:/projects/" + projectId + "/ai/requirement";
        }
        List<RequirementChatMessageDTO> messages = getChatMessages(session, projectId);
        messages.add(newMessage("user", requirementText));
        RequirementAnalysisResult result = requirementAiService.analyzeRequirement(projectId, requirementText);
        messages.add(newMessage("assistant", requirementAiService.buildAssistantDisplayText(result)));
        session.setAttribute(chatKey(projectId), messages);
        session.setAttribute(resultKey(projectId), result);
        return "redirect:/projects/" + projectId + "/ai/requirement";
    }

    @PostMapping("/projects/{projectId}/ai/requirement/apply")
    public String apply(@PathVariable Long projectId, HttpSession session) {
        RequirementAnalysisResult result = (RequirementAnalysisResult) session.getAttribute(resultKey(projectId));
        requirementAiService.applyAnalysisResult(projectId, result);
        return "redirect:/projects/" + projectId + "/uml";
    }

    @PostMapping("/projects/{projectId}/ai/requirement/clear")
    public String clear(@PathVariable Long projectId, HttpSession session) {
        session.removeAttribute(chatKey(projectId));
        session.removeAttribute(resultKey(projectId));
        return "redirect:/projects/" + projectId + "/ai/requirement";
    }

    private void addPageContext(Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        String provider = StringUtils.hasText(aiProperties.getProvider()) ? aiProperties.getProvider() : "mock";
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("classViews", umlService.buildClassDiagramViews(projectId));
        model.addAttribute("relationViews", umlService.buildRelationViews(projectId));
        model.addAttribute("historyTasks", requirementAiService.findRequirementHistory(projectId));
        model.addAttribute("aiProvider", provider);
        model.addAttribute("aiRealApi", isRealApiMode(provider));
        model.addAttribute("aiModeText", buildAiModeText(provider));
    }

    @SuppressWarnings("unchecked")
    private List<RequirementChatMessageDTO> getChatMessages(HttpSession session, Long projectId) {
        Object value = session.getAttribute(chatKey(projectId));
        if (value instanceof List<?>) {
            return (List<RequirementChatMessageDTO>) value;
        }
        List<RequirementChatMessageDTO> messages = new ArrayList<>();
        session.setAttribute(chatKey(projectId), messages);
        return messages;
    }

    private RequirementChatMessageDTO newMessage(String role, String content) {
        RequirementChatMessageDTO message = new RequirementChatMessageDTO();
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private String chatKey(Long projectId) {
        return "requirementChatMessages_" + projectId;
    }

    private String resultKey(Long projectId) {
        return "requirementLastResult_" + projectId;
    }

    private String buildAiModeText(String provider) {
        if (isRealApiMode(provider) && "qwen".equalsIgnoreCase(provider)) {
            return "当前使用阿里云百炼 Qwen 真实大模型 API。";
        }
        if (isRealApiMode(provider)) {
            return "当前使用真实大模型 API。";
        }
        return "当前使用 Mock AI，本地演示模式。";
    }

    private boolean isRealApiMode(String provider) {
        return StringUtils.hasText(provider)
                && !"mock".equalsIgnoreCase(provider)
                && StringUtils.hasText(aiProperties.getApiKey())
                && StringUtils.hasText(aiProperties.getBaseUrl())
                && StringUtils.hasText(aiProperties.getModel());
    }
}
