package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.annotations.CorrectSortFields;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final OrderService orderService;

    public EmployeeController(EmployeeService employeeService, OrderService orderService) {
        this.employeeService = employeeService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<EmployeeDTO>> getAllEmployees
            (@CorrectSortFields(entityType = "employee")
             @RequestParam(required = false)
             @PageableDefault(sort = "name") Pageable pageable){
        Page<EmployeeDTO> page = employeeService.getAllEmployees(pageable);
        PaginatedResponseDTO<EmployeeDTO> response = new PaginatedResponseDTO<>();
        response.setEmployees(page.getContent());
        response.setMeta(new MetaDTO(page));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable @Email String email){
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(email));
    }

    @GetMapping("/{email}/orders")
    public ResponseEntity<PaginatedResponseDTO<OrderDTO>> getOrdersByEmployee
            (@PathVariable @Email String email,
             @RequestParam(required = false)
             @CorrectSortFields(entityType = "order")
             @PageableDefault(sort = "orderDate") Pageable pageable){
        Page<OrderDTO> page = orderService.getOrdersByEmployee(email, pageable);
        PaginatedResponseDTO<OrderDTO> response = new PaginatedResponseDTO<>();
        response.setOrders(page.getContent());
        response.setMeta(new MetaDTO(page));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> addEmployee(@Valid @RequestBody EmployeeDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.addEmployee(dto));
    }

    @PutMapping("/{email}")
    public ResponseEntity<Void> updateEmployee(@PathVariable @Email String email, @Valid @RequestBody EmployeeUpdateDTO dto){
        employeeService.updateEmployeeByEmail(email, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable @Email String email){
        employeeService.deleteEmployeeByEmail(email);
        return ResponseEntity.noContent().build();
    }
}
