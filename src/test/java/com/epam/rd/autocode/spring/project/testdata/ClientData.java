package com.epam.rd.autocode.spring.project.testdata;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClientData {
    public static final String CLIENT_EMAIL_1 = "sarah.johnson@email.com";
    public static final String CLIENT_EMAIL_2 = "david.chen@email.com";
    public static final String CLIENT_EMAIL_3 = "emma.wilson@email.com";

    public static List<Client> getClientEnities() {
        return new ArrayList<>(List.of(
                getClientEntity(),
                new Client(2L, CLIENT_EMAIL_2, "passwo*rD923", "David Chen",
                        new BigDecimal("75.50")),
                new Client(3L, CLIENT_EMAIL_3, "password129*R3", "Emma Wilson",
                        new BigDecimal("120.00"))
        ));
    }

    public static Client getClientEntity() {
        return new Client(1L, CLIENT_EMAIL_1, "passwREord1*", "Sarah Johnson",
                new BigDecimal("250.75"));
    }
    public static List<ClientDTO> getClientDTOs() {
        return new ArrayList<>(List.of(
                getClientDTO(),
                new ClientDTO(CLIENT_EMAIL_2,
                        "passwo*rD923",
                        "David Chen",
                        new BigDecimal("75.50")),
                new ClientDTO(CLIENT_EMAIL_3,
                        "password129*R3",
                        "Emma Wilson",
                        new BigDecimal("120.00"))
        ));
    }

    public static ClientDTO getClientDTO() {
        return new ClientDTO(CLIENT_EMAIL_1,
                "passwREord1*",
                "Sarah Johnson",
                new BigDecimal("250.75"));
    }

    public static ClientUpdateDTO getClientUpdateDTO() {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setName("Updated Client Name");
        updateDTO.setBalance(new BigDecimal("500.00"));
        return updateDTO;
    }
}
