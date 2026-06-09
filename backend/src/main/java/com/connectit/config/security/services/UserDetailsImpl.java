package com.connectit.config.security.services;

import com.connectit.core.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private boolean passwordResetRequired;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, Long companyId, String username, String email, String password,
                           boolean passwordResetRequired, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.companyId = companyId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordResetRequired = passwordResetRequired;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getPermKey()))
                .collect(Collectors.toList());

        // Also add roles themselves as authorities
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));

        return new UserDetailsImpl(
                user.getId(),
                user.getCompanyId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPasswordResetRequired() != null ? user.getPasswordResetRequired() : false,
                authorities);
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPasswordResetRequired() {
        return passwordResetRequired;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o)
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
