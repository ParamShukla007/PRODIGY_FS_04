package com.example.contact_manager.contact_manager.Controllers;

import com.example.contact_manager.contact_manager.entities.User;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class CustomUserDetails implements UserDetails{
      private User user;
      
      public CustomUserDetails(User user) {
        super();  
        this.user = user;
      } 

        @Override 
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }


        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            SimpleGrantedAuthority s = new SimpleGrantedAuthority(user.getRole());
            return List.of(s);
        }
}
