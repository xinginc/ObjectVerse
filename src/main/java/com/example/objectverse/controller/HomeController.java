package com.example.objectverse.controller;

import com.example.objectverse.entity.KnowledgeNote;
import com.example.objectverse.entity.Project;
import com.example.objectverse.entity.UserAccount;
import com.example.objectverse.mapper.UserAccountMapper;
import com.example.objectverse.service.KnowledgePracticeService;
import com.example.objectverse.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    private static final String LIKE = "LIKE";
    private static final String FAVORITE = "FAVORITE";

    private final KnowledgePracticeService knowledgePracticeService;
    private final ProjectService projectService;
    private final UserAccountMapper userAccountMapper;

    public HomeController(
            KnowledgePracticeService knowledgePracticeService,
            ProjectService projectService,
            UserAccountMapper userAccountMapper
    ) {
        this.knowledgePracticeService = knowledgePracticeService;
        this.projectService = projectService;
        this.userAccountMapper = userAccountMapper;
    }

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/knowledge-practice")
    public String knowledgePractice(HttpSession session, Model model) {
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("knowledgeNotes", knowledgePracticeService.findNotes(userId));
        model.addAttribute("practiceQuestions", knowledgePracticeService.findQuestions(userId));
        return "knowledge-practice";
    }

    @PostMapping("/knowledge-practice/generate")
    public String generatePractice(HttpSession session) {
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        knowledgePracticeService.generateQuestions(userId);
        return "redirect:/knowledge-practice";
    }

    @GetMapping("/notes-community")
    public String notesCommunity(Model model) {
        model.addAttribute("communityNotes", knowledgePracticeService.findPublicNotes());
        return "notes-community";
    }

    @GetMapping("/notes-community/new")
    public String newCommunityNote(HttpSession session) {
        if (resolveCurrentUserId(session) == null) {
            return "redirect:/login";
        }
        return "notes-community-form";
    }

    @PostMapping("/notes-community")
    public String publishCommunityNote(
            @RequestParam String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpSession session
    ) throws IOException {
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        String imageUrl = saveNoteImage(imageFile);
        KnowledgeNote note = knowledgePracticeService.publishCommunityNote(
                userId,
                title,
                category,
                description,
                content,
                tags,
                imageUrl
        );
        return "redirect:/notes-community/" + note.getId();
    }

    @GetMapping("/notes-community/{id}")
    public String communityNoteDetail(@PathVariable Long id, HttpSession session, Model model) {
        KnowledgeNote note = knowledgePracticeService.findNote(id);
        if (note == null) {
            return "redirect:/notes-community";
        }
        Long userId = resolveCurrentUserId(session);
        model.addAttribute("note", note);
        model.addAttribute("liked", userId != null && knowledgePracticeService.hasReaction(id, userId, LIKE));
        model.addAttribute("favorited", userId != null && knowledgePracticeService.hasReaction(id, userId, FAVORITE));
        return "notes-community-detail";
    }

    @PostMapping("/notes-community/{id}/like")
    public String toggleLike(@PathVariable Long id, HttpSession session) {
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        knowledgePracticeService.toggleReaction(id, userId, LIKE);
        return "redirect:/notes-community/" + id;
    }

    @PostMapping("/notes-community/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id, HttpSession session) {
        Long userId = resolveCurrentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        knowledgePracticeService.toggleReaction(id, userId, FAVORITE);
        return "redirect:/notes-community/" + id;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        UserAccount loginUser = requireLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        Long userId = loginUser.getId();
        List<Project> myProjects = projectService.findAll().stream()
                .filter(project -> userId.equals(project.getUserId()))
                .toList();
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("myProjects", myProjects);
        model.addAttribute("myNotes", knowledgePracticeService.findPublishedNotes(userId));
        model.addAttribute("favoriteNotes", knowledgePracticeService.findFavoritedNotes(userId));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) String bio,
            HttpSession session
    ) {
        UserAccount loginUser = requireLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        loginUser.setNickname(trimToEmpty(nickname));
        loginUser.setEmail(trimToEmpty(email));
        loginUser.setAge(age);
        loginUser.setOccupation(trimToEmpty(occupation));
        loginUser.setBio(trimToEmpty(bio));
        userAccountMapper.updateProfile(loginUser);
        UserAccount refreshed = userAccountMapper.findById(loginUser.getId());
        if (refreshed != null) {
            session.setAttribute("loginUser", refreshed);
        }
        return "redirect:/profile";
    }

    private String saveNoteImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return "";
        }
        String original = imageFile.getOriginalFilename();
        String extension = "";
        if (StringUtils.hasText(original) && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        String fileName = UUID.randomUUID() + extension;
        Path uploadDir = Paths.get("uploads", "notes").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(fileName);
        imageFile.transferTo(target);
        return "/uploads/notes/" + fileName;
    }

    private UserAccount requireLoginUser(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserAccount userAccount && userAccount.getId() != null) {
            UserAccount refreshed = userAccountMapper.findById(userAccount.getId());
            if (refreshed != null) {
                session.setAttribute("loginUser", refreshed);
                return refreshed;
            }
            return userAccount;
        }
        return null;
    }

    private Long resolveCurrentUserId(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserAccount userAccount && userAccount.getId() != null) {
            return userAccount.getId();
        }
        return null;
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.strip() : "";
    }
}
