package com.goat.HireHub.user;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.dto.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
public class UserService {
     private final UserRepository userRepository;
     private final PasswordEncoder passwordEncoder;
     private final SecurityUtils securityUtils;

     public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        SecurityUtils securityUtils){
          this.userRepository=userRepository;
          this.passwordEncoder=passwordEncoder;
          this.securityUtils=securityUtils;

     }

     //users
    @Cacheable(value="users",key="{#pageable.pageNumber,#pageable.pageSize,#pageable.sort}")
     public Slice<UserResponse> getAllUsers(Pageable pageable){
          return userRepository.findAllUsers(pageable);
     }

     @Cacheable(value="user",key="{#userId}")
     public UserResponse getUserById(Long userId){
            return getById(userId);
     }

     @Cacheable(value="user",key="{#firstName?.trim()?.toLowerCase()," +
             "#lastName?.trim()?.toLowerCase()," +
             "#username,#pageable.pageNumber,#pageable.pageSize,#pageable.sort}")
     public Slice<UserResponse> searchUsers(String firstName,String lastName,String username,Pageable pageable){
          Specification<User> spec=UserSpecification.withFilters(firstName,lastName,username);

          return userRepository.findBy(spec,
                  q->q
                          .as(UserResponse.class)
                          .slice(pageable)

          );
     }

     @Transactional
     @CacheEvict(value="user",key ="#currentUserId")
     public void deleteUserById(Long currentUserId){
           userRepository.deleteUserById(currentUserId);
     }
     @Transactional
     public UserResponse updateUser(UserUpdateRequest userRequest){
         User savedUser= userRepository.findById(1L).orElse(null);
          String email=userRequest.email().toLowerCase().trim();
          if (!savedUser.getEmail().equals(email) && findByEmail(email)){
               throw new ConflictException("This email address is already taken by another account");
          }
          savedUser.setLastName(userRequest.lastName());
          savedUser.setFirstName(userRequest.firstName());
          savedUser.setUsername(userRequest.username());
          savedUser.setEmail(email);

          return UserResponse.from(savedUser);

     }

     @Transactional
     public UserResponse patchUser(UserPatchRequest userRequest){
          User savedUser=userRepository.findById(1L).orElse(null);
          if (userRequest.email()!=null){
               String email=userRequest.email().toLowerCase().trim();
               if (!savedUser.getEmail().equals(email) && findByEmail(email)){
                    throw new ConflictException("This email address is already taken by another account");
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

          if(findByEmail(email)){
                throw new ConflictException("An account with this email already exists");
          }
          User user=new User();
          user.setEmail(email);
          user.setFirstName(userRequest.firstName());
          user.setLastName(userRequest.lastName());
          user.setUsername(userRequest.username());
          user.setPasswordHash(passwordEncoder.encode(userRequest.password()));
          return  UserResponse.from(userRepository.save(user));
     }

     private Boolean findByEmail(String email){
            return userRepository.existsByEmailIgnoreCase(email);
     }

     private UserResponse getById(Long id){
           return userRepository.getUserById(id)
                   .orElseThrow(()->new ResourceNotFoundException("user",id));
     }

     private UserResponse getCurrentUser(){
          return getById(securityUtils.getCurrentUser().id());
     }

}
