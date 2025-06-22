package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeDTO employeeDTO;
    private List<EmployeeDTO> employeeDTOs;
    private EmployeeUpdateDTO employeeUpdateDTO;

    @BeforeEach
    void setUp() {
        employeeDTO = getEmployeeDTO();
        employeeDTOs = getEmployeeDTOs();
        employeeUpdateDTO = getEmployeeUpdateDTO();
    }

    private void verifyEmployeeJsonResponse(ResultActions resultActions, String jsonPath, EmployeeDTO expectedEmployee) throws Exception {
        resultActions
                .andExpect(jsonPath(jsonPath + ".email").value(expectedEmployee.getEmail()))
                .andExpect(jsonPath(jsonPath + ".name").value(expectedEmployee.getName()))
                .andExpect(jsonPath(jsonPath + ".phone").value(expectedEmployee.getPhone()))
                .andExpect(jsonPath(jsonPath + ".birthdate").value(expectedEmployee.getBirthDate().toString()));
    }

    private void verifyAllEmployeesInPaginatedResponse(ResultActions resultActions, List<EmployeeDTO> expectedEmployees) throws Exception {
        for (int i = 0; i < expectedEmployees.size(); i++) {
            verifyEmployeeJsonResponse(resultActions, "$.employees[" + i + "]", expectedEmployees.get(i));
        }
    }

    private void verifyPaginatedMetadata(ResultActions resultActions, int expectedTotalCount, int expectedPage, int expectedSize) throws Exception {
        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(expectedTotalCount))
                .andExpect(jsonPath("$.meta.page").value(expectedPage))
                .andExpect(jsonPath("$.employees").isArray())
                .andExpect(jsonPath("$.employees", hasSize(expectedSize)));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllEmployees_ShouldReturnPaginatedEmployees() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("name"));
        List<EmployeeDTO> filteredEmployees = employeeDTOs.stream()
                .limit(10).sorted(Comparator.comparing(EmployeeDTO::getName)).toList();
        Page<EmployeeDTO> page = new PageImpl<>(filteredEmployees, pageable, filteredEmployees.size());
        when(employeeService.getAllEmployees(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, filteredEmployees.size(), 0, filteredEmployees.size());
        verifyAllEmployeesInPaginatedResponse(resultActions, filteredEmployees);
        verify(employeeService).getAllEmployees(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllEmployees_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2).withSort(Sort.by("name"));
        List<EmployeeDTO> filteredEmployees = employeeDTOs.stream().skip(2)
                .limit(2).sorted(Comparator.comparing(EmployeeDTO::getName)).toList();
        Page<EmployeeDTO> page = new PageImpl<>(filteredEmployees, pageable, filteredEmployees.size());
        when(employeeService.getAllEmployees(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/employees")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, employeeDTOs.size(), 1, filteredEmployees.size());
        verifyAllEmployeesInPaginatedResponse(resultActions, filteredEmployees);
        verify(employeeService).getAllEmployees(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllEmployees_WithSorting_ShouldReturnSortedEmployees() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("birthDate"));
        List<EmployeeDTO> filteredEmployees = employeeDTOs.stream()
                .limit(10).sorted(Comparator.comparing(EmployeeDTO::getBirthDate)).toList();
        Page<EmployeeDTO> page = new PageImpl<>(filteredEmployees, pageable, filteredEmployees.size());
        when(employeeService.getAllEmployees(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/employees")
                        .param("sort", "birthDate"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, filteredEmployees.size(), 0, filteredEmployees.size());
        verifyAllEmployeesInPaginatedResponse(resultActions, filteredEmployees);
        verify(employeeService).getAllEmployees(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllEmployees_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).getAllEmployees(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getEmployeeByEmail_ShouldReturnEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByEmail(employeeDTO.getEmail())).thenReturn(employeeDTO);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/employees/{email}", employeeDTO.getEmail()))
                .andExpect(status().isOk());
        verifyEmployeeJsonResponse(resultActions, "$", employeeDTO);
        verify(employeeService).getEmployeeByEmail(employeeDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getEmployeeByEmail_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(employeeService.getEmployeeByEmail(nonExistentEmail))
                .thenThrow(new NotFoundException("Employee with email " + nonExistentEmail));

        // Act & Assert
        mockMvc.perform(get("/employees/{email}", nonExistentEmail))
                .andExpect(status().isNotFound());
        verify(employeeService).getEmployeeByEmail(nonExistentEmail);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getEmployeeByEmail_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/{email}", "invalid-email"))
                .andExpect(status().isBadRequest());
        verify(employeeService, never()).getEmployeeByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getEmployeeByEmail_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/{email}", employeeDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).getEmployeeByEmail(employeeDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "mike.stevens@bookstore.com")
    void updateEmployee_WhenUserUpdatesOwnProfile_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        String userEmail = "mike.stevens@bookstore.com";
        doNothing().when(employeeService).updateEmployeeByEmail(userEmail, employeeUpdateDTO);

        // Act & Assert
        mockMvc.perform(put("/employees/{email}", userEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeUpdateDTO)))
                .andExpect(status().isNoContent());
        verify(employeeService).updateEmployeeByEmail(userEmail, employeeUpdateDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "different.user@bookstore.com")
    void updateEmployee_WhenUserUpdatesOtherProfile_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/{email}", employeeDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeUpdateDTO)))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).updateEmployeeByEmail(employeeDTO.getEmail(), employeeUpdateDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "mike.stevens@bookstore.com")
    void updateEmployee_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        String userEmail = "mike.stevens@bookstore.com";
        doThrow(new NotFoundException("Employee with email " + userEmail))
                .when(employeeService).updateEmployeeByEmail(userEmail, employeeUpdateDTO);

        // Act & Assert
        mockMvc.perform(put("/employees/{email}", userEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeUpdateDTO)))
                .andExpect(status().isNotFound());
        verify(employeeService).updateEmployeeByEmail(userEmail, employeeUpdateDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "mike.stevens@bookstore.com")
    void updateEmployee_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        String userEmail = "mike.stevens@bookstore.com";
        EmployeeUpdateDTO invalidDTO = new EmployeeUpdateDTO();
        invalidDTO.setName(""); // Invalid name
        invalidDTO.setPhone("invalid-phone"); // Invalid phone
        invalidDTO.setBirthDate(LocalDate.now().plusDays(1)); // Future date

        // Act & Assert
        mockMvc.perform(put("/employees/{email}", userEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
        verify(employeeService, never()).updateEmployeeByEmail(anyString(), any(EmployeeUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void updateEmployee_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/{email}", employeeDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeUpdateDTO)))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).updateEmployeeByEmail(employeeDTO.getEmail(), employeeUpdateDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "mike.stevens@bookstore.com")
    void deleteEmployee_WhenUserDeletesOwnAccount_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        String userEmail = "mike.stevens@bookstore.com";
        doNothing().when(employeeService).deleteEmployeeByEmail(userEmail);

        // Act & Assert
        mockMvc.perform(delete("/employees/{email}", userEmail))
                .andExpect(status().isNoContent());
        verify(employeeService).deleteEmployeeByEmail(userEmail);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "different.user@bookstore.com")
    void deleteEmployee_WhenUserDeletesOtherAccount_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/{email}", employeeDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).deleteEmployeeByEmail(employeeDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"}, username = "mike.stevens@bookstore.com")
    void deleteEmployee_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        String userEmail = "mike.stevens@bookstore.com";
        doThrow(new NotFoundException("Employee with email " + userEmail))
                .when(employeeService).deleteEmployeeByEmail(userEmail);

        // Act & Assert
        mockMvc.perform(delete("/employees/{email}", userEmail))
                .andExpect(status().isNotFound());
        verify(employeeService).deleteEmployeeByEmail(userEmail);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void deleteEmployee_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/{email}", employeeDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).deleteEmployeeByEmail(employeeDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getOrdersByEmployee_ShouldReturnPaginatedOrders() throws Exception {
        // Arrange
        String employeeEmail = employeeDTO.getEmail();
        List<OrderDTO> orders = List.of(); // Empty list for simplicity
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderDTO> page = new PageImpl<>(orders, pageable, orders.size());
        when(orderService.getOrdersByEmployee(eq(employeeEmail), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/employees/{email}/orders", employeeEmail))
                .andExpect(status().isOk());
        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(orders.size()))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders", hasSize(orders.size())));
        verify(orderService).getOrdersByEmployee(eq(employeeEmail), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getOrdersByEmployee_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/{email}/orders", "invalid-email"))
                .andExpect(status().isBadRequest());
        verify(orderService, never()).getOrdersByEmployee(anyString(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getOrdersByEmployee_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/{email}/orders", employeeDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(orderService, never()).getOrdersByEmployee(anyString(), any(Pageable.class));
    }

    // Authentication tests
    @Test
    void getAllEmployees_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
        verify(employeeService, never()).getAllEmployees(any(Pageable.class));
    }

    @Test
    void getEmployeeByEmail_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/{email}", employeeDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(employeeService, never()).getEmployeeByEmail(employeeDTO.getEmail());
    }

    @Test
    void updateEmployee_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/{email}", employeeDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeUpdateDTO)))
                .andExpect(status().isUnauthorized());
        verify(employeeService, never()).updateEmployeeByEmail(employeeDTO.getEmail(), employeeUpdateDTO);
    }

    @Test
    void deleteEmployee_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/{email}", employeeDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(employeeService, never()).deleteEmployeeByEmail(employeeDTO.getEmail());
    }

    @Test
    void getOrdersByEmployee_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/{email}/orders", employeeDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(orderService, never()).getOrdersByEmployee(anyString(), any(Pageable.class));
    }
}