package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClientService {

    List<ClientDTO> getAllClients();

    Page<ClientDTO> getAllClients(Pageable pageable);

    ClientDTO getClientByEmail(String email);

    ClientDTO updateClientByEmail(String email, ClientDTO client);

    void updateClientByEmail(String email, ClientUpdateDTO client);

    void deleteClientByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    List<ClientDTO> getBlockedClients();

    Page<ClientDTO> getBlockedClients(Pageable pageable);

    void blockClient(String clientEmail);

    void unblockClient(String clientEmail);

    void updateClientPassword(String email, String newPassword);
}
