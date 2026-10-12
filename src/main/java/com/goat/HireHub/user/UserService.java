package com.goat.HireHub.user;

import com.goat.HireHub.user.dto.UserRequest;
import com.goat.HireHub.user.dto.UserResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
     private final UserRepository userRepository;
     private final PasswordEncoder passwordEncoder;

     public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
          this.userRepository=userRepository;
          this.passwordEncoder=passwordEncoder;
     }

     public List<UserResponse> getAllUsers(){
          return userRepository.findAll().stream().map(UserResponse::from).toList();
     }

     public UserResponse registerUser(UserRequest userRequest){
          String email= userRequest.email().toLowerCase().trim();
          String role=userRequest.role().toUpperCase();
          Set<String> roles=Set.of("EMPLOYEE","USER");

          if (!roles.contains(role)){
               throw new IllegalArgumentException("Unknown role "+role);
          }

          if(findByEmail(email)){
                throw new DataIntegrityViolationException("An account with this email already exists");
          }
          User user=new User();
          user.setEmail(email);
          user.setFirstName(userRequest.firstName());
          user.setLastName(userRequest.lastName());
          user.setRole(Role.valueOf(role));
          user.setPasswordHash(passwordEncoder.encode(userRequest.password()));
          return  UserResponse.from(userRepository.save(user));
     }

     private Boolean findByEmail(String email){
            return userRepository.existsByEmailIgnoreCase(email);
     }
}
