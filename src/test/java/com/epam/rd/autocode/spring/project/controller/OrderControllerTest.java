package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.OrderMustContainClientException;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.OrderData.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO orderDTO;
    private List<OrderDTO> orderDTOs;

    @BeforeEach
    void setUp() {
        orderDTO = getOrderDTO();
        orderDTOs = getOrderDTOs();
    }

    private void verifyOrderJsonResponse(ResultActions resultActions, String jsonPath, OrderDTO expectedOrder) throws Exception {
        resultActions
                .andExpect(jsonPath(jsonPath + ".employee_email").value(expectedOrder.getEmployeeEmail()))
                .andExpect(jsonPath(jsonPath + ".client_email").value(expectedOrder.getClientEmail()))
                .andExpect(jsonPath(jsonPath + ".order_date").value(Matchers.startsWith(
                        expectedOrder.getOrderDate().toString())))
                .andExpect(jsonPath(jsonPath + ".price").value(expectedOrder.getPrice()))
                .andExpect(jsonPath(jsonPath + ".book_items").isArray())
                .andExpect(jsonPath(jsonPath + ".book_items", hasSize(expectedOrder.getBookItems().size())));
    }

    private void verifyAllOrdersInPaginatedResponse(ResultActions resultActions, List<OrderDTO> expectedOrders) throws Exception {
        for (int i = 0; i < expectedOrders.size(); i++) {
            verifyOrderJsonResponse(resultActions, "$.orders[" + i + "]", expectedOrders.get(i));
        }
    }

    private void verifyPagination(ResultActions resultActions, List<OrderDTO> expectedDtos, int pageNumber, int pageSize) throws Exception{
        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(expectedDtos.size()))
                .andExpect(jsonPath("$.meta.page").value(pageNumber))
                .andExpect(jsonPath("$.meta.page_size").value(pageSize))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders", hasSize(expectedDtos.size())));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllOrders_ShouldReturnPaginatedOrders() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10)
                .withSort(Sort.by("orderDate"));
        List<OrderDTO> filteredDtos = orderDTOs.stream().limit(10)
                .sorted(Comparator.comparing(OrderDTO::getOrderDate)).toList();
        Page<OrderDTO> page = new PageImpl<>(filteredDtos, pageable, filteredDtos.size());
        when(orderService.getAllOrders(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        verifyPagination(resultActions, filteredDtos, 0, 10);
        verifyAllOrdersInPaginatedResponse(resultActions, filteredDtos);
        verify(orderService).getAllOrders(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllOrders_WithCustomPageSize_ShouldReturnCorrectPagination() throws Exception {
        // Arrange
        int pageSize = 5;
        Pageable pageable = PageRequest.of(0, pageSize).withSort(Sort.by("orderDate"));
        List<OrderDTO> filteredDtos = orderDTOs.stream().limit(pageSize)
                .sorted(Comparator.comparing(OrderDTO::getOrderDate)).toList();
        Page<OrderDTO> page = new PageImpl<>(filteredDtos, pageable, filteredDtos.size());
        when(orderService.getAllOrders(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/orders")
                        .param("size", String.valueOf(pageSize)))
                .andExpect(status().isOk());

        verifyPagination(resultActions, filteredDtos, 0, pageSize);
        verifyAllOrdersInPaginatedResponse(resultActions, filteredDtos);
        verify(orderService).getAllOrders(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllOrders_WithSorting_ShouldReturnSortedOrders() throws Exception {
        // Arrange
        List<OrderDTO> sortedOrders = orderDTOs.stream()
                .sorted(Comparator.comparing(OrderDTO::getPrice).reversed())
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Page<OrderDTO> page = new PageImpl<>(sortedOrders, pageable, sortedOrders.size());
        when(orderService.getAllOrders(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/orders")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk());

        verifyAllOrdersInPaginatedResponse(resultActions, sortedOrders);
        verify(orderService).getAllOrders(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllOrders_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("orderDate"));
        Page<OrderDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(orderService.getAllOrders(pageable)).thenReturn(emptyPage);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(0))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders", hasSize(0)));
        verifyPagination(resultActions, List.of(), 0, 10);
        verify(orderService).getAllOrders(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllOrders_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isForbidden());
        verify(orderService, never()).getAllOrders(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void addOrder_WhenUserIsClient_ShouldCreateOrder() throws Exception {
        // Arrange
        when(orderService.addOrder(orderDTO)).thenReturn(orderDTO);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isCreated());

        verifyOrderJsonResponse(resultActions, "$", orderDTO);
        verify(orderService).addOrder(orderDTO);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void addOrder_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        orderDTO.setPrice(new BigDecimal(-100));

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void addOrder_WithInvalidDataMultipleFields_ShouldReturn400() throws Exception {
        // Arrange
        orderDTO.setPrice(null);
        orderDTO.setClientEmail("");
        orderDTO.setOrderDate(null);

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void addOrder_WithMissingClientEmail_ShouldReturn400() throws Exception {
        // Arrange
        when(orderService.addOrder(orderDTO))
                .thenThrow(new OrderMustContainClientException());

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest());
        verify(orderService).addOrder(orderDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addOrder_WithEmployeeRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isForbidden());
        verify(orderService, never()).addOrder(orderDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void confirmOrder_WhenUserIsEmployee_ShouldConfirmSuccessfully() throws Exception {
        // Arrange
        orderDTO.setEmployeeEmail("employee@bookstore.com");

        // Act & Assert
        mockMvc.perform(put("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isNoContent());
        verify(orderService).confirmOrder(orderDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void confirmOrder_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        orderDTO.setPrice(new BigDecimal(-50));

        // Act & Assert
        mockMvc.perform(put("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void confirmOrder_WhenOrderNotFound_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Order not found"))
                .when(orderService).confirmOrder(orderDTO);

        // Act & Assert
        mockMvc.perform(put("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isNotFound());
        verify(orderService).confirmOrder(orderDTO);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void confirmOrder_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isForbidden());
        verify(orderService, never()).confirmOrder(orderDTO);
    }

    @Test
    void getAllOrders_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
        verify(orderService, never()).getAllOrders(any(Pageable.class));
    }

    @Test
    void addOrder_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isUnauthorized());
        verify(orderService, never()).addOrder(any(OrderDTO.class));
    }

    @Test
    void confirmOrder_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isUnauthorized());
        verify(orderService, never()).confirmOrder(any(OrderDTO.class));
    }
}