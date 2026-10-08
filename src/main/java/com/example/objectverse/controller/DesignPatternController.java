package com.example.objectverse.controller;

import com.example.objectverse.entity.Project;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DesignPatternController {

    private final ProjectService projectService;

    public DesignPatternController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects/{projectId}/design-patterns")
    public String list(@PathVariable Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        return "patterns/list";
    }
}
