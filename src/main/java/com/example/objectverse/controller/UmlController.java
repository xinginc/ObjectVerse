package com.example.objectverse.controller;

import com.example.objectverse.entity.Project;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopRelationService;
import com.example.objectverse.service.ProjectService;
import com.example.objectverse.service.UmlService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UmlController {

    private final UmlService umlService;
    private final ProjectService projectService;
    private final OopClassService oopClassService;
    private final OopRelationService oopRelationService;

    public UmlController(
            UmlService umlService,
            ProjectService projectService,
            OopClassService oopClassService,
            OopRelationService oopRelationService
    ) {
        this.umlService = umlService;
        this.projectService = projectService;
        this.oopClassService = oopClassService;
        this.oopRelationService = oopRelationService;
    }

    @GetMapping("/projects/{projectId}/uml")
    public String classDiagram(@PathVariable Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("classes", umlService.buildClassDiagramViews(projectId));
        model.addAttribute("relations", umlService.buildRelationViews(projectId));
        model.addAttribute("diagramText", umlService.generateClassDiagramText(projectId));
        return "uml/class-diagram";
    }
}
