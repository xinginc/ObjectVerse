package com.example.objectverse.controller;

import com.example.objectverse.entity.Project;
import com.example.objectverse.service.AiDemoService;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AiDemoController {

    private final AiDemoService aiDemoService;
    private final ProjectService projectService;

    public AiDemoController(AiDemoService aiDemoService, ProjectService projectService) {
        this.aiDemoService = aiDemoService;
        this.projectService = projectService;
    }

    @GetMapping("/projects/{projectId}/ai-demo")
    public String demo(@PathVariable Long projectId, Model model) {
        addDemoContext(projectId, model, "", "");
        return "ai/demo";
    }

    @PostMapping("/projects/{projectId}/ai-demo/requirement-analysis")
    public String requirementAnalysis(
            @PathVariable Long projectId,
            @RequestParam String userInput,
            Model model
    ) {
        String result = aiDemoService.runRequirementAnalysis(projectId, userInput);
        addDemoContext(projectId, model, userInput, result);
        return "ai/demo";
    }

    @PostMapping("/projects/{projectId}/ai-demo/code-review")
    public String codeReview(
            @PathVariable Long projectId,
            @RequestParam String userInput,
            Model model
    ) {
        String result = aiDemoService.runCodeReview(projectId, userInput);
        addDemoContext(projectId, model, userInput, result);
        return "ai/demo";
    }

    private void addDemoContext(Long projectId, Model model, String userInput, String result) {
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("userInput", userInput);
        model.addAttribute("result", result);
    }
}
