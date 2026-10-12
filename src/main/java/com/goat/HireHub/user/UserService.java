package com.goat.HireHub.user;

import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.user.dto.UserPatchRequest;
import com.goat.HireHub.user.dto.UserRequest;
import com.goat.HireHub.user.dto.UserResponse;
import com.goat.HireHub.user.dto.UserUpdateRequest;
import jakarta.transaction.Transactional;
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

     public UserResponse getUserById(Long id){
            User user=getById(id);
            return UserResponse.from(user);
     }

     public void deleteById(Long id){
           if(!userRepository.existsById(id)){
                 throw new ResourceNotFoundException("user not found",id);
           }
           userRepository.deleteById(id);
     }

     @Transactional
     public UserResponse updateUser(Long id, UserUpdateRequest userRequest){
          User savedUser=getById(id);
          String email=userRequest.email().toLowerCase().trim();
          if (!savedUser.getEmail().equals(email) && findByEmail(email)){
               throw new DataIntegrityViolationException("This email address is already taken by another account");
          }
          savedUser.setLastName(userRequest.lastName());
          savedUser.setFirstName(userRequest.firstName());
          savedUser.setUsername(userRequest.username());
          savedUser.setEmail(email);

          return UserResponse.from(savedUser);

     }


     @Transactional
     public UserResponse patchUser(Long id, UserPatchRequest userRequest){
          User savedUser=getById(id);

          if (userRequest.email()!=null){
               String email=userRequest.email().toLowerCase().trim();
               if (!savedUser.getEmail().equals(email) && findByEmail(email)){
                    throw new DataIntegrityViolationException("This email address is already taken by another account");
               }

               savedUser.setEmail(email);
          }

          if(userRequest.lastName()!=null){
               savedUser.setLastName(userRequest.lastName());
          }

          if (userRequest.firstName()!=null){
               savedUser.setFirstName(userRequest.firstName());
          }

          if(userRequest.username()!=null){
               savedUser.setUsername(userRequest.username());

          }

          if (userRequest.password() != null && !userRequest.password().isBlank()) {
               String encodedPassword = passwordEncoder.encode(userRequest.password());
               savedUser.setPasswordHash(encodedPassword);
          }

          return UserResponse.from(savedUser);
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

     private User getById(Long id){
           return userRepository.findById(id)
                   .orElseThrow(()->new ResourceNotFoundException("user not found",id));
     }


}
