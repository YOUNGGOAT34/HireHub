package com.goat.HireHub.user;
import com.goat.HireHub.exception.BadRequestException;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.dto.*;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
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
     @Caching(
             evict = {
                     @CacheEvict(value="user",key="#userId"),
                     @CacheEvict(value="authUsers",key="#email.trim().toLowerCase()")
             }
     )
     public void deleteUserById(Long currentUserId){
           userRepository.deleteUserById(currentUserId);
     }
     @Transactional

     @Caching(
             evict = {
                     @CacheEvict(value="user",key="#userId"),
                     @CacheEvict(value="authUsers",key="#email.trim().toLowerCase()")
             }
     )
     public void updateUser(Long userId,String email,UserUpdateRequest userRequest){
          try{
              userRepository.updateUser(userId,
                      userRequest.firstName(),
                      userRequest.lastName(),
                      userRequest.email(),
                      userRequest.username());

          }catch(DataIntegrityViolationException e){
              if(e.getCause() instanceof ConstraintViolationException cve){
                   String constraintName=cve.getConstraintName();

                   if (constraintName !=null){
                        if(constraintName.toLowerCase().contains("email_constraint")){
                            throw new ConflictException("email is already in use");
                        }
                        if(constraintName.toLowerCase().contains("username_constraint")){
                             throw new ConflictException("username already in use");
                        }
                   }
              }
              throw new ConflictException("email or username already in use");
          }

     }

     @Transactional
     @Caching(
           evict = {
                   @CacheEvict(value="user",key = "#userId"),
                   @CacheEvict(value="authUsers",key="#email.trim().toLowerCase()")
           }
     )
     public void patchUser(Long userId,String email,UserPatchRequest userRequest){
         if(userRequest.email()==null
                 && userRequest.lastName()==null
                 && userRequest.firstName()==null
         && userRequest.username()==null){
             throw new BadRequestException("empty patch body");
         }

          try{
               userRepository.patchUser(
                        userId,
                       userRequest.firstName(),
                       userRequest.lastName(),
                       userRequest.email(),
                       userRequest.username()
               );
          }catch (DataIntegrityViolationException e){
                if(e.getCause() instanceof ConstraintViolationException cve){
                    String constraint=cve.getConstraintName();

                    if(constraint!=null){
                          if(constraint.toLowerCase().contains("email_constraint")){
                               throw new ConflictException("email already in use");
                          }

                          if(constraint.toLowerCase().contains("username_constraint")){
                               throw new ConflictException("username already in use");
                          }
                    }

                }

                throw new ConflictException("email or username already in use");
          }


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
