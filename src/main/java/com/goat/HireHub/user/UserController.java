package com.goat.HireHub.user;


import com.goat.HireHub.user.dto.UserPatchRequest;
import com.goat.HireHub.user.dto.UserRequest;
import com.goat.HireHub.user.dto.UserResponse;
import com.goat.HireHub.user.dto.UserUpdateRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
      private final UserService userService;

      public UserController(UserService userService){
           this.userService=userService;
      }

      @GetMapping
      public List<UserResponse> getUsers(){
          return userService.getAllUsers();
      }

      @GetMapping ("/{id}")
      public UserResponse getUserById(@PathVariable Long id){
           return userService.getUserById(id);
      }

      @DeleteMapping("/{id}")
      public void deleteById(@PathVariable Long id){
          userService.deleteById(id);
      }

      @PostMapping
    public UserResponse registerUser(@RequestBody UserRequest userRequest){
           return userService.registerUser(userRequest);
      }

     @PutMapping("/{id}")
    public   UserResponse updateUser(@PathVariable Long id, @RequestParam UserUpdateRequest userUpdateRequest){
           return userService.updateUser(id,userUpdateRequest);
     }
     @PatchMapping("/{id}")
     public   UserResponse patchUser(@PathVariable Long id, @RequestParam UserPatchRequest userPatchRequest){
         return userService.patchUser(id,userPatchRequest);
     }

}
