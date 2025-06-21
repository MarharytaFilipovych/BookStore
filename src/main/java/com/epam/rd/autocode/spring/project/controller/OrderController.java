package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.annotations.CorrectSortFields;
import com.epam.rd.autocode.spring.project.dto.MetaDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.PaginatedResponseDTO;
import com.epam.rd.autocode.spring.project.model.enums.SortableEntity;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<OrderDTO>> getAllOrders
            (@RequestParam(required = false)
             @CorrectSortFields(entityType = SortableEntity.ORDER)
             @PageableDefault(sort = "orderDate") Pageable pageable){
        Page<OrderDTO> page = orderService.getAllOrders(pageable);
        PaginatedResponseDTO<OrderDTO> response = new PaginatedResponseDTO<>();
        response.setOrders(page.getContent());
        response.setMeta(new MetaDTO(page));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> addOrder(@Valid @RequestBody OrderDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addOrder(dto));
    }

    @PutMapping()
    public ResponseEntity<Void> confirmOrder(@Valid @RequestBody OrderDTO dto){
        orderService.confirmOrder(dto);
        return ResponseEntity.noContent().build();
    }
}
