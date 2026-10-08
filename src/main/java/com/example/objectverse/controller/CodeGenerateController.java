package com.example.objectverse.controller;

import com.example.objectverse.entity.CodeFile;
import com.example.objectverse.entity.Project;
import com.example.objectverse.service.CodeGenerateService;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/projects/{projectId}")
public class CodeGenerateController {

    private final CodeGenerateService codeGenerateService;
    private final OopClassService oopClassService;
    private final ProjectService projectService;

    public CodeGenerateController(
            CodeGenerateService codeGenerateService,
            OopClassService oopClassService,
            ProjectService projectService
    ) {
        this.codeGenerateService = codeGenerateService;
        this.oopClassService = oopClassService;
        this.projectService = projectService;
    }

    @GetMapping("/code-generator")
    public String generator(@PathVariable Long projectId, Model model) {
        addGeneratorContext(projectId, model);
        return "code/generator";
    }

    @PostMapping("/code-generator/generate")
    public String generate(
            @PathVariable Long projectId,
            @RequestParam Long classId,
            @RequestParam(required = false) String strategyType
    ) {
        codeGenerateService.generateClassCode(projectId, classId, strategyType);
        return "redirect:/projects/" + projectId + "/code-generator";
    }

    @GetMapping("/code-files/{codeFileId}")
    public String preview(@PathVariable Long projectId, @PathVariable Long codeFileId, Model model) {
        CodeFile codeFile = codeGenerateService.findCodeFileById(codeFileId);
        if (codeFile == null) {
            return "redirect:/projects/" + projectId + "/code-generator";
        }
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("codeFile", codeFile);
        return "code/preview";
    }

    private void addGeneratorContext(Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("classes", oopClassService.findByProjectId(projectId));
        model.addAttribute("codeFiles", codeGenerateService.findGeneratedFiles(projectId));
    }
}
