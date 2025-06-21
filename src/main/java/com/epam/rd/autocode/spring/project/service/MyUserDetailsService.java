package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface MyUserDetailsService extends UserDetailsService {
    UserDetails loadEmployeeByUsername(String email) throws UsernameNotFoundException;

    UserDetails loadUserBasedOnRole(String email, Role role);
}
