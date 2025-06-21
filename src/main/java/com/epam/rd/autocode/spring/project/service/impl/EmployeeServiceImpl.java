package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.SortMappingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final SortMappingService sortMappingService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper mapper, OrderRepository orderRepository, PasswordEncoder passwordEncoder, SortMappingService sortMappingService) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.sortMappingService = sortMappingService;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::toDto).toList();
    }

    @Override
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        Pageable mappedPageable = sortMappingService.applyMappings(pageable, "employee");
        return employeeRepository.findAll(pageable).map(employeeMapper::toDto);
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        return employeeRepository.getByEmail(email).map(employeeMapper::toDto)
                .orElseThrow(()-> new NotFoundException("Employee with email " + email));
    }

    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employee) {
        return employeeRepository.getByEmail(email)
                .map(existingEmployee -> {
                    Employee updatedEmployee = employeeMapper.toEntity(employee);
                    updatedEmployee.setId(existingEmployee.getId());
                    return employeeMapper.toDto(employeeRepository.save(updatedEmployee));
                })
                .orElseThrow(() -> new NotFoundException("Employee with email " + email));
    }

    @Override
    public void updateEmployeeByEmail(String email, EmployeeUpdateDTO employee) {
        employeeRepository.getByEmail(email)
                .ifPresentOrElse(
                        existingEmployee -> {
                            Employee updatedEmployee = employeeMapper.toEntity(employee);
                            updatedEmployee.setId(existingEmployee.getId());
                            updatedEmployee.setPassword(existingEmployee.getPassword());
                            updatedEmployee.setEmail(existingEmployee.getEmail());
                            employeeRepository.save(updatedEmployee);
                        },
                        () -> {
                            throw new NotFoundException("Employee with email " + email + " not found");
                        }
                );
    }



    @Override
    public void deleteEmployeeByEmail(String email) {
        employeeRepository.deleteByEmail(email);
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeDTO dto) {
        try{
            Employee employee = employeeMapper.toEntity(dto);
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            return employeeMapper.toDto(employeeRepository.save(employee));
        }catch (DataIntegrityViolationException e){
            throw new AlreadyExistException("Employee with email " + dto.getEmail());
        }
    }

    @Override
    public void updateEmployeePassword(String email, String newPassword) {
        Employee employee = employeeRepository.getByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee with email " + email));

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }
}
