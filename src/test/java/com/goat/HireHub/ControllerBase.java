package com.goat.HireHub;

import com.goat.HireHub.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class ControllerBase {
    @MockitoBean
    protected JwtService jwtService;
    @MockitoBean
    protected UserDetailsService userDetailsService;

}
