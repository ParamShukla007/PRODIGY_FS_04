package com.example.contact_manager.contact_manager.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.ui.Model;

@Controller
public class ErrorController {
    
    @GetMapping("/error")
    public String handleError(@RequestAttribute("errorMessage") String errorMessage, Model model) {
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }
}
