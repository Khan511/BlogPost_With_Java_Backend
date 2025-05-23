package com.example.demo.security;

import lombok.Getter;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import com.example.demo.entities.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@RequiredArgsConstructor
// Custom implementation of Spring Security's UserDetails interface.
// This wraps around your UserEntity class to expose user info and roles to
// Spring Security.
public class CustomUserDetails implements UserDetails {
    // The actual user data from your database
    private final UserEntity userEntity;

    // Converts user roles into a collection of GrantedAuthority objects
    // that Spring Security uses for authorization (like hasRole('ADMIN'))
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        try {
            return userEntity.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect((Collectors.toList()));

        } catch (Exception e) {
            throw new UnsupportedOperationException("Error loading roles from UserDetails in CustomeUserDetails.");
        }
    }

    // Returns the hashed password of the user from the database
    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    // Returns the username (can also be email if you use that for login)
    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    // Custom helper to get the user's email if needed elsewhere
    public String getEmail() {
        return userEntity.getEmail();
    }

    public String getUserId() {
        return userEntity.getUserId();
    }

    public Long getId() {
        return userEntity.getId();
    }

    public String getImageUrl() {
        return userEntity.getImageUrl();
    }

    // The following methods tell Spring Security if the account is in good
    // standing:
    @Override
    public boolean isAccountNonExpired() {
        return userEntity.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userEntity.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userEntity.isEnabled();
    }
}
