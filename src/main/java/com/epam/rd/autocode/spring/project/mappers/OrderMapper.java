package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class OrderMapper {

    private final BookItemMapper bookItemMapper;

    public OrderMapper(BookItemMapper bookItemMapper) {
        this.bookItemMapper = bookItemMapper;
    }

    public OrderDTO toDto(Order order) {
        OrderDTO dto = new OrderDTO();

        dto.setOrderDate(order.getOrderDate());
        dto.setPrice(order.getPrice());
        dto.setEmployeeEmail(order.getEmployee() != null ? order.getEmployee().getEmail() : null);
        dto.setClientEmail(order.getClient() != null ? order.getClient().getEmail() : null);

        if (order.getBookItems() != null && !order.getBookItems().isEmpty()) {
            List<BookItemDTO> bookItemDTOs = order.getBookItems().stream()
                    .map(bookItemMapper::toDto)
                    .toList();
            dto.setBookItems(bookItemDTOs);
        }
        else dto.setBookItems(new ArrayList<>());
        return dto;
    }

    public Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setOrderDate(dto.getOrderDate());
        order.setPrice(dto.getPrice());
        order.setBookItems(new ArrayList<>());
        return order;
    }
}