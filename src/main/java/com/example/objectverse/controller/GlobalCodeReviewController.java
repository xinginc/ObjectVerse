package com.example.objectverse.controller;

import com.example.objectverse.dto.ai.CodeReviewResultDTO;
import com.example.objectverse.entity.UserAccount;
import com.example.objectverse.service.GlobalCodeReviewService;
import com.example.objectverse.service.KnowledgePracticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GlobalCodeReviewController {

    private final GlobalCodeReviewService globalCodeReviewService;
    private final KnowledgePracticeService knowledgePracticeService;

    public GlobalCodeReviewController(
            GlobalCodeReviewService globalCodeReviewService,
            KnowledgePracticeService knowledgePracticeService
    ) {
        this.globalCodeReviewService = globalCodeReviewService;
        this.knowledgePracticeService = knowledgePracticeService;
    }

    @GetMapping({"/code-review", "/ai/code-review"})
    public String page(HttpSession session, Model model) {
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        model.addAttribute("reviewResult", session.getAttribute("globalCodeReviewResult"));
        model.addAttribute("codeText", session.getAttribute("globalCodeText"));
        return "ai/global-code-review";
    }

    @PostMapping({"/code-review/analyze", "/ai/code-review/analyze"})
    public String analyze(@RequestParam String codeText, HttpSession session) {
        if (!StringUtils.hasText(codeText)) {
            return "redirect:/code-review";
        }
        CodeReviewResultDTO result = globalCodeReviewService.analyze(codeText);
        session.setAttribute("globalCodeText", codeText);
        session.setAttribute("globalCodeReviewResult", result);
        return "redirect:/code-review";
    }

    @PostMapping({"/code-review/save-note", "/ai/code-review/save-note"})
    public String saveNote(HttpSession session) {
        Object value = session.getAttribute("globalCodeReviewResult");
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (value instanceof CodeReviewResultDTO result && StringUtils.hasText(result.getNoteContent())) {
            knowledgePracticeService.saveNote(
                    userId,
                    "代码审查知识点",
                    resolveNoteCategory(result),
                    result.getNoteContent(),
                    "CODE_REVIEW"
            );
        }
        return "redirect:/knowledge-practice";
    }

    @PostMapping({"/code-review/save-knowledge/{index}", "/ai/code-review/save-knowledge/{index}"})
    public String saveKnowledgePoint(@PathVariable int index, HttpSession session) {
        Object value = session.getAttribute("globalCodeReviewResult");
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (value instanceof CodeReviewResultDTO result
                && index >= 0
                && index < result.getKnowledgePoints().size()) {
            var point = result.getKnowledgePoints().get(index);
            knowledgePracticeService.saveNote(
                    userId,
                    point.getTitle(),
                    point.getCategory(),
                    point.getExplanation(),
                    "CODE_REVIEW"
            );
        }
        return "redirect:/knowledge-practice";
    }

    @PostMapping({"/code-review/clear", "/ai/code-review/clear"})
    public String clear(HttpSession session) {
        session.removeAttribute("globalCodeText");
        session.removeAttribute("globalCodeReviewResult");
        return "redirect:/code-review";
    }

    private String resolveNoteCategory(CodeReviewResultDTO result) {
        if (!result.getKnowledgePoints().isEmpty()
                && StringUtils.hasText(result.getKnowledgePoints().get(0).getCategory())) {
            return result.getKnowledgePoints().get(0).getCategory();
        }
        return "代码规范";
    }

    private Long resolveCurrentUserId(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserAccount userAccount && userAccount.getId() != null) {
            return userAccount.getId();
        }
        return null;
    }
}
