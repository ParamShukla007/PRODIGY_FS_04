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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;
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
            if (imageFile.isEmpty()) {
                System.out.println("File is empty, not uploaded");
            } else {
                // Save the file to the folder and update the name of contact
                contact.setImageUrl(imageFile.getOriginalFilename());
                File file = new ClassPathResource("static/img/contacts").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator + imageFile.getOriginalFilename());
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is uploaded successfully");
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
}
