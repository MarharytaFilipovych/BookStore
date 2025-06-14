package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.model.BookItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class BookItemMapper {
    private final ModelMapper mapper;

    public BookItemMapper(ModelMapper mapper) {
        this.mapper = mapper;

        mapper.createTypeMap(BookItem.class, BookItemDTO.class)
                .addMapping(bookItem -> bookItem.getBook().getName(), BookItemDTO::setBookName);
    }

    public BookItemDTO toDto(BookItem bookItem){
        return mapper.map(bookItem, BookItemDTO.class);
    }

    public BookItem toEntity(BookItemDTO dto){
        return mapper.map(dto, BookItem.class);
    }
}