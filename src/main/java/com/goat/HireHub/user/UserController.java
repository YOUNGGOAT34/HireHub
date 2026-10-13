package com.goat.HireHub.user;


import com.goat.HireHub.user.dto.UserPatchRequest;
import com.goat.HireHub.user.dto.UserRequest;
import com.goat.HireHub.user.dto.UserResponse;
import com.goat.HireHub.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
      private final UserService userService;

      public UserController(UserService userService){
           this.userService=userService;
      }

      @GetMapping
      public ResponseEntity<List<UserResponse>> getUsers(){
          return ResponseEntity.ok(userService.getAllUsers());
      }

      @GetMapping ("/{id}")
      public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
           return ResponseEntity.ok(userService.getUserById(id));
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<Void> deleteById(@PathVariable Long id){
          userService.deleteById(id);
          return ResponseEntity.noContent().build();
      }

      @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest){
           return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRequest));
      }

     @PutMapping("/{id}")
    public   ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest userUpdateRequest){
           return ResponseEntity.ok(userService.updateUser(id,userUpdateRequest));
     }
     @PatchMapping("/{id}")
     public   ResponseEntity<UserResponse> patchUser(@PathVariable Long id, @RequestBody @Valid UserPatchRequest userPatchRequest){
         return ResponseEntity.ok(userService.patchUser(id,userPatchRequest));
     }

}
