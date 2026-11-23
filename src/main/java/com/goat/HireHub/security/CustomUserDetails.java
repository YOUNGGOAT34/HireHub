package com.goat.HireHub.security;

import com.goat.HireHub.user.User;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
//    private final AuthenticatedUser user;
//    public CustomUserDetails(AuthenticatedUser user){
//        this.user=user;
//    }
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of();
//    }
//
//    @Override
//    public @Nullable String getPassword() {
//        return user.passwordHash();
//    }
//
//    @Override
//    public String getUsername() {
//        return user.email();
//    }
//
//
//    public AuthenticatedUser getUser(){
//        return this.user;
//    }


    private final AuthenticatedUser user;
    private final boolean enabled;
    public CustomUserDetails(AuthenticatedUser user){
         this.user=user;
         this.enabled=true;
    }

    public AuthenticatedUser getUser(){
        return this.user;
    }
    @Override
    public @NotNull String getUsername(){
         return this.user.email();
    }

    @Override
    public String getPassword(){
         return this.user.passwordHash();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
         return List.of();
    }
    //flags
    @Override
    public boolean isEnabled(){
         return enabled;
    }

    @Override
    public boolean isAccountNonLocked(){
         return true;
    }

    @Override
    public boolean isAccountNonExpired(){
         return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
         return true;
    }

    //getters and setters for extra data
    public Long getId(){
        return user.id();
    }

}
