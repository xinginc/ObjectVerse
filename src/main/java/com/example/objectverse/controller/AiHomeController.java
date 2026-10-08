package com.example.objectverse.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiHomeController {

    @GetMapping("/ai")
    public String index(HttpSession session, Model model) {
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        return "redirect:/idea-assistant";
    }
}
