package com.crapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;


/**
 * @author Traceabel AI
 */

public class UserPrinciple implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String email;

    @JsonIgnore
    private String password;

    private  GrantedAuthority authorities;

    public UserPrinciple(Long id,
                         String email, String password,
                         GrantedAuthority authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrinciple build(User user) {
        GrantedAuthority authorities =  new SimpleGrantedAuthority(user.getRole().toString());


        return new UserPrinciple(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Arrays.asList(authorities);
    }

    @Override
    public String getPassword() {
        return password;
    }

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
        return true;
    }
}
