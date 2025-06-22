package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.annotations.CorrectSortFields;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.SortableEntity;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService clientService;
    private final OrderService orderService;

    public ClientController(ClientService clientService, OrderService orderService) {
        this.clientService = clientService;
        this.orderService = orderService;
    }

    private PaginatedResponseDTO<ClientDTO> getPaginatedResponse(Page<ClientDTO> page){
        PaginatedResponseDTO<ClientDTO> response = new PaginatedResponseDTO<>();
        response.setBooks(page.getContent());
        response.setMeta(new MetaDTO(page));
        return response;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<ClientDTO>> getAllClients
            (@RequestParam(required = false)
             @CorrectSortFields(entityType = SortableEntity.CLIENT)
             @PageableDefault(sort = "name") Pageable pageable){
        Page<ClientDTO> page = clientService.getAllClients(pageable);
        return ResponseEntity.ok(getPaginatedResponse(page));
    }

    @PreAuthorize("#email == authentication.name and hasRole('CLIENT')")
    @GetMapping("/{email}")
    public ResponseEntity<ClientDTO> getClientByEmail(@PathVariable @Email String email){
        return ResponseEntity.ok(clientService.getClientByEmail(email));
    }

    @PreAuthorize("#email == authentication.name and hasRole('CLIENT')")
    @GetMapping("{email}/orders")
    public ResponseEntity<PaginatedResponseDTO<OrderDTO>> getOrdersByClient
            (@PathVariable @Email String email,
             @RequestParam(required = false)
             @CorrectSortFields(entityType = SortableEntity.ORDER)
             @PageableDefault(sort = "orderDate") Pageable pageable){
        Page<OrderDTO> page = orderService.getOrdersByClient(email, pageable);
        PaginatedResponseDTO<OrderDTO> response = new PaginatedResponseDTO<>();
        response.setOrders(page.getContent());
        response.setMeta(new MetaDTO(page));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("#email == authentication.name and hasRole('CLIENT')")
    @PutMapping("/{email}")
    public ResponseEntity<Void> updateClient
            (@PathVariable @Email String email,
             @Valid @RequestBody ClientUpdateDTO dto){
        clientService.updateClientByEmail(email, dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#email == authentication.name and hasRole('CLIENT')")
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteClient(@PathVariable @Email String email){
        clientService.deleteClientByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocked")
    public ResponseEntity<PaginatedResponseDTO<ClientDTO>> getBlockedClients(@RequestParam(required = false)
                                                                             @CorrectSortFields(entityType = SortableEntity.CLIENT)
                                                                             @PageableDefault(sort = "name") Pageable pageable){
        Page<ClientDTO> page = clientService.getBlockedClients(pageable);
        return ResponseEntity.ok(getPaginatedResponse(page));
    }

    @PostMapping("/blocked/{email}")
    public ResponseEntity<String> blockClient(@PathVariable @Email String email){
        clientService.blockClient(email);
        return ResponseEntity.ok(email);
    }

    @DeleteMapping("/blocked/{email}")
    public ResponseEntity<Void> unblockClient(@PathVariable @Email String email){
        clientService.unblockClient(email);
        return ResponseEntity.noContent().build();
    }
}
