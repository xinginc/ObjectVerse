package com.example.objectverse.controller;

import com.example.objectverse.dto.ai.KnowledgeNoteDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AiNotesController {

    @GetMapping("/ai/notes")
    public String notes(HttpSession session, Model model) {
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        model.addAttribute("knowledgeNotes", getNotes(session));
        return "ai/notes";
    }

    @SuppressWarnings("unchecked")
    private List<KnowledgeNoteDTO> getNotes(HttpSession session) {
        Object value = session.getAttribute("knowledgeNotes");
        if (value instanceof List<?>) {
            return (List<KnowledgeNoteDTO>) value;
        }
        return new ArrayList<>();
    }
}
