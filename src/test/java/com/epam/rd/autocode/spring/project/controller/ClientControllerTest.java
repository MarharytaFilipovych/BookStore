package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
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
import java.util.Comparator;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.*;
import static com.epam.rd.autocode.spring.project.testdata.OrderData.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientDTO clientDTO;
    private List<ClientDTO> clientDTOs;
    private ClientUpdateDTO clientUpdateDTO;
    private List<OrderDTO> orderDTOs;

    @BeforeEach
    void setUp() {
        clientDTO = getClientDTO();
        clientDTOs = getClientDTOs();
        clientUpdateDTO = getClientUpdateDTO();
        orderDTOs = getOrderDTOs();
    }

    private void verifyClientJsonResponse(ResultActions resultActions, String jsonPath, ClientDTO expectedClient) throws Exception {
        resultActions
                .andExpect(jsonPath(jsonPath + ".email").value(expectedClient.getEmail()))
                .andExpect(jsonPath(jsonPath + ".name").value(expectedClient.getName()))
                .andExpect(jsonPath(jsonPath + ".balance").value(expectedClient.getBalance().doubleValue()));
    }

    private void verifyAllClientsInPaginatedResponse(ResultActions resultActions, List<ClientDTO> expectedClients) throws Exception {
        for (int i = 0; i < expectedClients.size(); i++) {
            verifyClientJsonResponse(resultActions, "$.clients[" + i + "]", expectedClients.get(i));
        }
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

    private void verifyPaginatedMetadata(ResultActions resultActions, int expectedTotalCount, int expectedPage, int expectedSize) throws Exception {
        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(expectedTotalCount))
                .andExpect(jsonPath("$.meta.page").value(expectedPage))
                .andExpect(jsonPath("$.clients").isArray())
                .andExpect(jsonPath("$.clients", hasSize(expectedSize)));
    }

    private void verifyOrderPaginatedMetadata(ResultActions resultActions, int expectedTotalCount, int expectedPage, int expectedSize) throws Exception {
        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(expectedTotalCount))
                .andExpect(jsonPath("$.meta.page").value(expectedPage))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders", hasSize(expectedSize)));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllClients_ShouldReturnPaginatedClients() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("name"));
        List<ClientDTO> filteredClients = clientDTOs.stream()
                .limit(10).sorted(Comparator.comparing(ClientDTO::getName)).toList();
        Page<ClientDTO> page = new PageImpl<>(filteredClients, pageable, clientDTOs.size());
        when(clientService.getAllClients(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, clientDTOs.size(), 0, filteredClients.size());
        verifyAllClientsInPaginatedResponse(resultActions, filteredClients);
        verify(clientService).getAllClients(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllClients_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2).withSort(Sort.by("name"));
        List<ClientDTO> filteredClients = clientDTOs.stream()
                .skip(2).limit(2).sorted(Comparator.comparing(ClientDTO::getName)).toList();
        Page<ClientDTO> page = new PageImpl<>(filteredClients, pageable, clientDTOs.size());
        when(clientService.getAllClients(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, clientDTOs.size(), 1, filteredClients.size());
        verifyAllClientsInPaginatedResponse(resultActions, filteredClients);
        verify(clientService).getAllClients(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllClients_WithSorting_ShouldReturnSortedClients() throws Exception {
        // Arrange
        List<ClientDTO> sortedClients = clientDTOs.stream()
                .sorted(Comparator.comparing(ClientDTO::getBalance).reversed())
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("balance").descending());
        Page<ClientDTO> page = new PageImpl<>(sortedClients, pageable, sortedClients.size());
        when(clientService.getAllClients(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients")
                        .param("sort", "balance,desc"))
                .andExpect(status().isOk());

        verifyAllClientsInPaginatedResponse(resultActions, sortedClients);
        verify(clientService).getAllClients(pageable);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getAllClients_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("name"));
        Page<ClientDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(clientService.getAllClients(pageable)).thenReturn(emptyPage);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(0))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.clients").isArray())
                .andExpect(jsonPath("$.clients", hasSize(0)));
        verify(clientService).getAllClients(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllClients_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients"))
                .andExpect(status().isForbidden());
        verify(clientService, never()).getAllClients(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void getClientByEmail_WhenClientAccessesOwnData_ShouldReturnClient() throws Exception {
        // Arrange
        when(clientService.getClientByEmail(clientDTO.getEmail())).thenReturn(clientDTO);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isOk());
        verifyClientJsonResponse(resultActions, "$", clientDTO);
        verify(clientService).getClientByEmail(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(username = "other@email.com", roles = {"CLIENT"})
    void getClientByEmail_WhenClientAccessesOtherData_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(clientService, never()).getClientByEmail(anyString());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void getClientByEmail_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(clientService.getClientByEmail(clientDTO.getEmail()))
                .thenThrow(new NotFoundException("Client with email " + clientDTO.getEmail()));

        // Act & Assert
        mockMvc.perform(get("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isNotFound());
        verify(clientService).getClientByEmail(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void getClientByEmail_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/{email}", "invalid-email"))
                .andExpect(status().isBadRequest());
        verify(clientService, never()).getClientByEmail(anyString());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void getOrdersByClient_WhenClientAccessesOwnOrders_ShouldReturnOrders() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("orderDate"));
        Page<OrderDTO> page = new PageImpl<>(orderDTOs, pageable, orderDTOs.size());
        when(orderService.getOrdersByClient(clientDTO.getEmail(), pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients/{email}/orders", clientDTO.getEmail()))
                .andExpect(status().isOk());
        verifyOrderPaginatedMetadata(resultActions, orderDTOs.size(), 0, orderDTOs.size());
        verifyAllOrdersInPaginatedResponse(resultActions, orderDTOs);
        verify(orderService).getOrdersByClient(clientDTO.getEmail(), pageable);
    }

    @Test
    @WithMockUser(username = "other@email.com", roles = {"CLIENT"})
    void getOrdersByClient_WhenClientAccessesOtherOrders_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/{email}/orders", clientDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(orderService, never()).getOrdersByClient(anyString(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void getOrdersByClient_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2).withSort(Sort.by("orderDate"));
        List<OrderDTO> pageOrders = orderDTOs.stream().skip(2).limit(2).toList();
        Page<OrderDTO> page = new PageImpl<>(pageOrders, pageable, orderDTOs.size());
        when(orderService.getOrdersByClient(clientDTO.getEmail(), pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients/{email}/orders", clientDTO.getEmail())
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk());
        verifyOrderPaginatedMetadata(resultActions, orderDTOs.size(), 1, pageOrders.size());
        verifyAllOrdersInPaginatedResponse(resultActions, pageOrders);
        verify(orderService).getOrdersByClient(clientDTO.getEmail(), pageable);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void updateClient_WhenClientUpdatesOwnData_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(clientService).updateClientByEmail(clientDTO.getEmail(), clientUpdateDTO);

        // Act & Assert
        mockMvc.perform(put("/clients/{email}", clientDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDTO)))
                .andExpect(status().isNoContent());
        verify(clientService).updateClientByEmail(clientDTO.getEmail(), clientUpdateDTO);
    }

    @Test
    @WithMockUser(username = "other@email.com", roles = {"CLIENT"})
    void updateClient_WhenClientUpdatesOtherData_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/clients/{email}", clientDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDTO)))
                .andExpect(status().isForbidden());
        verify(clientService, never()).updateClientByEmail(anyString(), any(ClientUpdateDTO.class));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void updateClient_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        clientUpdateDTO.setName("");
        clientUpdateDTO.setBalance(new BigDecimal("-100"));

        // Act & Assert
        mockMvc.perform(put("/clients/{email}", clientDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void updateClient_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Client with email " + clientDTO.getEmail()))
                .when(clientService).updateClientByEmail(clientDTO.getEmail(), clientUpdateDTO);

        // Act & Assert
        mockMvc.perform(put("/clients/{email}", clientDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDTO)))
                .andExpect(status().isNotFound());
        verify(clientService).updateClientByEmail(clientDTO.getEmail(), clientUpdateDTO);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void deleteClient_WhenClientDeletesOwnAccount_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(clientService).deleteClientByEmail(clientDTO.getEmail());

        // Act & Assert
        mockMvc.perform(delete("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isNoContent());
        verify(clientService).deleteClientByEmail(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(username = "other@email.com", roles = {"CLIENT"})
    void deleteClient_WhenClientDeletesOtherAccount_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(clientService, never()).deleteClientByEmail(anyString());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL_1, roles = {"CLIENT"})
    void deleteClient_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Client with email " + clientDTO.getEmail()))
                .when(clientService).deleteClientByEmail(clientDTO.getEmail());

        // Act & Assert
        mockMvc.perform(delete("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isNotFound());
        verify(clientService).deleteClientByEmail(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getBlockedClients_ShouldReturnPaginatedBlockedClients() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("name"));
        List<ClientDTO> blockedClients = clientDTOs.stream().limit(2).toList();
        Page<ClientDTO> page = new PageImpl<>(blockedClients, pageable, blockedClients.size());
        when(clientService.getBlockedClients(pageable)).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients/blocked"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, blockedClients.size(), 0, blockedClients.size());
        verifyAllClientsInPaginatedResponse(resultActions, blockedClients);
        verify(clientService).getBlockedClients(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getBlockedClients_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/blocked"))
                .andExpect(status().isForbidden());
        verify(clientService, never()).getBlockedClients(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void blockClient_WhenEmployeeBlocksClient_ShouldReturnEmail() throws Exception {
        // Arrange
        doNothing().when(clientService).blockClient(clientDTO.getEmail());

        // Act & Assert
        mockMvc.perform(post("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().string(clientDTO.getEmail()));
        verify(clientService).blockClient(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void blockClient_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(clientService, never()).blockClient(anyString());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void blockClient_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Client with email " + clientDTO.getEmail()))
                .when(clientService).blockClient(clientDTO.getEmail());

        // Act & Assert
        mockMvc.perform(post("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isNotFound());
        verify(clientService).blockClient(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void blockClient_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/clients/blocked/{email}", "invalid-email"))
                .andExpect(status().isBadRequest());
        verify(clientService, never()).blockClient(anyString());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void unblockClient_WhenEmployeeUnblocksClient_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(clientService).unblockClient(clientDTO.getEmail());

        // Act & Assert
        mockMvc.perform(delete("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isNoContent());
        verify(clientService).unblockClient(clientDTO.getEmail());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void unblockClient_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isForbidden());
        verify(clientService, never()).unblockClient(anyString());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void unblockClient_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/clients/blocked/{email}", "invalid-email"))
                .andExpect(status().isBadRequest());
        verify(clientService, never()).unblockClient(anyString());
    }

    @Test
    void getAllClients_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients"))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).getAllClients(any(Pageable.class));
    }

    @Test
    void getClientByEmail_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).getClientByEmail(anyString());
    }

    @Test
    void getOrdersByClient_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/{email}/orders", clientDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(orderService, never()).getOrdersByClient(anyString(), any(Pageable.class));
    }

    @Test
    void updateClient_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/clients/{email}", clientDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDTO)))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).updateClientByEmail(anyString(), any(ClientUpdateDTO.class));
    }

    @Test
    void deleteClient_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/clients/{email}", clientDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).deleteClientByEmail(anyString());
    }

    @Test
    void getBlockedClients_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/blocked"))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).getBlockedClients(any(Pageable.class));
    }

    @Test
    void blockClient_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).blockClient(anyString());
    }

    @Test
    void unblockClient_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/clients/blocked/{email}", clientDTO.getEmail()))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).unblockClient(anyString());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getBlockedClientsList_ShouldReturnListOfBlockedClients() throws Exception {
        // Arrange
        List<ClientDTO> blockedClients = clientDTOs.stream().limit(3).toList();
        when(clientService.getBlockedClients()).thenReturn(blockedClients);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/clients/blocked/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(blockedClients.size())));

        for (int i = 0; i < blockedClients.size(); i++) {
            verifyClientJsonResponse(resultActions, "$[" + i + "]", blockedClients.get(i));
        }

        verify(clientService).getBlockedClients();
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getBlockedClientsList_WithEmptyResults_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(clientService.getBlockedClients()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/clients/blocked/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(clientService).getBlockedClients();
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getBlockedClientsList_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/blocked/list"))
                .andExpect(status().isForbidden());

        verify(clientService, never()).getBlockedClients();
    }

    @Test
    void getBlockedClientsList_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/clients/blocked/list"))
                .andExpect(status().isUnauthorized());

        verify(clientService, never()).getBlockedClients();
    }
}