package com.example.contact_manager.contact_manager.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import com.example.contact_manager.contact_manager.entities.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.example.contact_manager.contact_manager.doa.UserRepository;
import jakarta.servlet.http.HttpSession;
import com.example.contact_manager.contact_manager.Helper.Message;

import org.springframework.core.io.ClassPathResource;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String Home(Model model)
    {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String About(Model model)
    {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());  // Changed to lowercase
        return "signup";
    }

    @GetMapping("/login")
    public String login(Model model)
    {
        model.addAttribute("title", "Login - Smart Contact Manager");
        return "login";
    }

    //handler for registration
    @PostMapping("/do_register")
    public String registerUser(
            @ModelAttribute("user") User user,
            @RequestParam(value = "imageFile", required = false) MultipartFile file,
            HttpSession session) {
        try {
            // Handle profile image upload
            if (file != null && !file.isEmpty()) {
                // Save the new profile image
                File saveFile = new ClassPathResource("static/img/profile").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Update the image URL in the user object
                user.setImageUrl(file.getOriginalFilename());
            } else {
                // Set a default profile image if no file is uploaded
                user.setImageUrl("default.png");
            }

            // Encrypt the password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Set other default values (e.g., roles, enabled status)
            user.setRole("ROLE_USER");
            user.setIs_enbled(true);

            // Save the user to the database
            userRepository.save(user);

            // Set success message
            session.setAttribute("message", new Message("User registered successfully!", "success"));
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
            return "redirect:/signup";
        }
    }
}
