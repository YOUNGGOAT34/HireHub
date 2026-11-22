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
     @Transactional(readOnly = true)
    @Cacheable(value="users",key="{#pageable.pageNumber,#pageable.pageSize,#pageable.sort}")
     public Slice<UserResponse> getAllUsers(Pageable pageable){
          return userRepository.findAllUsers(pageable);
     }
    @Transactional(readOnly = true)
     @Cacheable(value="user",key="{#userId}")
     public UserResponse getUserById(Long userId){
            return getById(userId);
     }
    @Transactional(readOnly = true)
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
                      userRequest.email().trim().toLowerCase(),
                      userRequest.username().trim().toLowerCase());

          }catch(DataIntegrityViolationException e){
              throwDataIntegrityViolationException(e);
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
                       userRequest.email()==null?null:userRequest.email().trim().toLowerCase(),
                       userRequest.username()==null?null:userRequest.username().trim().toLowerCase()
               );
          }catch (DataIntegrityViolationException e){
               throwDataIntegrityViolationException(e);
          }

     }

     @Transactional
     public void registerUser(UserRequest userRequest){

          try{
              userRepository.createAccount(userRequest.email().trim().toLowerCase(),
                                           userRequest.firstName().trim(),
                                          userRequest.lastName().trim(),
                                          userRequest.username().trim().toLowerCase(),
                                          passwordEncoder.encode(userRequest.password())
                      );

          }catch(DataIntegrityViolationException e){
               throwDataIntegrityViolationException(e);
         }

     }

     private UserResponse getById(Long id){
           return userRepository.getUserById(id)
                   .orElseThrow(()->new ResourceNotFoundException("user",id));
     }

     private UserResponse getCurrentUser(){
          return getById(securityUtils.getCurrentUser().id());
     }

     private void throwDataIntegrityViolationException(DataIntegrityViolationException e){
         if(e.getCause() instanceof ConstraintViolationException cve){
             String constraint=cve.getConstraintName();

             if(constraint!=null){
                 if(constraint.trim().contains("email_constraint")){
                     throw new ConflictException("email already in use");
                 }

                 if(constraint.trim().contains("username_constraint")){
                     throw new ConflictException("username already in use");
                 }
             }

             throw new ConflictException("email or username already in use");
         }

         throw e;
     }

}
