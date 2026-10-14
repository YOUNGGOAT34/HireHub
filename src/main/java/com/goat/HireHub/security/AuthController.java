
package com.goat.HireHub.security;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService=jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        String token = jwtService.generateToken(
                authentication.getName()
        );

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds()
        );

        return ResponseEntity.ok(
                "Authenticated successfully: " +
                        authentication.getName()
        );
    }
}