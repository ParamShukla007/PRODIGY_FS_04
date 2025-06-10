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
import com.example.contact_manager.contact_manager.doa.UserRepository;
import jakarta.servlet.http.HttpSession;
import com.example.contact_manager.contact_manager.Helper.Message;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;
    
    @GetMapping("/")
    public String Home1(Model model)
    {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/home")
    public String Home(Model model)
    {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
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
                // Handle image upload
            if (file != null && !file.isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(file);
                    user.setImageUrl(imageUrl);
                    System.out.println("Image uploaded successfully: " + imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading image: " + e.getMessage());
                    e.printStackTrace();
                }
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

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request) {
        System.err.println("Error in HomeController: " + e.getMessage());
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "forward:/error";
    }
}
