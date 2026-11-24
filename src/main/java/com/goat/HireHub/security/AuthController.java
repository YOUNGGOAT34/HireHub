
package com.goat.HireHub.security;

import com.goat.HireHub.security.dto.LoginRequest;
import com.goat.HireHub.security.dto.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirements
@RequestMapping("/auth")
public class AuthController {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtService jwtService;
//    private final UserService userService;
//
//    public AuthController(AuthenticationManager authenticationManager,JwtService jwtService, UserService userService) {
//        this.authenticationManager = authenticationManager;
//        this.jwtService=jwtService;
//        this.userService=userService;
//    }
//
//
//    @PostMapping("/register")
//    public ResponseEntity<Void> registerUser(@RequestBody @Valid UserRequest userRequest){
//        userService.registerUser(userRequest);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(
//            @Valid @RequestBody LoginRequest request) {
//
//        Authentication authentication =
//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                request.email(),
//                                request.password()
//                        )
//                );
//
//        String token = jwtService.generateToken(
//                request.email()
//        );
//
//        LoginResponse response = new LoginResponse(
//                token,
//                "Bearer",
//                jwtService.getExpirationSeconds()
//        );
//
//        return ResponseEntity.ok(
//                response
//        );
//    }

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,JwtService jwtService){
        this.jwtService=jwtService;
        this.authenticationManager=authenticationManager;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
         String email=request.email().trim().toLowerCase();

         Authentication authentication=authenticationManager.authenticate(
                  UsernamePasswordAuthenticationToken.unauthenticated(email,request.password())
         );

         String token=jwtService.generateToken(authentication.getName());

         return new LoginResponse(token,"Bearer",jwtService.getExpirationSeconds());
    }
}