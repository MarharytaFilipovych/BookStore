package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.ClientMapper;
import com.epam.rd.autocode.spring.project.model.BlockedClient;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final BlockedClientRepository blockedClientRepository;

    public ClientServiceImpl(ClientRepository clientRepository, ClientMapper mapper, BlockedClientRepository blockedClientRepository) {
        this.clientRepository = clientRepository;
        this.clientMapper = mapper;
        this.blockedClientRepository = blockedClientRepository;
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
        return blockedClientRepository.findAll(pageable)
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
    public Page<ClientDTO> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable).map(clientMapper::toDto);
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
    public void deleteClientByEmail(String email) {
        unblockClient(email);
        clientRepository.deleteByEmail(email);
    }

    @Override
    public ClientDTO addClient(ClientDTO client) {
        try{
            return clientMapper.toDto(clientRepository.save(clientMapper.toEntity(client)));
        }catch (DataIntegrityViolationException e){
            throw new AlreadyExistException("Client with email " + client.getEmail());
        }
    }
}
