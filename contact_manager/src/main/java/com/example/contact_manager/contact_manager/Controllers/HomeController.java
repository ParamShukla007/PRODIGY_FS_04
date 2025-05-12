package com.example.contact_manager.contact_manager.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

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
import jakarta.validation.Valid;

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
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam(value = "terms", defaultValue = "false") boolean terms,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            HttpSession session) {
        try {
            if(result.hasErrors()) {
                model.addAttribute("title", "Register - Smart Contact Manager");
                model.addAttribute("user", user);  // Add this line
                return "signup";  // Return with validation errors
            }

            // Then check terms
            if(!terms) {
                session.setAttribute("message", new Message("You must accept the terms and conditions", "alert-danger"));
                model.addAttribute("user", user);  // Add this line
                return "signup";
            }

            // Process image if present
            if(imageFile != null && !imageFile.isEmpty()) {
                // Define upload directory path
                String uploadDir = "src/main/resources/static/img/profile/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                
                // Save file
                Path path = Paths.get(uploadDir + fileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                
                // Set imageUrl in user object
                user.setImageUrl("/img/profile/" + fileName);
            }

            // Set default values and save
            user.setRole("ROLE_USER");
            user.setIs_enbled(true);
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            this.userRepository.save(user);
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "redirect:/login";  // Change this to redirect to login

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong!! " + e.getMessage(), "alert-danger"));
            model.addAttribute("user", user);  // Add this line
            return "signup";
        }
    }
}
