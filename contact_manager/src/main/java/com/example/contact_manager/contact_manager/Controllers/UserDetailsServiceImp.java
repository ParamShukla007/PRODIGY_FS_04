package com.example.contact_manager.contact_manager.Controllers;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.contact_manager.contact_manager.doa.UserRepository;
import com.example.contact_manager.contact_manager.entities.User;
import org.springframework.beans.factory.annotation.Autowired;




public class UserDetailsServiceImp implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        User user = userRepository.getUserByUserEmail(username);
        if(user == null)
        {
            throw new UsernameNotFoundException("Could not find user!!");
        }
        
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;
    }
    
}
