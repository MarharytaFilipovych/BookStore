package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    private final ModelMapper mapper;

    public OrderMapper(ModelMapper mapper) {
        this.mapper = mapper;
        mapper.typeMap(Order.class, OrderDTO.class).addMappings(m -> {
            m.map(src -> src.getEmployee() != null ? src.getEmployee().getEmail() : null,
                    OrderDTO::setEmployeeEmail);
            m.map(src -> src.getClient() != null ? src.getClient().getEmail() : null,
                    OrderDTO::setClientEmail);
        });
        mapper.typeMap(OrderDTO.class, Order.class).addMappings(m ->{
            m.skip(Order::setEmployee);
            m.skip(Order::setClient);
            m.skip(Order::setId);
            m.skip(Order::setBookItems);
        });
    }

    public OrderDTO toDto(Order order){
       return mapper.map(order, OrderDTO.class);
    }

    public Order toEntity(OrderDTO dto){
        return mapper.map(dto, Order.class);
    }
}
