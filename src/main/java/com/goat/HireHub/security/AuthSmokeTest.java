package com.goat.HireHub.security;

import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthSmokeTest implements CommandLineRunner {
     private final AuthenticationManager authenticationManager;
     private final PasswordEncoder passwordEncoder;
     private final UserRepository userRepository;

     public AuthSmokeTest(AuthenticationManager authManager,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository
                          ){
         this.authenticationManager=authManager;
         this.passwordEncoder=passwordEncoder;
         this.userRepository=userRepository;

     }

     @Override
    public void run(String... args){

//         User user=new User();
//         user.setEmail("goatee@example.com");
//         user.setPasswordHash(passwordEncoder.encode("goat@01"));
//         user.setFirstName("goat");
//         user.setLastName("goat");
//         userRepository.save(user);

          String email="goatee@example.com";
          String rawPassword="goat@01";


          Authentication before= UsernamePasswordAuthenticationToken.unauthenticated(email,rawPassword);
          System.out.println("Before principal: "+before.getPrincipal()
                  +"("+before.getPrincipal().getClass().getSimpleName()+")"
          );

          System.out.println("Before credentials : "+before.getCredentials());
          System.out.println("Before authorities : "+before.getAuthorities());
          System.out.println("Before authenticated: "+before.isAuthenticated());

          Authentication after=authenticationManager.authenticate(before);

         System.out.println("After principal: "+after.getPrincipal()
                 +"("+before.getPrincipal().getClass().getSimpleName()+")"
         );
         System.out.println("After credentials : "+after.getCredentials());
         System.out.println("After authorities : "+after.getAuthorities());
         System.out.println("After authenticated "+after.isAuthenticated());

         try{
             authenticationManager.authenticate(
                      UsernamePasswordAuthenticationToken.unauthenticated(email,
                              "wrong password")
             );
         }catch (AuthenticationException e){
                System.out.println("WRONG PASSWORD->"+e.getClass().getSimpleName());
         }

         try{
             authenticationManager.authenticate(
                      UsernamePasswordAuthenticationToken.unauthenticated("nobody@email.com","x")
             );
         }catch (AuthenticationException e){
              System.out.println("unknown email->"+e.getClass().getSimpleName());
         }

     }
}
