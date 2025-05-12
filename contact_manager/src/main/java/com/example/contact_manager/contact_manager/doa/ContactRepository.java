package com.example.contact_manager.contact_manager.doa;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.contact_manager.contact_manager.entities.Contact;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    // Custom query methods can be defined here if needed
    // For example, to find contacts by user ID:
    // List<Contact> findByUserId(Integer userId);
    @Query("from Contact c where c.user.user_id = :userId")
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable p); // Find contacts by user ID
    //pagebale will contain current page and contact per page
}
