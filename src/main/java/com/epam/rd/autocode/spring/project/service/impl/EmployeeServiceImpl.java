package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
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

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper mapper, OrderRepository orderRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::toDto).toList();
    }

    @Override
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
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
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeUpdateDTO employee) {
        return employeeRepository.getByEmail(email)
                .map(existingEmployee -> {
                    Employee updatedEmployee = employeeMapper.toEntity(employee);
                    updatedEmployee.setId(existingEmployee.getId());
                    updatedEmployee.setPassword(existingEmployee.getPassword());
                    updatedEmployee.setEmail(existingEmployee.getEmail());
                    return employeeMapper.toDto(employeeRepository.save(updatedEmployee));
                })
                .orElseThrow(() -> new NotFoundException("Employee with email " + email));
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
}
