package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.ClientMapper;
import com.epam.rd.autocode.spring.project.model.BlockedClient;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private BlockedClientRepository blockedClientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private List<Client> clients;
    private List<ClientDTO> clientDTOs;
    private Client client;
    private ClientDTO clientDTO;
    private BlockedClient blockedClient;

    @BeforeEach
    public void setUp(){
        client = getClientEntity();
        clientDTO = getClientDTO();
        clients = getClientEnities();
        clientDTOs = getClientDTOs();
        blockedClient = new BlockedClient(client);
    }

    @Test
    void getAllClients_WithPageable_ShouldReturnPageOfClientDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(clients, pageable, clients.size());
        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        for (int i = 0; i < clients.size(); i++) {
            when(clientMapper.toDto(clients.get(i))).thenReturn(clientDTOs.get(i));
        }

        // Act
        Page<ClientDTO> result = clientService.getAllClients(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(clients.size(), result.getTotalElements());
        assertEquals(clientDTOs, result.getContent());
        verify(clientRepository).findAll(pageable);
        verify(clientMapper, times(clients.size())).toDto(any(Client.class));
    }

    @Test
    void getAllClients_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Client> clientPage = new PageImpl<>(clients, pageable, clients.size());
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);
        for (int i = 0; i < clients.size(); i++) {
            when(clientMapper.toDto(clients.get(i))).thenReturn(clientDTOs.get(i));
        }

        // Act
        clientService.getAllClients(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(clientRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order nameOrder = capturedPageable.getSort().getOrderFor("name");
        assertNotNull(nameOrder);
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());
        assertEquals("name", nameOrder.getProperty());
    }

    @Test
    void getAllClients_WithSortByEmailDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("balance").descending());
        Page<Client> clientPage = new PageImpl<>(clients, pageable, clients.size());
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);
        for (int i = 0; i < clients.size(); i++) {
            when(clientMapper.toDto(clients.get(i))).thenReturn(clientDTOs.get(i));
        }

        // Act
        clientService.getAllClients(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(clientRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order balanceOrder = capturedPageable.getSort().getOrderFor("balance");
        assertNotNull(balanceOrder);
        assertEquals(Sort.Direction.DESC, balanceOrder.getDirection());
        assertEquals("balance", balanceOrder.getProperty());
    }

    @Test
    void getAllClients_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange
        Sort multiSort = Sort.by("name").ascending().and(Sort.by("balance").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Page<Client> clientPage = new PageImpl<>(clients, pageable, clients.size());
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);
        for (int i = 0; i < clients.size(); i++) {
            when(clientMapper.toDto(clients.get(i))).thenReturn(clientDTOs.get(i));
        }

        // Act
        clientService.getAllClients(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(clientRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort capturedSort = capturedPageable.getSort();

        List<Sort.Order> orders = capturedSort.toList();
        assertEquals(2, orders.size());

        Sort.Order nameOrder = orders.get(0);
        assertEquals("name", nameOrder.getProperty());
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());

        Sort.Order balanceOrder = orders.get(1);
        assertEquals("balance", balanceOrder.getProperty());
        assertEquals(Sort.Direction.DESC, balanceOrder.getDirection());
    }

    @Test
    void getAllClients_WithoutPageable_ShouldReturnListOfClientDTOs() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(clients);
        for (int i = 0; i < clients.size(); i++) {
            when(clientMapper.toDto(clients.get(i))).thenReturn(clientDTOs.get(i));
        }

        // Act
        List<ClientDTO> result = clientService.getAllClients();

        // Assert
        assertNotNull(result);
        assertEquals(clients.size(), result.size());
        assertEquals(clientDTOs, result);
        verify(clientRepository).findAll();
        verify(clientMapper, times(clients.size())).toDto(any(Client.class));
    }

    @Test
    void getAllClients_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(List.of());

        // Act
        List<ClientDTO> result = clientService.getAllClients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository).findAll();
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void getAllClients_WithPageable_WhenEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(clientRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<ClientDTO> result = clientService.getAllClients(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(clientRepository).findAll(pageable);
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void updateClientByEmail_WhenClientExists_ShouldReturnUpdatedClientDTO() {
        // Arrange
        ClientDTO updateData = getClientDTO();
        updateData.setName("New Name");

        Client updatedClient = getClientEntity();
        updatedClient.setName("New Name");

        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(clientMapper.toEntity(updateData)).thenReturn(updatedClient);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client saved = invocation.getArgument(0);
            assertEquals(client.getId(), saved.getId());
            return saved;
        });
        when(clientMapper.toDto(updatedClient)).thenReturn(updateData);

        // Act
        ClientDTO result = clientService.updateClientByEmail(client.getEmail(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(updateData, result);
        verify(clientRepository).getByEmail(client.getEmail());
        verify(clientMapper).toEntity(updateData);
        verify(clientRepository).save(argThat(c -> c.getId().equals(client.getId())));
        verify(clientMapper).toDto(updatedClient);
    }

    @Test
    void updateClientByEmail_WhenClientDoesNotExist_ShouldThrowNotFoundException(){
        // Arrange
        String clientEmail = "nonexistent@example.com";
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> clientService.updateClientByEmail(clientEmail, clientDTO));

        assertTrue(exception.getMessage().contains("Client with email " + clientEmail));
        verify(clientRepository).getByEmail(clientEmail);
        verify(clientMapper, never()).toEntity(any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClientByEmail_ShouldCallRepositoryDelete() {
        // Arrange
        String clientEmail = "test@example.com";

        // Act
        clientService.deleteClientByEmail(clientEmail);

        // Assert
        verify(blockedClientRepository).deleteByClient_Email(clientEmail);
        verify(clientRepository).deleteByEmail(clientEmail);
    }

    @Test
    void addClient_WhenClientIsValid_ShouldReturnSavedClientDTO() {
        // Arrange
        when(clientMapper.toEntity(clientDTO)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.addClient(clientDTO);

        // Assert
        assertNotNull(result);
        assertEquals(clientDTO, result);
        verify(clientMapper).toEntity(clientDTO);
        verify(clientRepository).save(client);
        verify(clientMapper).toDto(client);
    }

    @Test
    void addClient_WhenClientAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        when(clientMapper.toEntity(clientDTO)).thenReturn(client);
        when(clientRepository.save(client)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        AlreadyExistException exception = assertThrows(AlreadyExistException.class,
                () -> clientService.addClient(clientDTO));

        assertTrue(exception.getMessage().contains("Client with email " + clientDTO.getEmail()));
        verify(clientMapper).toEntity(clientDTO);
        verify(clientRepository).save(client);
    }

    @Test
    void getClientByEmail_WhenClientExists_ShouldReturnClientDTO() {
        // Arrange
        String clientEmail = client.getEmail();
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.getClientByEmail(clientEmail);

        // Assert
        assertNotNull(result);
        assertEquals(clientDTO, result);
        verify(clientRepository).getByEmail(clientEmail);
        verify(clientMapper).toDto(client);
    }

    @Test
    void getClientByEmail_WhenClientDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String clientEmail = "nonexistent@example.com";
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> clientService.getClientByEmail(clientEmail));

        assertTrue(exception.getMessage().contains("Client with email " + clientEmail));
        verify(clientRepository).getByEmail(clientEmail);
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void blockClient_WhenClientExists_ShouldCreateBlockedClient() {
        // Arrange
        String clientEmail = client.getEmail();
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.of(client));
        when(blockedClientRepository.save(any(BlockedClient.class))).thenReturn(blockedClient);

        // Act
        clientService.blockClient(clientEmail);

        // Assert
        verify(clientRepository).getByEmail(clientEmail);
        verify(blockedClientRepository).save(argThat(blocked ->
                blocked.getClient().equals(client)));
    }

    @Test
    void blockClient_WhenClientDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String clientEmail = "nonexistent@example.com";
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> clientService.blockClient(clientEmail));
        assertTrue(exception.getMessage().contains("Client with email " + clientEmail));
        verify(clientRepository).getByEmail(clientEmail);
        verify(blockedClientRepository, never()).save(any());
    }

    @Test
    void unblockClient_ShouldCallRepositoryDelete() {
        // Arrange
        String clientEmail = "test@example.com";

        // Act
        clientService.unblockClient(clientEmail);

        // Assert
        verify(blockedClientRepository).deleteByClient_Email(clientEmail);
    }

    @Test
    void getBlockedClients_WithoutPageable_ShouldReturnListOfClientDTOs() {
        // Arrange
        List<BlockedClient> blockedClients = List.of(blockedClient);
        when(blockedClientRepository.findAll()).thenReturn(blockedClients);
        when(clientMapper.toDto(client)).thenReturn(clientDTO);

        // Act
        List<ClientDTO> result = clientService.getBlockedClients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clientDTO, result.get(0));
        verify(blockedClientRepository).findAll();
        verify(clientMapper).toDto(client);
    }

    @Test
    void getBlockedClients_WithPageable_ShouldReturnPageOfClientDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<BlockedClient> blockedClients = List.of(blockedClient);
        Page<BlockedClient> blockedPage = new PageImpl<>(blockedClients, pageable, 1);
        when(blockedClientRepository.findAll(pageable)).thenReturn(blockedPage);
        when(clientMapper.toDto(client)).thenReturn(clientDTO);

        // Act
        Page<ClientDTO> result = clientService.getBlockedClients(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(clientDTO, result.getContent().get(0));
        verify(blockedClientRepository).findAll(pageable);
        verify(clientMapper).toDto(client);
    }

    @Test
    void getBlockedClients_WithEmptyBlockedRepository_ShouldReturnEmptyList() {
        // Arrange
        when(blockedClientRepository.findAll()).thenReturn(List.of());

        // Act
        List<ClientDTO> result = clientService.getBlockedClients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockedClientRepository).findAll();
        verify(clientMapper, never()).toDto(any());
    }
}