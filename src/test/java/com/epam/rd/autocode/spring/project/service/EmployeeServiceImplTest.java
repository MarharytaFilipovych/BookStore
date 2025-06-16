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
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());
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
        verify(employeeRepository).findAll(pageable);
        verify(employeeMapper, times(employees.size())).toDto(any(Employee.class));
    }

    @Test
    void getAllEmployees_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order nameOrder = capturedPageable.getSort().getOrderFor("name");
        assertNotNull(nameOrder);
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());
        assertEquals("name", nameOrder.getProperty());
    }

    @Test
    void getAllEmployees_WithSortByBirthDateDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("birthdate").descending());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order order = capturedPageable.getSort().getOrderFor("birthdate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
        assertEquals("birthdate", order.getProperty());
    }

    @Test
    void getAllEmployees_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange
        Sort multiSort = Sort.by("name").ascending().and(Sort.by("birthdate").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);
        for (int i = 0; i < employees.size(); i++) {
            when(employeeMapper.toDto(employees.get(i))).thenReturn(employeeDTOs.get(i));
        }

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort capturedSort = capturedPageable.getSort();

        List<Sort.Order> orders = capturedSort.toList();
        assertEquals(2, orders.size());

        Sort.Order nameOrder = orders.get(0);
        assertEquals("name", nameOrder.getProperty());
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());

        Sort.Order birthdateOrder = orders.get(1);
        assertEquals("birthdate", birthdateOrder.getProperty());
        assertEquals(Sort.Direction.DESC, birthdateOrder.getDirection());
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
        Page<Employee> emptyPage = new PageImpl<>(List.of(), pageable, 0);
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
    void updateEmployeeByEmailWithEmployeeUpdateDTO_WhenEmployeeExists_ShouldReturnUpdatedEmployeeDTO() {
        // Arrange
        EmployeeUpdateDTO updateData = new EmployeeUpdateDTO();
        updateData.setName("Updated Name");
        updateData.setPhone("+1-555-9999");
        updateData.setBirthDate(LocalDate.of(1990, 1, 1));

        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Updated Name");
        updatedEmployee.setPhone("+1-555-9999");
        updatedEmployee.setBirthDate(LocalDate.of(1990, 1, 1));

        EmployeeDTO expectedResult = new EmployeeDTO();
        expectedResult.setEmail(employee.getEmail());
        expectedResult.setName("Updated Name");
        expectedResult.setPhone("+1-555-9999");
        expectedResult.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(employeeMapper.toEntity(updateData)).thenReturn(updatedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            assertEquals(employee.getId(), saved.getId());
            assertEquals(employee.getPassword(), saved.getPassword());
            assertEquals(employee.getEmail(), saved.getEmail());
            return saved;
        });
        when(employeeMapper.toDto(updatedEmployee)).thenReturn(expectedResult);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(employeeRepository).getByEmail(employee.getEmail());
        verify(employeeMapper).toEntity(updateData);
        verify(employeeRepository).save(argThat(emp ->
                emp.getId().equals(employee.getId()) &&
                        emp.getPassword().equals(employee.getPassword()) &&
                        emp.getEmail().equals(employee.getEmail())
        ));
        verify(employeeMapper).toDto(updatedEmployee);
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
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(employeeDTO);

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
        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.addEmployee(employeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(employeeDTO, result);
        verify(employeeMapper).toEntity(employeeDTO);
        verify(employeeRepository).save(employee);
        verify(employeeMapper).toDto(employee);
    }

    @Test
    void addEmployee_WhenEmployeeAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        AlreadyExistException exception = assertThrows(AlreadyExistException.class,
                () -> employeeService.addEmployee(employeeDTO));

        assertTrue(exception.getMessage().contains("Employee with email " + employeeDTO.getEmail()));
        verify(employeeMapper).toEntity(employeeDTO);
        verify(employeeRepository).save(employee);
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
}