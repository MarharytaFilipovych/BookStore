package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.OrderMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, EmployeeRepository employeeRepository, ClientRepository clientRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByClientNotNullAndEmployeeNotNull(pageable).map(orderMapper::toDto);
    }

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return orderRepository.findAllByClient_Email(clientEmail).stream().map(orderMapper::toDto).toList();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return orderRepository.findAllByEmployee_Email(employeeEmail).stream().map(orderMapper::toDto).toList();
    }

    @Override
    public OrderDTO addOrder(OrderDTO dto) {
        Order order = orderMapper.toEntity(dto);
        Employee employee = employeeRepository.getByEmail(dto.getEmployeeEmail())
                .orElseThrow(()-> new NotFoundException("Employee with email " + dto.getEmployeeEmail()));
        Client client = clientRepository.getByEmail(dto.getClientEmail())
                .orElseThrow(() -> new NotFoundException("Client with email " + dto.getClientEmail()));
        order.setEmployee(employee);
        order.setClient(client);
        return orderMapper.toDto(orderRepository.save(order));
    }
}
