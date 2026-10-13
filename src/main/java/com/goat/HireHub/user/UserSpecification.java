package com.goat.HireHub.user;

import com.goat.HireHub.user.dto.UserResponse;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
     public static Specification<User> firstNameMatches(String firstName){
          return (root,query,criteriaBuilder)->{
               if(firstName==null || firstName.isBlank()) return null;

               return criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),"%"+firstName.toLowerCase()+"%");
          };
     }

    public static Specification<User> lastNameMatches(String lastName){
        return (root,query,criteriaBuilder)->{
            if(lastName==null || lastName.isBlank()) return null;

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),"%"+lastName.toLowerCase()+"%");
        };
    }

    public static Specification<User> usernameMatches(String username){
        return (root,query,criteriaBuilder)->{
            if(username==null || username.isBlank()) return null;

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),"%"+username.toLowerCase()+"%");
        };

    }

    public static Specification<User> roleEqual(String role){
        return (root,query,criteriaBuilder)->{
            if(role==null || role.isBlank()) return null;

            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("role")),role.toLowerCase());
        };
    }
}
