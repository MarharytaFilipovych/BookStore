package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRefreshTokenRepository;
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

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeMapper employeeMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SortMappingService sortMappingService;
    @Mock private EmployeeRefreshTokenRepository employeeRefreshTokenRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private List<Employee> employees;
    private List<EmployeeDTO> employeeDTOs;
    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp() {
        employee = getEmployeeEntity();
        employeeDTO = getEmployeeDTO();
        employees = getEmployeeEntities();
        employeeDTOs = getEmployeeDTOs();
    }

    private void mockPageableEmployeeOperations(Pageable pageable, Pageable mappedPageable,
                                                List<Employee> employeeList, List<EmployeeDTO> employeeDTOList) {
        Page<Employee> employeePage = new PageImpl<>(employeeList, mappedPageable, employeeList.size());

        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(mappedPageable)).thenReturn(employeePage);

        for (int i = 0; i < employeeList.size(); i++) {
            when(employeeMapper.toDto(employeeList.get(i))).thenReturn(employeeDTOList.get(i));
        }
    }

    private void mockSimpleEmployeeListOperations(List<Employee> employeeList, List<EmployeeDTO> employeeDTOList) {
        when(employeeRepository.findAll()).thenReturn(employeeList);
        for (int i = 0; i < employeeList.size(); i++) {
            when(employeeMapper.toDto(employeeList.get(i))).thenReturn(employeeDTOList.get(i));
        }
    }

    private void mockEmptyPageableRepository(Pageable pageable, Pageable mappedPageable) {
        Page<Employee> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);
        when(sortMappingService.applyMappings(pageable, "employee")).thenReturn(mappedPageable);
        when(employeeRepository.findAll(mappedPageable)).thenReturn(emptyPage);
    }

    private void mockSuccessfulEmployeeUpdate(EmployeeDTO updateData, Employee updatedEmployee) {
        when(employeeMapper.toEntity(updateData)).thenReturn(updatedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            assertEquals(employee.getId(), saved.getId());
            return saved;
        });
        when(employeeMapper.toDto(updatedEmployee)).thenReturn(updateData);
    }

    private void mockSuccessfulEmployeeUpdateDTO(EmployeeUpdateDTO updateData, Employee mappedEmployee) {
        when(employeeMapper.toEntity(updateData)).thenReturn(mappedEmployee);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void mockPasswordEncoding(String rawPassword, String encodedPassword) {
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
    }

    private void mockEmployeeCreation(EmployeeDTO dto, Employee employeeToSave, Employee savedEmployee,
                                      String rawPassword, String encodedPassword) {
        when(employeeMapper.toEntity(dto)).thenReturn(employeeToSave);
        mockPasswordEncoding(rawPassword, encodedPassword);
        when(employeeRepository.save(employeeToSave)).thenReturn(savedEmployee);
        when(employeeMapper.toDto(savedEmployee)).thenReturn(dto);
    }

    private void mockEmployeeCreationFailure(EmployeeDTO dto, Employee employeeToSave) {
        when(employeeMapper.toEntity(dto)).thenReturn(employeeToSave);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(employeeToSave)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));
    }

    private EmployeeUpdateDTO createEmployeeUpdateDTO(String name, String phone, LocalDate birthDate) {
        EmployeeUpdateDTO updateData = new EmployeeUpdateDTO();
        updateData.setName(name);
        updateData.setPhone(phone);
        updateData.setBirthDate(birthDate);
        return updateData;
    }

    private Employee createMappedEmployee(String name, String phone, LocalDate birthDate) {
        Employee mappedEmployee = new Employee();
        mappedEmployee.setName(name);
        mappedEmployee.setPhone(phone);
        mappedEmployee.setBirthDate(birthDate);
        return mappedEmployee;
    }

    private void verifyPageableOperations(Pageable originalPageable, Pageable mappedPageable) {
        verify(sortMappingService).applyMappings(originalPageable, "employee");
        verify(employeeRepository).findAll(mappedPageable);
    }

    private void verifyPagedResults(Page<EmployeeDTO> result, List<EmployeeDTO> expectedContent, int expectedTotalElements) {
        assertNotNull(result);
        assertEquals(expectedTotalElements, result.getTotalElements());
        assertEquals(expectedContent, result.getContent());
    }

    private void verifyListResults(List<EmployeeDTO> result, List<EmployeeDTO> expectedContent) {
        assertNotNull(result);
        assertEquals(expectedContent.size(), result.size());
        assertEquals(expectedContent, result);
    }

    private void verifyEmptyResults(Page<EmployeeDTO> result) {
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    private void verifyEmptyListResults(List<EmployeeDTO> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private void verifyEmployeeUpdateOperations(String email, EmployeeDTO updateData, Employee updatedEmployee) {
        verify(employeeRepository).getByEmail(email);
        verify(employeeMapper).toEntity(updateData);
        verify(employeeRepository).save(argThat(emp -> emp.getId().equals(employee.getId())));
        verify(employeeMapper).toDto(updatedEmployee);
    }

    private void verifyEmployeeUpdateDTOOperations(String email, EmployeeUpdateDTO updateData,
                                                   String expectedName, String expectedPhone, LocalDate expectedBirthDate) {
        verify(employeeRepository).getByEmail(email);
        verify(employeeMapper).toEntity(updateData);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());

        Employee savedEmployee = employeeCaptor.getValue();
        assertEquals(employee.getId(), savedEmployee.getId());
        assertEquals(employee.getPassword(), savedEmployee.getPassword());
        assertEquals(employee.getEmail(), savedEmployee.getEmail());
        assertEquals(expectedName, savedEmployee.getName());
        assertEquals(expectedPhone, savedEmployee.getPhone());
        assertEquals(expectedBirthDate, savedEmployee.getBirthDate());
    }

    private void verifyNotFoundScenario(String email) {
        Exception exception = assertThrows(NotFoundException.class,
                () -> employeeService.getEmployeeByEmail(email));
        assertTrue(exception.getMessage().contains("Employee with email " + email));
        verify(employeeRepository).getByEmail(email);
        verify(employeeMapper, never()).toDto(any());
    }

    private void verifyEmployeeCreationOperations(EmployeeDTO dto, String rawPassword) {
        verify(employeeMapper).toEntity(dto);
        verify(passwordEncoder).encode(rawPassword);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toDto(any(Employee.class));
    }

    private void verifyPasswordUpdateOperations(String email, String newPassword, String encodedPassword) {
        verify(employeeRepository).getByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(employeeRepository).save(employee);
        assertEquals(encodedPassword, employee.getPassword());
    }

    private void testSortingScenario(Sort sort) {
        Pageable pageable = PageRequest.of(0, 10, sort);
        Pageable mappedPageable = PageRequest.of(0, 10, sort);

        mockPageableEmployeeOperations(pageable, mappedPageable, employees, employeeDTOs);

        // Act
        employeeService.getAllEmployees(pageable);

        // Assert
        verifyPageableOperations(pageable, mappedPageable);
    }

    @Test
    void getAllEmployees_WithPageable_ShouldReturnPageOfEmployeeDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockPageableEmployeeOperations(pageable, mappedPageable, employees, employeeDTOs);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        verifyPagedResults(result, employeeDTOs, employees.size());
        verifyPageableOperations(pageable, mappedPageable);
        verify(employeeMapper, times(employees.size())).toDto(any(Employee.class));
    }

    @Test
    void getAllEmployees_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("name"));
    }

    @Test
    void getAllEmployees_WithSortByBirthDateDesc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("birthdate").descending());
    }

    @Test
    void getAllEmployees_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        Sort multiSort = Sort.by("name").ascending().and(Sort.by("birthdate").descending());
        testSortingScenario(multiSort);
    }

    @Test
    void getAllEmployees_WithoutPageable_ShouldReturnListOfEmployeeDTOs() {
        // Arrange
        mockSimpleEmployeeListOperations(employees, employeeDTOs);

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        verifyListResults(result, employeeDTOs);
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
        verifyEmptyListResults(result);
        verify(employeeRepository).findAll();
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    void getAllEmployees_WithPageable_WhenEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockEmptyPageableRepository(pageable, mappedPageable);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        verifyEmptyResults(result);
        verify(employeeRepository).findAll(mappedPageable);
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
        mockSuccessfulEmployeeUpdate(updateData, updatedEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(updateData, result);
        verifyEmployeeUpdateOperations(employee.getEmail(), updateData, updatedEmployee);
    }

    @Test
    void updateEmployeeByEmailWithEmployeeDTO_WhenEmployeeDoesNotExist_ShouldThrowNotFoundException() {
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
        EmployeeUpdateDTO updateData = createEmployeeUpdateDTO("Updated Name", "+1-555-9999", LocalDate.of(1990, 1, 1));
        Employee mappedEmployee = createMappedEmployee("Updated Name", "+1-555-9999", LocalDate.of(1990, 1, 1));

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        mockSuccessfulEmployeeUpdateDTO(updateData, mappedEmployee);

        // Act
        employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        verifyEmployeeUpdateDTOOperations(employee.getEmail(), updateData, "Updated Name", "+1-555-9999", LocalDate.of(1990, 1, 1));
    }

    @Test
    void updateEmployeeByEmailWithEmployeeUpdateDTO_WhenEmployeeDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String employeeEmail = "nonexistent@example.com";
        EmployeeUpdateDTO updateData = createEmployeeUpdateDTO("Updated Name", "+1-555-9999", LocalDate.of(1990, 1, 1));
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
        EmployeeUpdateDTO updateData = createEmployeeUpdateDTO("New Name", "+1-555-0000", LocalDate.of(1995, 5, 5));
        Employee mappedEmployee = createMappedEmployee("New Name", "+1-555-0000", LocalDate.of(1995, 5, 5));

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        mockSuccessfulEmployeeUpdateDTO(updateData, mappedEmployee);

        // Act
        employeeService.updateEmployeeByEmail(employee.getEmail(), updateData);

        // Assert
        verifyEmployeeUpdateDTOOperations(employee.getEmail(), updateData, "New Name", "+1-555-0000", LocalDate.of(1995, 5, 5));
    }

    @Test
    void deleteEmployeeByEmail_ShouldCallRepositoryDelete() {
        // Arrange
        String employeeEmail = "test@example.com";

        // Act
        employeeService.deleteEmployeeByEmail(employeeEmail);

        // Assert
        verify(employeeRepository).deleteByEmail(employeeEmail);
        verify(employeeRefreshTokenRepository).deleteEmployeeRefreshTokenByEmployee_Email(employeeEmail);
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

        mockEmployeeCreation(employeeDTO, employeeToSave, savedEmployee, rawPassword, encodedPassword);

        // Act
        EmployeeDTO result = employeeService.addEmployee(employeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(employeeDTO, result);
        verifyEmployeeCreationOperations(employeeDTO, rawPassword);
        assertEquals(encodedPassword, employeeToSave.getPassword());
    }

    @Test
    void addEmployee_WhenEmployeeAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        Employee employeeToSave = getEmployeeEntity();
        mockEmployeeCreationFailure(employeeDTO, employeeToSave);

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
        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
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
        verifyNotFoundScenario(employeeEmail);
    }

    @Test
    void updateEmployeePassword_WhenEmployeeExists_ShouldUpdatePassword() {
        // Arrange
        String employeeEmail = employee.getEmail();
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newEncodedPassword";

        when(employeeRepository.getByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        mockPasswordEncoding(newPassword, encodedPassword);
        when(employeeRepository.save(employee)).thenReturn(employee);

        // Act
        employeeService.updateEmployeePassword(employeeEmail, newPassword);

        // Assert
        verifyPasswordUpdateOperations(employeeEmail, newPassword, encodedPassword);
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
        mockPasswordEncoding(rawPassword, encodedPassword);
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
        mockPageableEmployeeOperations(originalPageable, mappedPageable, employees, employeeDTOs);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(originalPageable);

        // Assert
        assertNotNull(result);
        verifyPageableOperations(originalPageable, mappedPageable);
    }
}