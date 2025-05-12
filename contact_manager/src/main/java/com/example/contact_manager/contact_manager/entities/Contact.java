package com.example.contact_manager.contact_manager.entities;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
@Entity
@Table(name="Contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int contact_id;
    private String name;
    @Column(unique = true)
    private String email;
    private String work;
    @Column(length = 10)
    private String phone_no;
    @Column(length = 1000)
    private String description;
    private String imageUrl;
    @ManyToOne
    private User user;
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public int getContact_id() {
        return contact_id;
    }
    public void setContact_id(int contact_id) {
        this.contact_id = contact_id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getWork() {
        return work;
    }
    public void setWork(String work) {
        this.work = work;
    }
    public String getPhone_no() {
        return phone_no;
    }
    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public Contact() {
    }
    public Contact(int contact_id, String name, String email, String work, String phone_no, String description,
            String imageUrl) {
        this.contact_id = contact_id;
        this.name = name;
        this.email = email;
        this.work = work;
        this.phone_no = phone_no;
        this.description = description;
        this.imageUrl = imageUrl;
    }
    @Override
    public String toString() {
        return "Contact [contact_id=" + contact_id + ", name=" + name + ", email=" + email + ", work=" + work
                + ", phone_no=" + phone_no + ", description=" + description + ", imageUrl=" + imageUrl + "]";
    }

}
