package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class OrderMapper {

    private final ModelMapper mapper;
    private final BookItemMapper bookItemMapper;

    public OrderMapper(ModelMapper mapper, BookItemMapper bookItemMapper) {
        this.mapper = mapper;
        this.bookItemMapper = bookItemMapper;

        this.mapper.createTypeMap(Order.class, OrderDTO.class).addMappings(m -> {
            m.map(src -> src.getEmployee() != null ? src.getEmployee().getEmail() : null,
                    OrderDTO::setEmployeeEmail);
            m.map(src -> src.getClient() != null ? src.getClient().getEmail() : null,
                    OrderDTO::setClientEmail);
        });
    }

    public OrderDTO toDto(Order order) {
        OrderDTO dto = mapper.map(order, OrderDTO.class);

        if (order.getBookItems() != null && !order.getBookItems().isEmpty()) {
            List<BookItemDTO> bookItemDTOs = order.getBookItems().stream()
                    .map(bookItemMapper::toDto)
                    .toList();
            dto.setBookItems(bookItemDTOs);
        }

        return dto;
    }

    public Order toEntity(OrderDTO dto) {
        return mapper.map(dto, Order.class);
    }
}
