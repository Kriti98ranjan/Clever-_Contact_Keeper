package com.clever.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.clever.dao.UserRepository;
import com.clever.entities.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetching user from database using the UserRepository
        User user = userRepository.getUserbyUserName(username);
        
        if (user == null) {
            // If the user is not found, throw an exception
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        // Return an instance of CustomUserDetails, which implements UserDetails
        return new CustomUserDetails(user);
    }
}
