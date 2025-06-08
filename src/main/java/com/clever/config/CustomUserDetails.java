package com.clever.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.clever.entities.User;

public class CustomUserDetails implements UserDetails {
    
    private static final long serialVersionUID = 1L;

    private User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming `user.getRole()` returns a string role like "ROLE_USER" or "ROLE_ADMIN"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return List.of(authority);  // Return the list of authorities (roles)
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // Return the user's hashed password
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // Return the username (in this case, it's the user's email)
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
        return user.isEnabled();
    }
}
