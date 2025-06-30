package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.ClientMapper;
import com.epam.rd.autocode.spring.project.model.BlockedClient;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRefreshTokenRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.SortMappingService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final BlockedClientRepository blockedClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final SortMappingService sortMappingService;
    private final ClientRefreshTokenRepository clientRefreshTokenRepository;

    public ClientServiceImpl(ClientRepository clientRepository, ClientMapper mapper, BlockedClientRepository blockedClientRepository, PasswordEncoder passwordEncoder, SortMappingService sortMappingService, ClientRefreshTokenRepository clientRefreshTokenRepository) {
        this.clientRepository = clientRepository;
        this.clientMapper = mapper;
        this.blockedClientRepository = blockedClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.sortMappingService = sortMappingService;
        this.clientRefreshTokenRepository = clientRefreshTokenRepository;
    }

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream().map(clientMapper::toDto).toList();
    }

    @Override
    public List<ClientDTO> getBlockedClients() {
        return blockedClientRepository.findAll().stream()
                .map(b -> clientMapper.toDto(b.getClient())).toList();
    }

    @Override
    public Page<ClientDTO> getBlockedClients(Pageable pageable) {
        Pageable mappedPageable = sortMappingService.applyMappings(pageable, "blocked_client");
        return blockedClientRepository.findAll(mappedPageable)
                .map(b -> clientMapper.toDto(b.getClient()));

    }

    @Override
    public void blockClient(String clientEmail) {
        Client client = clientRepository.getByEmail(clientEmail)
                .orElseThrow(()-> new NotFoundException("Client with email " + clientEmail));
        blockedClientRepository.save(new BlockedClient(client));
    }

    @Override
    public void unblockClient(String clientEmail) {
        blockedClientRepository.deleteByClient_Email(clientEmail);
    }

    @Override
    public boolean isBlocked(String clientEmail){
        return blockedClientRepository.existsByClient_Email(clientEmail);
    }

    @Override
    public Page<ClientDTO> getAllClients(Pageable pageable) {
        Pageable mappedPageable = sortMappingService.applyMappings(pageable, "client");
        return clientRepository.findAll(mappedPageable).map(clientMapper::toDto);
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        return clientRepository.getByEmail(email).map(clientMapper::toDto)
                .orElseThrow(()-> new NotFoundException("Client with email " + email));
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        return clientRepository.getByEmail(email)
                .map(existingClient -> {
                    Client updatedClient = clientMapper.toEntity(client);
                    updatedClient.setId(existingClient.getId());
                    return clientMapper.toDto(clientRepository.save(updatedClient));
                })
                .orElseThrow(() -> new NotFoundException("Client with email " + email));
    }

    @Override
    public void updateClientByEmail(String email, ClientUpdateDTO client) {
        clientRepository.getByEmail(email)
                .ifPresentOrElse(
                        existingClient -> {
                            Client updatedClient = clientMapper.toEntity(client);
                            updatedClient.setId(existingClient.getId());
                            updatedClient.setEmail(existingClient.getEmail());
                            updatedClient.setPassword(existingClient.getPassword());
                            clientRepository.save(updatedClient);
                        },
                        () -> {throw new NotFoundException("Client with email " + email);}
                );
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        unblockClient(email);
        clientRefreshTokenRepository.deleteClientRefreshTokensByClient_Email(email);
        clientRepository.deleteByEmail(email);
    }

    @Override
    public ClientDTO addClient(ClientDTO dto) {
        try{
            Client client = clientMapper.toEntity(dto);
            client.setPassword(passwordEncoder.encode(dto.getPassword()));
            return clientMapper.toDto(clientRepository.save(client));
        }catch (DataIntegrityViolationException e){
            throw new AlreadyExistException("Client with email " + dto.getEmail());
        }
    }

    @Override
    public void updateClientPassword(String email, String newPassword) {
        Client client = clientRepository.getByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client with email " + email));

        client.setPassword(passwordEncoder.encode(newPassword));
        clientRepository.save(client);
    }
}
