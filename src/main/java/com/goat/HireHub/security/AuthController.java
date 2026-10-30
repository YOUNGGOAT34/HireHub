
package com.goat.HireHub.security;

import com.goat.HireHub.user.UserService;
import com.goat.HireHub.user.dto.UserRequest;
import com.goat.HireHub.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirements
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService=jwtService;
        this.userService=userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        String token = jwtService.generateToken(
                request.email()
        );

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds()
        );

        return ResponseEntity.ok(
                response
        );
    }
}