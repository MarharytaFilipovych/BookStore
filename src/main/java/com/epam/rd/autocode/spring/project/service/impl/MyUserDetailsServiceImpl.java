package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.MyUserDetailsService;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MyUserDetailsServiceImpl implements MyUserDetailsService {
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final BlockedClientRepository blockedClientRepository;

    public MyUserDetailsServiceImpl
            (EmployeeRepository employeeRepository, ClientRepository clientRepository,
             BlockedClientRepository blockedClientRepository) {
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
        this.blockedClientRepository = blockedClientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Client client = clientRepository.getByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Client with email " + email + " was not found!"));
        if(blockedClientRepository.existsByClient_Email(email)){
            throw new LockedException("Account is blocked!");
        }
        return client;
    }

    public UserDetails loadEmployeeByUsername(String email)throws UsernameNotFoundException{
        return employeeRepository.getByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Employee with email " + email + " was not found!"));
    }

    public UserDetails loadUserBasedOnRole(String email, Role role) {
        return role == Role.EMPLOYEE
                ? loadEmployeeByUsername(email)
                : loadUserByUsername(email);
    }
}
