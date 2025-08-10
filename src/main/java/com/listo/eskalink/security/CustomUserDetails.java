package com.listo.eskalink.security;

import com.listo.eskalink.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private UUID userId;
    private String email;
    private String password;
    private Boolean verified;
    private Collection<? extends GrantedAuthority> authorities;
    private UserRole role;

    @Override
    public String getUsername() {
        return email;
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
        return verified;
    }
}
