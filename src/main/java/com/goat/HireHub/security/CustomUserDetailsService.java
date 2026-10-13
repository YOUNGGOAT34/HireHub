package com.goat.HireHub.security;

import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
         this.userRepository=userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
          User user=userRepository.findByEmailIgnoreCase(email)
                  .orElseThrow(()->new UsernameNotFoundException("user not found"));
          return org.springframework.security.core.userdetails.User
                  .withUsername(user.getEmail())
                  .password(user.getPassword())
                  .roles(user.getRole().name())
                  .build();
    }
}
