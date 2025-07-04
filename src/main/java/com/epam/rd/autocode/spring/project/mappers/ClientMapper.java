package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    private final ModelMapper mapper;

    public ClientMapper(ModelMapper mapper) {
        this.mapper = mapper;
        mapper.createTypeMap(ClientDTO.class, Client.class).addMappings(m ->
           m.skip(Client::setId)
        );
        mapper.createTypeMap(ClientUpdateDTO.class, Client.class)
                .addMappings(mapping -> {
                    mapping.skip(Client::setId);
                    mapping.skip(Client::setPassword);
                    mapping.skip(Client::setEmail);
                });
    }

    public ClientDTO toDto(Client client){
        return mapper.map(client, ClientDTO.class);
    }

    public Client toEntity(ClientDTO dto){
        return mapper.map(dto, Client.class);
    }

    public Client toEntity(ClientUpdateDTO dto){
        return mapper.map(dto, Client.class);
    }
}
