package com.goat.HireHub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean // Registers BCryptPasswordEncoder in Spring's application context
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF so Postman can send POST, PUT, and DELETE requests
                .csrf(csrf -> csrf.disable())

                // 2. Configure which endpoints are public or private
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to all user endpoints
                        .requestMatchers("/users/**").permitAll()

                        // Allow public access to your other endpoints (seen in your Postman history)
                        .requestMatchers("/companies/**").permitAll()
                        .requestMatchers("/jobs/**").permitAll()

                        // Any other request not specified above will require logging in
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}