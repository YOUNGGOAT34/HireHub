package com.goat.HireHub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//    }
//
//    @Bean // Registers BCryptPasswordEncoder in Spring's application context
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity http,
//            AuthenticationProvider authenticationProvider,
//            RestAuthenticationEntryPoint entryPoint,
//            RestAccessDeniedHandler accessDeniedHandler)
//            throws Exception {
//
//        http
//
//                .authenticationProvider(authenticationProvider)
//                .exceptionHandling(ex->
//                        ex.authenticationEntryPoint(entryPoint)
//                                .accessDeniedHandler(accessDeniedHandler)
//                )
//
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,PasswordEncoder passwordEncoder){
//        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder);
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
//         return configuration.getAuthenticationManager();
//    }


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    public SecurityConfig(JwtAuthenticationFilter authenticationFilter){
        this.jwtAuthenticationFilter=authenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RestAuthenticationEntryPoint entryPoint,
                                                   RestAccessDeniedHandler accessDenied
                                                   ){
         http
                 .csrf(csrf->csrf.disable())

                 .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .exceptionHandling(ex->
                         ex.authenticationEntryPoint(entryPoint)
                                 .accessDeniedHandler(accessDenied)
                         )
                 .authorizeHttpRequests(
                           auth-> auth
//                                   .requestMatchers("/error").permitAll()
                                   .requestMatchers("/auth/**").permitAll()
//                                   .requestMatchers(HttpMethod.GET,"/jobs/**","/companies/**").permitAll()
//                                   .requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
                                   .anyRequest().authenticated())
                 .addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);

                 return http.build();
    }
   @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}