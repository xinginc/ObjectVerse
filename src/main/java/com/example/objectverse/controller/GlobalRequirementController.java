package com.example.objectverse.controller;

import com.example.objectverse.dto.ai.RequirementAnalysisResult;
import com.example.objectverse.dto.ai.RequirementChatMessageDTO;
import com.example.objectverse.entity.Project;
import com.example.objectverse.entity.UserAccount;
import com.example.objectverse.service.GlobalRequirementService;
import com.example.objectverse.service.ProjectService;
import com.example.objectverse.service.RequirementAiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GlobalRequirementController {

    private static final String CHAT_KEY = "globalRequirementMessages";
    private static final String RESULT_KEY = "globalRequirementResult";
    private static final String TEXT_KEY = "globalRequirementText";
    private static final String APPLY_MESSAGE_KEY = "globalRequirementApplyMessage";
    public static final String PENDING_RESULT_KEY = "ideaPendingAnalysisResult";
    public static final String PENDING_TEXT_KEY = "ideaPendingRequirementText";
    public static final String PENDING_PROJECT_NAME_KEY = "ideaPendingProjectName";
    public static final String PENDING_PROJECT_DESCRIPTION_KEY = "ideaPendingProjectDescription";

    private final GlobalRequirementService globalRequirementService;
    private final RequirementAiService requirementAiService;
    private final ProjectService projectService;

    public GlobalRequirementController(
            GlobalRequirementService globalRequirementService,
            RequirementAiService requirementAiService,
            ProjectService projectService
    ) {
        this.globalRequirementService = globalRequirementService;
        this.requirementAiService = requirementAiService;
        this.projectService = projectService;
    }

    @GetMapping({"/idea-assistant", "/ai/idea-assistant", "/ai/requirement"})
    public String page(HttpSession session, Model model) {
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        model.addAttribute("analysisResult", session.getAttribute(RESULT_KEY));
        model.addAttribute("requirementText", session.getAttribute(TEXT_KEY));
        model.addAttribute("chatMessages", getChatMessages(session));
        model.addAttribute("myProjects", findMyProjects(session));
        model.addAttribute("applyMessage", session.getAttribute(APPLY_MESSAGE_KEY));
        session.removeAttribute(APPLY_MESSAGE_KEY);
        return "ai/global-requirement";
    }

    @PostMapping({"/idea-assistant/chat", "/ai/idea-assistant/chat"})
    public String chat(@RequestParam String requirementText, HttpSession session) {
        if (!StringUtils.hasText(requirementText)) {
            return "redirect:/idea-assistant";
        }
        List<RequirementChatMessageDTO> messages = getChatMessages(session);
        messages.add(newMessage("user", requirementText.strip()));

        String conversationRequirement = buildConversationRequirement(messages);
        String reply = globalRequirementService.chat(conversationRequirement);
        messages.add(newMessage("assistant", reply));

        session.setAttribute(TEXT_KEY, conversationRequirement);
        session.setAttribute(CHAT_KEY, messages);
        return "redirect:/idea-assistant";
    }

    @PostMapping({"/idea-assistant/analyze", "/ai/idea-assistant/analyze", "/ai/requirement/analyze"})
    public String analyze(@RequestParam String requirementText, HttpSession session) {
        List<RequirementChatMessageDTO> messages = getChatMessages(session);
        if (StringUtils.hasText(requirementText)) {
            messages.add(newMessage("user", requirementText.strip()));
        }
        String conversationRequirement = buildConversationRequirement(messages);
        if (!StringUtils.hasText(conversationRequirement)) {
            return "redirect:/idea-assistant";
        }
        RequirementAnalysisResult result = globalRequirementService.analyze(conversationRequirement);
        messages.add(newMessage("assistant", "我已根据上面的交流生成建模方案，结果在下方。你可以继续补充想法，再重新生成一版。"));
        session.setAttribute(TEXT_KEY, conversationRequirement);
        session.setAttribute(RESULT_KEY, result);
        session.setAttribute(CHAT_KEY, messages);
        return "redirect:/idea-assistant";
    }

    @PostMapping({"/idea-assistant/apply", "/ai/idea-assistant/apply"})
    public String apply(@RequestParam Long projectId, HttpSession session) {
        Object value = session.getAttribute(RESULT_KEY);
        if (!(value instanceof RequirementAnalysisResult result)) {
            session.setAttribute(APPLY_MESSAGE_KEY, "请先生成建模方案，再应用到项目。");
            return "redirect:/idea-assistant";
        }
        Project project = projectService.findById(projectId);
        Long currentUserId = resolveCurrentUserId(session);
        if (project == null || currentUserId == null || !currentUserId.equals(project.getUserId())) {
            session.setAttribute(APPLY_MESSAGE_KEY, "请选择你自己的有效项目。");
            return "redirect:/idea-assistant";
        }
        requirementAiService.applyAnalysisResult(projectId, result);
        return "redirect:/projects/" + projectId + "/uml";
    }

    @PostMapping({"/idea-assistant/new-project", "/ai/idea-assistant/new-project"})
    public String newProjectFromIdea(@RequestParam(required = false) String requirementText, HttpSession session) {
        List<RequirementChatMessageDTO> messages = getChatMessages(session);
        if (StringUtils.hasText(requirementText)) {
            messages.add(newMessage("user", requirementText.strip()));
        }
        String conversationRequirement = buildConversationRequirement(messages);
        if (!StringUtils.hasText(conversationRequirement)) {
            session.setAttribute(APPLY_MESSAGE_KEY, "请先输入项目构思，再生成新项目草稿。");
            return "redirect:/idea-assistant";
        }

        RequirementAnalysisResult result = resolveOrCreateAnalysisResult(conversationRequirement, session);
        session.setAttribute(PENDING_RESULT_KEY, result);
        session.setAttribute(PENDING_TEXT_KEY, conversationRequirement);
        session.setAttribute(PENDING_PROJECT_NAME_KEY, buildProjectName(result, conversationRequirement));
        session.setAttribute(PENDING_PROJECT_DESCRIPTION_KEY, buildProjectDescription(result, conversationRequirement));
        session.setAttribute(CHAT_KEY, messages);
        return "redirect:/projects/new?fromIdea=true";
    }

    @PostMapping({"/idea-assistant/clear", "/ai/idea-assistant/clear"})
    public String clear(HttpSession session) {
        session.removeAttribute(CHAT_KEY);
        session.removeAttribute(RESULT_KEY);
        session.removeAttribute(TEXT_KEY);
        return "redirect:/idea-assistant";
    }

    @SuppressWarnings("unchecked")
    private List<RequirementChatMessageDTO> getChatMessages(HttpSession session) {
        Object value = session.getAttribute(CHAT_KEY);
        if (value instanceof List<?>) {
            return (List<RequirementChatMessageDTO>) value;
        }
        List<RequirementChatMessageDTO> messages = new ArrayList<>();
        session.setAttribute(CHAT_KEY, messages);
        return messages;
    }

    private RequirementChatMessageDTO newMessage(String role, String content) {
        RequirementChatMessageDTO message = new RequirementChatMessageDTO();
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private String buildConversationRequirement(List<RequirementChatMessageDTO> messages) {
        StringBuilder builder = new StringBuilder();
        for (RequirementChatMessageDTO message : messages) {
            if ("user".equals(message.getRole()) && StringUtils.hasText(message.getContent())) {
                if (!builder.isEmpty()) {
                    builder.append("\n\n");
                }
                builder.append(message.getContent());
            }
        }
        return builder.toString();
    }

    private List<Project> findMyProjects(HttpSession session) {
        Long currentUserId = resolveCurrentUserId(session);
        if (currentUserId == null) {
            return List.of();
        }
        return projectService.findAll().stream()
                .filter(project -> currentUserId.equals(project.getUserId()))
                .toList();
    }

    private Long resolveCurrentUserId(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserAccount userAccount && userAccount.getId() != null) {
            return userAccount.getId();
        }
        return null;
    }

    private RequirementAnalysisResult resolveOrCreateAnalysisResult(String conversationRequirement, HttpSession session) {
        Object value = session.getAttribute(RESULT_KEY);
        if (value instanceof RequirementAnalysisResult result) {
            return result;
        }
        RequirementAnalysisResult result = globalRequirementService.analyze(conversationRequirement);
        List<RequirementChatMessageDTO> messages = getChatMessages(session);
        messages.add(newMessage("assistant", "我已生成一版建模方案，并准备将它带入新项目创建页。"));
        session.setAttribute(TEXT_KEY, conversationRequirement);
        session.setAttribute(RESULT_KEY, result);
        return result;
    }

    private String buildProjectName(RequirementAnalysisResult result, String conversationRequirement) {
        String summary = result != null ? result.getSummary() : "";
        String source = StringUtils.hasText(summary) ? summary : conversationRequirement;
        source = source.replaceAll("[\\r\\n]+", " ").strip();
        if (source.length() > 18) {
            source = source.substring(0, 18);
        }
        return StringUtils.hasText(source) ? source + "项目" : "AI 构思项目";
    }

    private String buildProjectDescription(RequirementAnalysisResult result, String conversationRequirement) {
        StringBuilder builder = new StringBuilder();
        if (result != null && StringUtils.hasText(result.getSummary())) {
            builder.append(result.getSummary()).append("\n\n");
        }
        builder.append("构思来源：\n").append(conversationRequirement);
        return builder.toString();
    }
}
