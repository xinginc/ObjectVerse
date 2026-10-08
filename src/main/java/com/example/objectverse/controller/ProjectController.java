package com.example.objectverse.controller;

import com.example.objectverse.dto.ai.RequirementAnalysisResult;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.Project;
import com.example.objectverse.entity.UserAccount;
import com.example.objectverse.service.OopAttributeService;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopMethodService;
import com.example.objectverse.service.OopRelationService;
import com.example.objectverse.service.ProjectService;
import com.example.objectverse.service.RequirementAiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private static final String PUBLIC_VISIBILITY = "PUBLIC";

    private final ProjectService projectService;
    private final OopClassService oopClassService;
    private final OopAttributeService oopAttributeService;
    private final OopMethodService oopMethodService;
    private final OopRelationService oopRelationService;
    private final RequirementAiService requirementAiService;

    public ProjectController(
            ProjectService projectService,
            OopClassService oopClassService,
            OopAttributeService oopAttributeService,
            OopMethodService oopMethodService,
            OopRelationService oopRelationService,
            RequirementAiService requirementAiService
    ) {
        this.projectService = projectService;
        this.oopClassService = oopClassService;
        this.oopAttributeService = oopAttributeService;
        this.oopMethodService = oopMethodService;
        this.oopRelationService = oopRelationService;
        this.requirementAiService = requirementAiService;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        List<Project> projects = projectService.findAll();
        Long currentUserId = resolveCurrentUserId(session);
        List<Project> myProjects = projects.stream()
                .filter(project -> currentUserId != null && currentUserId.equals(project.getUserId()))
                .toList();
        List<Project> publicProjects = projects.stream()
                .filter(project -> PUBLIC_VISIBILITY.equals(project.getVisibility()))
                .toList();

        model.addAttribute("projects", projects);
        model.addAttribute("myProjects", myProjects);
        model.addAttribute("publicProjects", publicProjects);
        model.addAttribute("myProjectCount", myProjects.size());
        model.addAttribute("publicProjectCount", publicProjects.size());
        model.addAttribute("recommendedProjectCount", publicProjects.size());
        addProjectCardStats(projects, model);
        return "projects/list";
    }

    @GetMapping("/new")
    public String newForm(Model model, HttpSession session) {
        if (resolveCurrentUserId(session) == null) {
            return "redirect:/login";
        }
        Project project = new Project();
        Object pendingName = session.getAttribute(GlobalRequirementController.PENDING_PROJECT_NAME_KEY);
        Object pendingDescription = session.getAttribute(GlobalRequirementController.PENDING_PROJECT_DESCRIPTION_KEY);
        if (pendingName instanceof String name) {
            project.setName(name);
        }
        if (pendingDescription instanceof String description) {
            project.setDescription(description);
        }
        project.setDomainType("Java/OOP 建模");
        project.setStatus("DRAFT");
        project.setVisibility("PRIVATE");
        model.addAttribute("project", project);
        model.addAttribute("formTitle", "新建 OOP 建模项目");
        return "projects/form";
    }

    @PostMapping
    public String create(@ModelAttribute Project project, HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserAccount userAccount) {
            project.setUserId(userAccount.getId());
        } else {
            return "redirect:/login";
        }
        projectService.create(project);
        applyPendingIdeaResult(project, session);
        return "redirect:/projects";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        if (project == null) {
            return "redirect:/projects";
        }
        model.addAttribute("project", project);
        addDashboardStats(id, model);
        return "projects/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        if (project == null) {
            return "redirect:/projects";
        }
        model.addAttribute("project", project);
        model.addAttribute("formTitle", "编辑 OOP 建模项目");
        return "projects/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute Project project) {
        project.setId(id);
        projectService.update(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/visibility")
    public String updateVisibility(@PathVariable Long id, @RequestParam String visibility) {
        projectService.updateVisibility(id, visibility);
        return "redirect:/projects";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return "redirect:/projects";
    }

    private void addDashboardStats(Long projectId, Model model) {
        List<OopClass> classes = oopClassService.findByProjectId(projectId);
        int attributeCount = 0;
        int methodCount = 0;
        for (OopClass oopClass : classes) {
            attributeCount += oopAttributeService.findByClassId(oopClass.getId()).size();
            methodCount += oopMethodService.findByClassId(oopClass.getId()).size();
        }
        model.addAttribute("classCount", classes.size());
        model.addAttribute("attributeCount", attributeCount);
        model.addAttribute("methodCount", methodCount);
        model.addAttribute("relationCount", oopRelationService.findByProjectId(projectId).size());
        model.addAttribute("recentClasses", classes.stream().limit(3).toList());
    }

    private void addProjectCardStats(List<Project> projects, Model model) {
        Map<Long, Integer> classCounts = new HashMap<>();
        Map<Long, Integer> attributeCounts = new HashMap<>();
        Map<Long, Integer> methodCounts = new HashMap<>();
        Map<Long, Integer> relationCounts = new HashMap<>();
        for (Project project : projects) {
            List<OopClass> classes = oopClassService.findByProjectId(project.getId());
            int attributeCount = 0;
            int methodCount = 0;
            for (OopClass oopClass : classes) {
                attributeCount += oopAttributeService.findByClassId(oopClass.getId()).size();
                methodCount += oopMethodService.findByClassId(oopClass.getId()).size();
            }
            classCounts.put(project.getId(), classes.size());
            attributeCounts.put(project.getId(), attributeCount);
            methodCounts.put(project.getId(), methodCount);
            relationCounts.put(project.getId(), oopRelationService.findByProjectId(project.getId()).size());
        }
        model.addAttribute("classCounts", classCounts);
        model.addAttribute("attributeCounts", attributeCounts);
        model.addAttribute("methodCounts", methodCounts);
        model.addAttribute("relationCounts", relationCounts);
    }

    private Long resolveCurrentUserId(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserAccount userAccount && userAccount.getId() != null) {
            return userAccount.getId();
        }
        return null;
    }

    private void applyPendingIdeaResult(Project project, HttpSession session) {
        Object value = session.getAttribute(GlobalRequirementController.PENDING_RESULT_KEY);
        if (value instanceof RequirementAnalysisResult result && project.getId() != null) {
            requirementAiService.applyAnalysisResult(project.getId(), result);
            session.removeAttribute(GlobalRequirementController.PENDING_RESULT_KEY);
            session.removeAttribute(GlobalRequirementController.PENDING_TEXT_KEY);
            session.removeAttribute(GlobalRequirementController.PENDING_PROJECT_NAME_KEY);
            session.removeAttribute(GlobalRequirementController.PENDING_PROJECT_DESCRIPTION_KEY);
        }
    }
}
