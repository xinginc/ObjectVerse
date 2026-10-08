package com.example.objectverse.controller;

import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.Project;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects/{projectId}/classes")
public class OopClassController {

    private final OopClassService oopClassService;
    private final ProjectService projectService;

    public OopClassController(OopClassService oopClassService, ProjectService projectService) {
        this.oopClassService = oopClassService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(@PathVariable Long projectId, Model model) {
        addProjectContext(projectId, model);
        model.addAttribute("classes", oopClassService.findByProjectId(projectId));
        return "classes/list";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long projectId, Model model) {
        OopClass oopClass = new OopClass();
        oopClass.setProjectId(projectId);
        oopClass.setVisibility("PUBLIC");
        oopClass.setIsAbstract(false);
        addProjectContext(projectId, model);
        model.addAttribute("oopClass", oopClass);
        model.addAttribute("formTitle", "新建类抽象");
        return "classes/form";
    }

    @PostMapping
    public String create(@PathVariable Long projectId, @ModelAttribute OopClass oopClass) {
        oopClass.setProjectId(projectId);
        oopClassService.create(oopClass);
        return "redirect:/projects/" + projectId + "/classes";
    }

    @GetMapping("/{classId}")
    public String detail(@PathVariable Long projectId, @PathVariable Long classId, Model model) {
        OopClass oopClass = oopClassService.findById(classId);
        if (oopClass == null) {
            return "redirect:/projects/" + projectId + "/classes";
        }
        addProjectContext(projectId, model);
        model.addAttribute("oopClass", oopClass);
        return "classes/detail";
    }

    @GetMapping("/{classId}/edit")
    public String editForm(@PathVariable Long projectId, @PathVariable Long classId, Model model) {
        OopClass oopClass = oopClassService.findById(classId);
        if (oopClass == null) {
            return "redirect:/projects/" + projectId + "/classes";
        }
        addProjectContext(projectId, model);
        model.addAttribute("oopClass", oopClass);
        model.addAttribute("formTitle", "编辑类抽象");
        return "classes/form";
    }

    @PostMapping("/{classId}/update")
    public String update(@PathVariable Long projectId, @PathVariable Long classId, @ModelAttribute OopClass oopClass) {
        oopClass.setId(classId);
        oopClass.setProjectId(projectId);
        oopClassService.update(oopClass);
        return "redirect:/projects/" + projectId + "/classes";
    }

    @PostMapping("/{classId}/delete")
    public String delete(@PathVariable Long projectId, @PathVariable Long classId) {
        oopClassService.deleteById(classId);
        return "redirect:/projects/" + projectId + "/classes";
    }

    private void addProjectContext(Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
    }
}
