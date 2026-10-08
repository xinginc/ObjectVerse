package com.example.objectverse.controller;

import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import com.example.objectverse.entity.Project;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopMethodService;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects/{projectId}/classes/{classId}/methods")
public class OopMethodController {

    private final OopMethodService oopMethodService;
    private final OopClassService oopClassService;
    private final ProjectService projectService;

    public OopMethodController(
            OopMethodService oopMethodService,
            OopClassService oopClassService,
            ProjectService projectService
    ) {
        this.oopMethodService = oopMethodService;
        this.oopClassService = oopClassService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(@PathVariable Long projectId, @PathVariable Long classId, Model model) {
        addMethodContext(projectId, classId, model);
        model.addAttribute("methods", oopMethodService.findByClassId(classId));
        return "methods/list";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long projectId, @PathVariable Long classId, Model model) {
        OopMethod method = new OopMethod();
        method.setProjectId(projectId);
        method.setClassId(classId);
        method.setVisibility("PUBLIC");
        method.setReturnType("void");
        method.setParameters("");
        method.setMethodBody("// TODO: 实现方法逻辑");
        addMethodContext(projectId, classId, model);
        model.addAttribute("method", method);
        model.addAttribute("formTitle", "新建方法");
        return "methods/form";
    }

    @PostMapping
    public String create(@PathVariable Long projectId, @PathVariable Long classId, @ModelAttribute OopMethod method) {
        method.setProjectId(projectId);
        method.setClassId(classId);
        oopMethodService.create(method);
        return redirectToMethodList(projectId, classId);
    }

    @GetMapping("/{methodId}/edit")
    public String editForm(
            @PathVariable Long projectId,
            @PathVariable Long classId,
            @PathVariable Long methodId,
            Model model
    ) {
        OopMethod method = oopMethodService.findById(methodId);
        if (method == null) {
            return redirectToMethodList(projectId, classId);
        }
        addMethodContext(projectId, classId, model);
        model.addAttribute("method", method);
        model.addAttribute("formTitle", "编辑方法");
        return "methods/form";
    }

    @PostMapping("/{methodId}/update")
    public String update(
            @PathVariable Long projectId,
            @PathVariable Long classId,
            @PathVariable Long methodId,
            @ModelAttribute OopMethod method
    ) {
        method.setId(methodId);
        method.setProjectId(projectId);
        method.setClassId(classId);
        oopMethodService.update(method);
        return redirectToMethodList(projectId, classId);
    }

    @PostMapping("/{methodId}/delete")
    public String delete(@PathVariable Long projectId, @PathVariable Long classId, @PathVariable Long methodId) {
        oopMethodService.deleteById(methodId);
        return redirectToMethodList(projectId, classId);
    }

    private void addMethodContext(Long projectId, Long classId, Model model) {
        Project project = projectService.findById(projectId);
        OopClass oopClass = oopClassService.findById(classId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("classId", classId);
        model.addAttribute("project", project);
        model.addAttribute("oopClass", oopClass);
    }

    private String redirectToMethodList(Long projectId, Long classId) {
        return "redirect:/projects/" + projectId + "/classes/" + classId + "/methods";
    }
}
