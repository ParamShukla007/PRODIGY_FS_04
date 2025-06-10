package com.example.contact_manager.contact_manager.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.contact_manager.contact_manager.Helper.Message;
import com.example.contact_manager.contact_manager.doa.ContactRepository;
import com.example.contact_manager.contact_manager.doa.UserRepository;
import com.example.contact_manager.contact_manager.entities.Contact;
import com.example.contact_manager.contact_manager.entities.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    // Add this method to make user available in all templates
    @ModelAttribute
    public void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.getUserByUserEmail(username);
            model.addAttribute("user", user);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal)
    {
        String username = principal.getName();
        //get the userd details using username(email)
        User user = userRepository.getUserByUserEmail(username);
        System.out.println("User: " + user);
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/add_contact")
    public String addContact(Model model, Principal principal) {
        String username = principal.getName();
        //get the userd details using username(email)
        User user = userRepository.getUserByUserEmail(username);
        System.out.println("User: " + user);
         model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        model.addAttribute("user", user);
        return "add_contact";
    }

    //processing the add contact form
    @PostMapping("/process-contact")
    public String processContact(
            @Valid @ModelAttribute Contact contact,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            HttpSession session,
            Principal principal) {
        try {
            // Processing and uploading file
            // Handle profile image upload
                // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    contact.setImageUrl(imageUrl);
                    System.out.println("Image uploaded successfully: " + imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading image: " + e.getMessage());
                    e.printStackTrace();
                }
            }


            if (result.hasErrors()) {
                model.addAttribute("title", "Add Contact");
                return "add_contact";
            }

            String username = principal.getName();
            User user = userRepository.getUserByUserEmail(username);

            contact.setUser(user);
            user.getContacts().add(contact);
            userRepository.save(user);

            // Set session attribute if not already set
            if (session.getAttribute("message") == null) {
                session.setAttribute("message", new Message("Contact added successfully!", "success"));
            }

            return "add_contact";

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
            return "add_contact";
        }
    }

    @GetMapping("/show-contacts")
    public String showContactsDefault(Model m, Principal p) {
        // Redirect to page 0 by default
        return "redirect:/user/show-contacts/0";
    }

    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal p) {
        m.addAttribute("title", "Show User Contacts");
        String userName = p.getName();
        User user = userRepository.getUserByUserEmail(userName);
      
        //current page and contact per page - 5
        Pageable pa = PageRequest.of(page, 6);
        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getUser_id(), pa);
        
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());
        
        return "show_contacts";
    }

    @GetMapping("/delete-contact/{contact_id}")
    public String deleteContact(@PathVariable("contact_id") Integer contact_id, Model model, HttpSession session)
    {
        try{
            Optional<Contact> contactOptional = contactRepository.findById(contact_id);
            Contact contact = contactOptional.get();
            contact.setUser(null);
            File contactImageFile = new ClassPathResource("static/img/contacts").getFile();
            File contactImage = new File(contactImageFile, contact.getImageUrl());
            if (contactImage.exists()) {
                    contactImage.delete();
            }
            this.contactRepository.delete(contact);
            session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
        }catch(Exception e){
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
        }
        return "redirect:/user/show-contacts/0";
    }

    @PostMapping("/update-contact/{contact_id}")
    public String updateFrom(@PathVariable("contact_id") Integer contact_id, Model m)
    {
        m.addAttribute("title", "Update Contact");
         Contact contact = this.contactRepository.findById(contact_id).get();
        m.addAttribute("contact", contact);
        return "update_contact";
    }

    @PostMapping("/process-update")
    public String processUpdate(Principal principal, 
                          @ModelAttribute Contact contact, 
                          @RequestParam("imageFile") MultipartFile file, 
                          Model m, 
                          HttpSession session) {
        try {
            // Get the contact from the database
            Contact oldContact = this.contactRepository.findById(contact.getContact_id()).get();
            
            // Processing and uploading file
            if (file != null && !file.isEmpty()) {
                try {
                    // Upload new image to Cloudinary
                    String imageUrl = cloudinaryService.uploadFile(file);
                    contact.setImageUrl(imageUrl);
                    System.out.println("Image uploaded successfully: " + imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading to Cloudinary: " + e.getMessage());
                    // If upload fails, keep the old image
                    contact.setImageUrl(oldContact.getImageUrl());
                }
            } else {
                // If no new file is uploaded, keep the existing image
                contact.setImageUrl(oldContact.getImageUrl());
            }

            // Set user and save contact
            User user = this.userRepository.getUserByUserEmail(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            
            session.setAttribute("message", new Message("Contact updated successfully!", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
        }
        
        return "redirect:/user/show-contacts/0";
    }
    @GetMapping("/profile")
    public String profile(Model m, Principal p)
    {
        String userName = p.getName();
        User user = userRepository.getUserByUserEmail(userName);
        m.addAttribute("user", user);
        m.addAttribute("title", "Profile");
        return "profile";
    }
    
    @PostMapping("/update-user/{user_id}")
    public String updateUser(@PathVariable("user_id") Integer user_id, Model m)
    {
        m.addAttribute("title", "Update User Password");
        User user = this.userRepository.findById(user_id).get();
        m.addAttribute("user", user);
        return "update_user";
    }

    @PostMapping("/process-update-user")
    public String processUpdateUser(
            Principal principal,
            @ModelAttribute User updatedUser,
            @RequestParam(value = "imageFile", required = false) MultipartFile file,
            HttpSession session) {
        try {
            // Retrieve the currently logged-in user
            User existingUser = this.userRepository.getUserByUserEmail(principal.getName());

            // Update user details only if they are provided
            if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
                existingUser.setName(updatedUser.getName());
            }
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPhone_no() != null && !updatedUser.getPhone_no().isEmpty()) {
                existingUser.setPhone_no(updatedUser.getPhone_no());
            }

            // Handle profile image upload using Cloudinary
            if (file != null && !file.isEmpty()) {
                try {
                    // Upload new image to Cloudinary
                    String imageUrl = cloudinaryService.uploadFile(file);
                    existingUser.setImageUrl(imageUrl);
                    System.out.println("Profile image uploaded successfully: " + imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading to Cloudinary: " + e.getMessage());
                    // If upload fails, keep the old image
                    session.setAttribute("message", new Message("Failed to upload image, but other details were updated.", "warning"));
                }
            }

            // Save the updated user to the database
            this.userRepository.save(existingUser);

            // Set success message
            session.setAttribute("message", new Message("User updated successfully!", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/update-password/{user_id}")
    public String updatePassword(@PathVariable("user_id") Integer user_id, Model m)
    {
        m.addAttribute("title", "Update User Password");
        User user = this.userRepository.findById(user_id).get();
        m.addAttribute("user", user);
        return "update_password";
    }

    //update user password
    @PostMapping("/process-update-password")
    public String updateUserPassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            HttpSession session) {
        try {
            // Validate that the passwords match
            if (!newPassword.equals(confirmPassword)) {
                session.setAttribute("message", new Message("Passwords do not match!", "danger"));
                return "redirect:/user/update-password/" + userRepository.getUserByUserEmail(principal.getName()).getUser_id();
            }

            // Get the currently logged-in user
            String userName = principal.getName();
            User oldUser = userRepository.getUserByUserEmail(userName);

            // Update the password
            oldUser.setPassword(passwordEncoder.encode(confirmPassword));
            this.userRepository.save(oldUser);

            // Success message
            session.setAttribute("message", new Message("Password updated successfully!", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
        }
        return "redirect:/user/profile";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request) {
        System.err.println("Error in UserController: " + e.getMessage());
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "forward:/error";
    }

    // Specific handler for pagination errors
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        System.err.println("Pagination Error: " + e.getMessage());
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value());
        return "forward:/error";
    }
}


