package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ClientMapper clientMapper;
    @Mock private BlockedClientRepository blockedClientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SortMappingService sortMappingService;

    @InjectMocks
    private ClientServiceImpl clientService;

    private List<Client> clients;
    private List<ClientDTO> clientDTOs;
    private Client client;
    private ClientDTO clientDTO;
    private BlockedClient blockedClient;

    @BeforeEach
    public void setUp() {
        client = getClientEntity();
        clientDTO = getClientDTO();
        clients = getClientEnities();
        clientDTOs = getClientDTOs();
        blockedClient = new BlockedClient(client);
    }

    private void mockPageableClientOperations(Pageable pageable, Pageable mappedPageable,
                                              List<Client> clientList, List<ClientDTO> clientDTOList) {
        Page<Client> clientPage = new PageImpl<>(clientList, mappedPageable, clientList.size());

        when(sortMappingService.applyMappings(pageable, "client")).thenReturn(mappedPageable);
        when(clientRepository.findAll(mappedPageable)).thenReturn(clientPage);

        for (int i = 0; i < clientList.size(); i++) {
            when(clientMapper.toDto(clientList.get(i))).thenReturn(clientDTOList.get(i));
        }
    }

    private void mockSimpleClientListOperations(List<Client> clientList, List<ClientDTO> clientDTOList) {
        when(clientRepository.findAll()).thenReturn(clientList);
        for (int i = 0; i < clientList.size(); i++) {
            when(clientMapper.toDto(clientList.get(i))).thenReturn(clientDTOList.get(i));
        }
    }

    private void mockEmptyPageableRepository(Pageable pageable, Pageable mappedPageable) {
        Page<Client> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);
        when(sortMappingService.applyMappings(pageable, "client")).thenReturn(mappedPageable);
        when(clientRepository.findAll(mappedPageable)).thenReturn(emptyPage);
    }

    private void mockSuccessfulClientUpdate(ClientDTO updateData, Client updatedClient) {
        when(clientMapper.toEntity(updateData)).thenReturn(updatedClient);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client saved = invocation.getArgument(0);
            assertEquals(client.getId(), saved.getId());
            return saved;
        });
        when(clientMapper.toDto(updatedClient)).thenReturn(updateData);
    }

    private void mockSuccessfulClientUpdateDTO(ClientUpdateDTO updateData, Client mappedClient) {
        when(clientMapper.toEntity(updateData)).thenReturn(mappedClient);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void mockClientCreation(ClientDTO dto, Client clientToSave, Client savedClient,
                                    String rawPassword, String encodedPassword) {
        when(clientMapper.toEntity(dto)).thenReturn(clientToSave);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        when(clientRepository.save(clientToSave)).thenReturn(savedClient);
        when(clientMapper.toDto(savedClient)).thenReturn(dto);
    }

    private void mockClientCreationFailure(ClientDTO dto, Client clientToSave) {
        when(clientMapper.toEntity(dto)).thenReturn(clientToSave);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clientRepository.save(clientToSave)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));
    }

    private void mockBlockedClientOperations(List<BlockedClient> blockedClients, List<ClientDTO> expectedDTOs) {
        when(blockedClientRepository.findAll()).thenReturn(blockedClients);
        for (int i = 0; i < blockedClients.size(); i++) {
            when(clientMapper.toDto(blockedClients.get(i).getClient())).thenReturn(expectedDTOs.get(i));
        }
    }

    private void mockPageableBlockedClientOperations(Pageable pageable, List<BlockedClient> blockedClients,
                                                     List<ClientDTO> expectedDTOs) {
        Page<BlockedClient> blockedPage = new PageImpl<>(blockedClients, pageable, blockedClients.size());
        when(blockedClientRepository.findAll(pageable)).thenReturn(blockedPage);
        for (int i = 0; i < blockedClients.size(); i++) {
            when(clientMapper.toDto(blockedClients.get(i).getClient())).thenReturn(expectedDTOs.get(i));
        }
    }

    private void mockEmptyPageableBlockedClientRepository(Pageable pageable) {
        Page<BlockedClient> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(blockedClientRepository.findAll(pageable)).thenReturn(emptyPage);
    }

    private void verifyPageableOperations(Pageable originalPageable, Pageable mappedPageable) {
        verify(sortMappingService).applyMappings(originalPageable, "client");
        verify(clientRepository).findAll(mappedPageable);
    }

    private void verifyPagedResults(Page<ClientDTO> result, List<ClientDTO> expectedContent, int expectedTotalElements) {
        assertNotNull(result);
        assertEquals(expectedTotalElements, result.getTotalElements());
        assertEquals(expectedContent, result.getContent());
    }

    private void verifyListResults(List<ClientDTO> result, List<ClientDTO> expectedContent) {
        assertNotNull(result);
        assertEquals(expectedContent.size(), result.size());
        assertEquals(expectedContent, result);
    }

    private void verifyEmptyResults(Page<ClientDTO> result) {
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    private void verifyEmptyListResults(List<ClientDTO> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private void verifyClientUpdateOperations(String email, ClientDTO updateData, Client updatedClient) {
        verify(clientRepository).getByEmail(email);
        verify(clientMapper).toEntity(updateData);
        verify(clientRepository).save(argThat(c -> c.getId().equals(client.getId())));
        verify(clientMapper).toDto(updatedClient);
    }

    private void verifyClientUpdateDTOOperations(String email, ClientUpdateDTO updateData,
                                                 String expectedName, BigDecimal expectedBalance) {
        verify(clientRepository).getByEmail(email);
        verify(clientMapper).toEntity(updateData);

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());

        Client savedClient = clientCaptor.getValue();
        assertEquals(client.getId(), savedClient.getId());
        assertEquals(client.getEmail(), savedClient.getEmail());
        assertEquals(expectedName, savedClient.getName());
        assertEquals(expectedBalance, savedClient.getBalance());
    }

    private void verifyNotFoundScenario(String email) {
        Exception exception = assertThrows(NotFoundException.class,
                () -> clientService.getClientByEmail(email));
        assertTrue(exception.getMessage().contains("Client with email " + email));
        verify(clientRepository).getByEmail(email);
        verify(clientMapper, never()).toDto(any());
    }

    private void verifyClientCreationOperations(ClientDTO dto, String rawPassword) {
        verify(clientMapper).toEntity(dto);
        verify(passwordEncoder).encode(rawPassword);
        verify(clientRepository).save(any(Client.class));
        verify(clientMapper).toDto(any(Client.class));
    }

    private void verifyPasswordUpdateOperations(String email, String newPassword, String encodedPassword) {
        verify(clientRepository).getByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(clientRepository).save(client);
        assertEquals(encodedPassword, client.getPassword());
    }

    private void testSortingScenario(Sort sort) {
        Pageable pageable = PageRequest.of(0, 10, sort);
        Pageable mappedPageable = PageRequest.of(0, 10, sort);

        mockPageableClientOperations(pageable, mappedPageable, clients, clientDTOs);

        // Act
        clientService.getAllClients(pageable);

        // Assert
        verifyPageableOperations(pageable, mappedPageable);
    }

    @Test
    void getAllClients_WithPageable_ShouldReturnPageOfClientDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockPageableClientOperations(pageable, mappedPageable, clients, clientDTOs);

        // Act
        Page<ClientDTO> result = clientService.getAllClients(pageable);

        // Assert
        verifyPagedResults(result, clientDTOs, clients.size());
        verifyPageableOperations(pageable, mappedPageable);
        verify(clientMapper, times(clients.size())).toDto(any(Client.class));
    }

    @Test
    void getAllClients_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("name"));
    }

    @Test
    void getAllClients_WithSortByBalanceDesc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("balance").descending());
    }

    @Test
    void getAllClients_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        Sort multiSort = Sort.by("name").ascending().and(Sort.by("balance").descending());
        testSortingScenario(multiSort);
    }

    @Test
    void getAllClients_WithoutPageable_ShouldReturnListOfClientDTOs() {
        // Arrange
        mockSimpleClientListOperations(clients, clientDTOs);

        // Act
        List<ClientDTO> result = clientService.getAllClients();

        // Assert
        verifyListResults(result, clientDTOs);
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
        verifyEmptyListResults(result);
        verify(clientRepository).findAll();
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void getAllClients_WithPageable_WhenEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockEmptyPageableRepository(pageable, mappedPageable);

        // Act
        Page<ClientDTO> result = clientService.getAllClients(pageable);

        // Assert
        verifyEmptyResults(result);
        verify(clientRepository).findAll(mappedPageable);
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void updateClientByEmailWithClientDTO_WhenClientExists_ShouldReturnUpdatedClientDTO() {
        // Arrange
        ClientDTO updateData = getClientDTO();
        updateData.setName("New Name");
        Client updatedClient = getClientEntity();
        updatedClient.setName("New Name");

        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        mockSuccessfulClientUpdate(updateData, updatedClient);

        // Act
        ClientDTO result = clientService.updateClientByEmail(client.getEmail(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(updateData, result);
        verifyClientUpdateOperations(client.getEmail(), updateData, updatedClient);
    }

    @Test
    void updateClientByEmailWithClientDTO_WhenClientDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String clientEmail = "nonexistent@example.com";
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> clientService.updateClientByEmail(clientEmail, clientDTO));

        assertTrue(exception.getMessage().contains("Client with email " + clientEmail));
        verify(clientRepository).getByEmail(clientEmail);
        verify(clientMapper, never()).toEntity(any(ClientDTO.class));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClientByEmailWithClientUpdateDTO_WhenClientExists_ShouldUpdateClientAndSave() {
        // Arrange
        ClientUpdateDTO updateData = new ClientUpdateDTO();
        updateData.setName("Updated Name");
        updateData.setBalance(new BigDecimal("500.00"));

        Client mappedClient = new Client();
        mappedClient.setName("Updated Name");
        mappedClient.setBalance(new BigDecimal("500.00"));

        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        mockSuccessfulClientUpdateDTO(updateData, mappedClient);

        // Act
        clientService.updateClientByEmail(client.getEmail(), updateData);

        // Assert
        verifyClientUpdateDTOOperations(client.getEmail(), updateData, "Updated Name", new BigDecimal("500.00"));
    }

    @Test
    void updateClientByEmailWithClientUpdateDTO_WhenClientDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String clientEmail = "nonexistent@example.com";
        ClientUpdateDTO updateData = new ClientUpdateDTO();
        updateData.setName("Updated Name");
        updateData.setBalance(new BigDecimal("500.00"));
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> clientService.updateClientByEmail(clientEmail, updateData));

        assertTrue(exception.getMessage().contains("Client with email " + clientEmail));
        verify(clientRepository).getByEmail(clientEmail);
        verify(clientMapper, never()).toEntity(any(ClientUpdateDTO.class));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClientByEmailWithClientUpdateDTO_ShouldPreserveOriginalIdAndEmail() {
        // Arrange
        ClientUpdateDTO updateData = new ClientUpdateDTO();
        updateData.setName("New Name");
        updateData.setBalance(new BigDecimal("999.99"));

        Client mappedClient = new Client();
        mappedClient.setName("New Name");
        mappedClient.setBalance(new BigDecimal("999.99"));

        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        mockSuccessfulClientUpdateDTO(updateData, mappedClient);

        // Act
        clientService.updateClientByEmail(client.getEmail(), updateData);

        // Assert
        verifyClientUpdateDTOOperations(client.getEmail(), updateData, "New Name", new BigDecimal("999.99"));
    }

    @Test
    void deleteClientByEmail_ShouldCallRepositoryDeleteAndUnblock() {
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
        String rawPassword = clientDTO.getPassword();
        String encodedPassword = "$2a$10$encodedPassword";
        Client clientToSave = getClientEntity();
        clientToSave.setPassword(rawPassword);
        Client savedClient = getClientEntity();
        savedClient.setPassword(encodedPassword);
        mockClientCreation(clientDTO, clientToSave, savedClient, rawPassword, encodedPassword);

        // Act
        ClientDTO result = clientService.addClient(clientDTO);

        // Assert
        assertNotNull(result);
        assertEquals(clientDTO, result);
        verifyClientCreationOperations(clientDTO, rawPassword);
        assertEquals(encodedPassword, clientToSave.getPassword());
    }

    @Test
    void addClient_WhenClientAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        Client clientToSave = getClientEntity();
        mockClientCreationFailure(clientDTO, clientToSave);

        // Act & Assert
        AlreadyExistException exception = assertThrows(AlreadyExistException.class,
                () -> clientService.addClient(clientDTO));

        assertTrue(exception.getMessage().contains("Client with email " + clientDTO.getEmail()));
        verify(clientMapper).toEntity(clientDTO);
        verify(passwordEncoder).encode(anyString());
        verify(clientRepository).save(clientToSave);
    }

    @Test
    void getClientByEmail_WhenClientExists_ShouldReturnClientDTO() {
        // Arrange
        String clientEmail = client.getEmail();
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
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
        verifyNotFoundScenario(clientEmail);
    }

    @Test
    void blockClient_WhenClientExists_ShouldCreateBlockedClient() {
        // Arrange
        String clientEmail = client.getEmail();
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
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
        mockBlockedClientOperations(blockedClients, List.of(clientDTO));

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
        mockPageableBlockedClientOperations(pageable, blockedClients, List.of(clientDTO));

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
        verifyEmptyListResults(result);
        verify(blockedClientRepository).findAll();
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void getBlockedClients_WithPageable_WhenEmptyBlockedRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        mockEmptyPageableBlockedClientRepository(pageable);

        // Act
        Page<ClientDTO> result = clientService.getBlockedClients(pageable);

        // Assert
        verifyEmptyResults(result);
        verify(blockedClientRepository).findAll(pageable);
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void updateClientPassword_WhenClientExists_ShouldUpdatePassword() {
        // Arrange
        String clientEmail = client.getEmail();
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newEncodedPassword";

        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(clientRepository.save(client)).thenReturn(client);

        // Act
        clientService.updateClientPassword(clientEmail, newPassword);

        // Assert
        verifyPasswordUpdateOperations(clientEmail, newPassword, encodedPassword);
    }

    @Test
    void updateClientPassword_WhenClientDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String clientEmail = "nonexistent@example.com";
        String newPassword = "newPassword123";
        when(clientRepository.getByEmail(clientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> clientService.updateClientPassword(clientEmail, newPassword));

        assertTrue(exception.getMessage().contains("Client with email " + clientEmail));
        verify(clientRepository).getByEmail(clientEmail);
        verify(passwordEncoder, never()).encode(anyString());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void addClient_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "plainTextPassword";
        String encodedPassword = "$2a$10$encodedHashedPassword";

        ClientDTO dtoWithPlainPassword = getClientDTO();
        dtoWithPlainPassword.setPassword(rawPassword);
        Client clientEntity = getClientEntity();
        clientEntity.setPassword(rawPassword);

        when(clientMapper.toEntity(dtoWithPlainPassword)).thenReturn(clientEntity);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client saved = invocation.getArgument(0);
            assertEquals(encodedPassword, saved.getPassword());
            return saved;
        });
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDTO);

        // Act
        clientService.addClient(dtoWithPlainPassword);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
        verify(clientRepository).save(argThat(c -> encodedPassword.equals(c.getPassword())));
    }

    @Test
    void getAllClients_WithPageable_ShouldUseMappedPageable() {
        // Arrange
        Pageable originalPageable = PageRequest.of(0, 10, Sort.by("balance"));
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("balance"));
        mockPageableClientOperations(originalPageable, mappedPageable, clients, clientDTOs);

        // Act
        Page<ClientDTO> result = clientService.getAllClients(originalPageable);

        // Assert
        assertNotNull(result);
        verifyPageableOperations(originalPageable, mappedPageable);
    }
}