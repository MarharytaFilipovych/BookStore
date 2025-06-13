package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return List.of();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return List.of();
    }

    @Override
    public OrderDTO addOrder(OrderDTO order) {
        return null;
    }
}
