package com.example.objectverse.controller;

import com.example.objectverse.entity.OopRelation;
import com.example.objectverse.entity.Project;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopRelationService;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects/{projectId}/relations")
public class OopRelationController {

    private final OopRelationService oopRelationService;
    private final OopClassService oopClassService;
    private final ProjectService projectService;

    public OopRelationController(
            OopRelationService oopRelationService,
            OopClassService oopClassService,
            ProjectService projectService
    ) {
        this.oopRelationService = oopRelationService;
        this.oopClassService = oopClassService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(@PathVariable Long projectId, Model model) {
        addRelationContext(projectId, model);
        model.addAttribute("relations", oopRelationService.findByProjectId(projectId));
        return "relations/list";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long projectId, Model model) {
        OopRelation relation = new OopRelation();
        relation.setProjectId(projectId);
        relation.setSourceType("CLASS");
        relation.setTargetType("CLASS");
        relation.setRelationType("ASSOCIATION");
        addRelationContext(projectId, model);
        model.addAttribute("relation", relation);
        model.addAttribute("formTitle", "新建类关系");
        return "relations/form";
    }

    @PostMapping
    public String create(@PathVariable Long projectId, @ModelAttribute OopRelation relation) {
        relation.setProjectId(projectId);
        relation.setSourceType("CLASS");
        relation.setTargetType("CLASS");
        oopRelationService.create(relation);
        return redirectToRelationList(projectId);
    }

    @GetMapping("/{relationId}/edit")
    public String editForm(@PathVariable Long projectId, @PathVariable Long relationId, Model model) {
        OopRelation relation = oopRelationService.findById(relationId);
        if (relation == null) {
            return redirectToRelationList(projectId);
        }
        addRelationContext(projectId, model);
        model.addAttribute("relation", relation);
        model.addAttribute("formTitle", "编辑类关系");
        return "relations/form";
    }

    @PostMapping("/{relationId}/update")
    public String update(@PathVariable Long projectId, @PathVariable Long relationId, @ModelAttribute OopRelation relation) {
        relation.setId(relationId);
        relation.setProjectId(projectId);
        relation.setSourceType("CLASS");
        relation.setTargetType("CLASS");
        oopRelationService.update(relation);
        return redirectToRelationList(projectId);
    }

    @PostMapping("/{relationId}/delete")
    public String delete(@PathVariable Long projectId, @PathVariable Long relationId) {
        oopRelationService.deleteById(relationId);
        return redirectToRelationList(projectId);
    }

    private void addRelationContext(Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("classes", oopClassService.findByProjectId(projectId));
    }

    private String redirectToRelationList(Long projectId) {
        return "redirect:/projects/" + projectId + "/relations";
    }
}
