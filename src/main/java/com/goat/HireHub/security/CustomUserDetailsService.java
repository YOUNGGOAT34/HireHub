package com.goat.HireHub.security;

import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

//    private final UserRepository userRepository;
//
//    public CustomUserDetailsService(UserRepository userRepository){
//         this.userRepository=userRepository;
//    }
//    @Override
//    @Cacheable(value="authUsers",key="#email.trim().toLowerCase()")
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        AuthenticatedUser user = userRepository.findAuthenticatedUserByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
//        return new CustomUserDetails(user);
//    }

    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository repo){
        this.userRepository=repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        AuthenticatedUser user=userRepository.findAuthenticatedUserByEmail(email.trim().toLowerCase())
                .orElseThrow(()-> new UsernameNotFoundException("username not found"));
        return new CustomUserDetails(user);
    }
}
