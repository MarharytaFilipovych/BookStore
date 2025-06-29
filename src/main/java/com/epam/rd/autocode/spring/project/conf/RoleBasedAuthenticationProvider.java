package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.service.MyUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RoleBasedAuthenticationProvider implements AuthenticationProvider {
    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public RoleBasedAuthenticationProvider
            (MyUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();
        if(authentication.getDetails() instanceof Map<?, ?> details &&  details.get("role") instanceof Role role){
            UserDetails userDetails = userDetailsService.loadUserBasedOnRole(email, role);
            if(!passwordEncoder.matches(password, userDetails.getPassword())){
                throw new BadCredentialsException("Invalid credentials!");
            }
            return new UsernamePasswordAuthenticationToken
                    (userDetails, null, userDetails.getAuthorities());
        }
        throw new BadCredentialsException("Invalid authentication details: role required");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
