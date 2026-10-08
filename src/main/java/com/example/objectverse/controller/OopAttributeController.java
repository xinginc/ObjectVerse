package com.example.objectverse.controller;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.Project;
import com.example.objectverse.service.OopAttributeService;
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
@RequestMapping("/projects/{projectId}/classes/{classId}/attributes")
public class OopAttributeController {

    private final OopAttributeService oopAttributeService;
    private final OopClassService oopClassService;
    private final ProjectService projectService;

    public OopAttributeController(
            OopAttributeService oopAttributeService,
            OopClassService oopClassService,
            ProjectService projectService
    ) {
        this.oopAttributeService = oopAttributeService;
        this.oopClassService = oopClassService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(@PathVariable Long projectId, @PathVariable Long classId, Model model) {
        addAttributeContext(projectId, classId, model);
        model.addAttribute("attributes", oopAttributeService.findByClassId(classId));
        return "attributes/list";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long projectId, @PathVariable Long classId, Model model) {
        OopAttribute attribute = new OopAttribute();
        attribute.setProjectId(projectId);
        attribute.setClassId(classId);
        attribute.setVisibility("PRIVATE");
        attribute.setAttributeType("String");
        addAttributeContext(projectId, classId, model);
        model.addAttribute("attribute", attribute);
        model.addAttribute("formTitle", "新建属性");
        return "attributes/form";
    }

    @PostMapping
    public String create(@PathVariable Long projectId, @PathVariable Long classId, @ModelAttribute OopAttribute attribute) {
        attribute.setProjectId(projectId);
        attribute.setClassId(classId);
        oopAttributeService.create(attribute);
        return redirectToAttributeList(projectId, classId);
    }

    @GetMapping("/{attributeId}/edit")
    public String editForm(
            @PathVariable Long projectId,
            @PathVariable Long classId,
            @PathVariable Long attributeId,
            Model model
    ) {
        OopAttribute attribute = oopAttributeService.findById(attributeId);
        if (attribute == null) {
            return redirectToAttributeList(projectId, classId);
        }
        addAttributeContext(projectId, classId, model);
        model.addAttribute("attribute", attribute);
        model.addAttribute("formTitle", "编辑属性");
        return "attributes/form";
    }

    @PostMapping("/{attributeId}/update")
    public String update(
            @PathVariable Long projectId,
            @PathVariable Long classId,
            @PathVariable Long attributeId,
            @ModelAttribute OopAttribute attribute
    ) {
        attribute.setId(attributeId);
        attribute.setProjectId(projectId);
        attribute.setClassId(classId);
        oopAttributeService.update(attribute);
        return redirectToAttributeList(projectId, classId);
    }

    @PostMapping("/{attributeId}/delete")
    public String delete(@PathVariable Long projectId, @PathVariable Long classId, @PathVariable Long attributeId) {
        oopAttributeService.deleteById(attributeId);
        return redirectToAttributeList(projectId, classId);
    }

    private void addAttributeContext(Long projectId, Long classId, Model model) {
        Project project = projectService.findById(projectId);
        OopClass oopClass = oopClassService.findById(classId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("classId", classId);
        model.addAttribute("project", project);
        model.addAttribute("oopClass", oopClass);
    }

    private String redirectToAttributeList(Long projectId, Long classId) {
        return "redirect:/projects/" + projectId + "/classes/" + classId + "/attributes";
    }
}
