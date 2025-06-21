package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SortMappingService sortMappingService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private List<Employee> employees;
    private List<EmployeeDTO> employeeDTOs;
    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp(){
        employee = getEmployeeEntity();
        employeeDTO = getEmployeeDTO();
        employees = getEmployeeEntities();
        employeeDTOs = getEmployeeDTOs();
    }

    @Test
    void getAllEmployees_WithPageable_ShouldReturnPageOfEmployeeDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Employee> employeePage = new PageImpl<>(employees, mappedPageable, employees.size());

        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(employees.size(), result.getTotalElements());
        assertEquals(employeeDTOs, result.getContent());
        verify(sortMappingService).applyMappings(pageable, "employee");
        verify(employeeRepository).findAll(pageable);
        verify(employeeMapper, times(employees.size())).toDto(any(Employee.class));
    }

    @Test
    void getAllEmployees_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Employee> employeePage = new PageImpl<>(employees, mappedPageable, employees.size());

        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "employee");
        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void getAllEmployees_WithSortByBirthDateDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("birthdate").descending());
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("birthDate").descending());
        Page<Employee> employeePage = new PageImpl<>(employees, mappedPageable, employees.size());

        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "employee");
        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void getAllEmployees_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange
        Sort multiSort = Sort.by("name").ascending().and(Sort.by("birthdate").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Pageable mappedPageable = PageRequest.of(0, 10, multiSort);
        Page<Employee> employeePage = new PageImpl<>(employees, mappedPageable, employees.size());

        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "employee");
        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void getAllEmployees_WithoutPageable_ShouldReturnListOfEmployeeDTOs() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(employees);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(employees.size(), result.size());
        assertEquals(employeeDTOs, result);
        verify(employeeRepository).findAll();
        verify(employeeMapper, times(employees.size())).toDto(any(Employee.class));
    }

    @Test
    void getAllEmployees_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(List.of());

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository).findAll();
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    void getAllEmployees_WithPageable_WhenEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Employee> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);

        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository).findAll(pageable);
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    void updateEmployeeByEmailWithEmployeeDTO_WhenEmployeeExists_ShouldReturnUpdatedEmployeeDTO() {
        // Arrange
        EmployeeDTO updateData = getEmployeeDTO();
        updateData.setName("New Name");

        Employee updatedEmployee = getEmployeeEntity();
        updatedEmployee.setName("New Name");

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(employeeMapper.toEntity(updateData)).thenReturn(updatedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            assertEquals(employee.getId(), saved.getId());
            return saved;
        });
        when(employeeMapper.toDto(updatedEmployee)).thenReturn(updateData);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(updateData, result);
        verify(employeeRepository).getByEmail(employee.getEmail());
        verify(employeeMapper).toEntity(updateData);
        verify(employeeRepository).save(argThat(emp -> emp.getId().equals(employee.getId())));
        verify(employeeMapper).toDto(updatedEmployee);
    }

    @Test
    void updateEmployeeByEmailWithEmployeeDTO_WhenEmployeeDoesNotExist_ShouldThrowNotFoundException(){
        // Arrange
        String employeeEmail = "nonexistent@example.com";
        when(employeeRepository.getByEmail(employeeEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> employeeService.updateEmployeeByEmail(employeeEmail, employeeDTO));

        assertTrue(exception.getMessage().contains("Employee with email " + employeeEmail));
        verify(employeeRepository).getByEmail(employeeEmail);
        verify(employeeMapper, never()).toEntity(any(EmployeeDTO.class));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployeeByEmailWithEmployeeUpdateDTO_WhenEmployeeExists_ShouldSaveUpdatedEmployee() {
        // Arrange
        EmployeeUpdateDTO updateData = new EmployeeUpdateDTO();
        updateData.setName("Updated Name");
        updateData.setPhone("+1-555-9999");
        updateData.setBirthDate(LocalDate.of(1990, 1, 1));

        Employee mappedEmployee = new Employee();
        mappedEmployee.setName("Updated Name");
        mappedEmployee.setPhone("+1-555-9999");
        mappedEmployee.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(employeeMapper.toEntity(updateData)).thenReturn(mappedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        verify(employeeRepository).getByEmail(employee.getEmail());
        verify(employeeMapper).toEntity(updateData);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());

        Employee savedEmployee = employeeCaptor.getValue();
        assertEquals(employee.getId(), savedEmployee.getId());
        assertEquals(employee.getPassword(), savedEmployee.getPassword());
        assertEquals(employee.getEmail(), savedEmployee.getEmail());
        assertEquals("Updated Name", savedEmployee.getName());
        assertEquals("+1-555-9999", savedEmployee.getPhone());
        assertEquals(LocalDate.of(1990, 1, 1), savedEmployee.getBirthDate());
    }

    @Test
    void updateEmployeeByEmailWithEmployeeUpdateDTO_WhenEmployeeDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String employeeEmail = "nonexistent@example.com";
        EmployeeUpdateDTO updateData = new EmployeeUpdateDTO();
        updateData.setName("Updated Name");
        updateData.setPhone("+1-555-9999");
        updateData.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.getByEmail(employeeEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> employeeService.updateEmployeeByEmail(employeeEmail, updateData));

        assertTrue(exception.getMessage().contains("Employee with email " + employeeEmail));
        verify(employeeRepository).getByEmail(employeeEmail);
        verify(employeeMapper, never()).toEntity(any(EmployeeUpdateDTO.class));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployeeByEmailWithEmployeeUpdateDTO_ShouldPreserveOriginalPasswordAndEmail() {
        // Arrange
        EmployeeUpdateDTO updateData = new EmployeeUpdateDTO();
        updateData.setName("New Name");
        updateData.setPhone("+1-555-0000");
        updateData.setBirthDate(LocalDate.of(1995, 5, 5));

        Employee mappedEmployee = new Employee();
        mappedEmployee.setName("New Name");
        mappedEmployee.setPhone("+1-555-0000");
        mappedEmployee.setBirthDate(LocalDate.of(1995, 5, 5));

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(employeeMapper.toEntity(updateData)).thenReturn(mappedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());

        Employee savedEmployee = employeeCaptor.getValue();
        assertEquals(employee.getId(), savedEmployee.getId());
        assertEquals(employee.getPassword(), savedEmployee.getPassword());
        assertEquals(employee.getEmail(), savedEmployee.getEmail());
        assertEquals("New Name", savedEmployee.getName());
        assertEquals("+1-555-0000", savedEmployee.getPhone());
        assertEquals(LocalDate.of(1995, 5, 5), savedEmployee.getBirthDate());
    }

    @Test
    void deleteEmployeeByEmail_ShouldCallRepositoryDelete() {
        // Arrange
        String employeeEmail = "test@example.com";

        // Act
        employeeService.deleteEmployeeByEmail(employeeEmail);

        // Assert
        verify(employeeRepository).deleteByEmail(employeeEmail);
    }

    @Test
    void addEmployee_WhenEmployeeIsValid_ShouldReturnSavedEmployeeDTO() {
        // Arrange
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPassword";

        Employee employeeToSave = getEmployeeEntity();
        employeeToSave.setPassword(rawPassword);

        Employee savedEmployee = getEmployeeEntity();
        savedEmployee.setPassword(encodedPassword);

        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employeeToSave);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(employeeRepository.save(employeeToSave)).thenReturn(savedEmployee);
        when(employeeMapper.toDto(savedEmployee)).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.addEmployee(employeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(employeeDTO, result);
        verify(employeeMapper).toEntity(employeeDTO);
        verify(passwordEncoder).encode(rawPassword);
        verify(employeeRepository).save(employeeToSave);
        verify(employeeMapper).toDto(savedEmployee);
        assertEquals(encodedPassword, employeeToSave.getPassword());
    }

    @Test
    void addEmployee_WhenEmployeeAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        Employee employeeToSave = getEmployeeEntity();
        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employeeToSave);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(employeeToSave)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        AlreadyExistException exception = assertThrows(AlreadyExistException.class,
                () -> employeeService.addEmployee(employeeDTO));

        assertTrue(exception.getMessage().contains("Employee with email " + employeeDTO.getEmail()));
        verify(employeeMapper).toEntity(employeeDTO);
        verify(passwordEncoder).encode(anyString());
        verify(employeeRepository).save(employeeToSave);
    }

    @Test
    void getEmployeeByEmail_WhenEmployeeExists_ShouldReturnEmployeeDTO() {
        // Arrange
        String employeeEmail = employee.getEmail();
        when(employeeRepository.getByEmail(employeeEmail)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.getEmployeeByEmail(employeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals(employeeDTO, result);
        verify(employeeRepository).getByEmail(employeeEmail);
        verify(employeeMapper).toDto(employee);
    }

    @Test
    void getEmployeeByEmail_WhenEmployeeDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String employeeEmail = "nonexistent@example.com";
        when(employeeRepository.getByEmail(employeeEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> employeeService.getEmployeeByEmail(employeeEmail));

        assertTrue(exception.getMessage().contains("Employee with email " + employeeEmail));
        verify(employeeRepository).getByEmail(employeeEmail);
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    void updateEmployeePassword_WhenEmployeeExists_ShouldUpdatePassword() {
        // Arrange
        String employeeEmail = employee.getEmail();
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newEncodedPassword";

        when(employeeRepository.getByEmail(employeeEmail)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(employeeRepository.save(employee)).thenReturn(employee);

        // Act
        employeeService.updateEmployeePassword(employeeEmail, newPassword);

        // Assert
        verify(employeeRepository).getByEmail(employeeEmail);
        verify(passwordEncoder).encode(newPassword);
        verify(employeeRepository).save(employee);
        assertEquals(encodedPassword, employee.getPassword());
    }

    @Test
    void updateEmployeePassword_WhenEmployeeDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String employeeEmail = "nonexistent@example.com";
        String newPassword = "newPassword123";

        when(employeeRepository.getByEmail(employeeEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> employeeService.updateEmployeePassword(employeeEmail, newPassword));

        assertTrue(exception.getMessage().contains("Employee with email " + employeeEmail));
        verify(employeeRepository).getByEmail(employeeEmail);
        verify(passwordEncoder, never()).encode(anyString());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void addEmployee_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "plainTextPassword";
        String encodedPassword = "$2a$10$encodedHashedPassword";

        EmployeeDTO dtoWithPlainPassword = getEmployeeDTO();
        dtoWithPlainPassword.setPassword(rawPassword);

        Employee employeeEntity = getEmployeeEntity();
        employeeEntity.setPassword(rawPassword);

        when(employeeMapper.toEntity(dtoWithPlainPassword)).thenReturn(employeeEntity);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            assertEquals(encodedPassword, saved.getPassword());
            return saved;
        });
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        employeeService.addEmployee(dtoWithPlainPassword);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
        verify(employeeRepository).save(argThat(emp -> encodedPassword.equals(emp.getPassword())));
    }

    @Test
    void getAllEmployees_WithPageable_ShouldUseMappedPageable() {
        // Arrange
        Pageable originalPageable = PageRequest.of(0, 10, Sort.by("birthdate"));
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("birthDate"));
        Page<Employee> employeePage = new PageImpl<>(employees, mappedPageable, employees.size());

        when(sortMappingService.applyMappings(originalPageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(originalPageable)).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(originalPageable);

        // Assert
        assertNotNull(result);
        verify(sortMappingService).applyMappings(originalPageable, "employee");
        verify(employeeRepository).findAll(originalPageable);
    }
}