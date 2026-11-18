package com.goat.HireHub.security;
import com.goat.HireHub.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class SecurityUtils {

    public AuthenticatedUser getCurrentUser() {

        Authentication auth=SecurityContextHolder.getContext().getAuthentication();

        if(auth!=null && auth.getPrincipal() instanceof CustomUserDetails userDetails){
             return userDetails.getUser();
        }
       throw new AccessDeniedException("No authenticated user found in session");
    }
}