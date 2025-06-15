package com.epam.rd.autocode.spring.project.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface MyUserDetailsService extends UserDetailsService {
    UserDetails loadEmployeeByUsername(String email) throws UsernameNotFoundException;

    UserDetails loadUserBasedOnRole(String email, String role);
}
