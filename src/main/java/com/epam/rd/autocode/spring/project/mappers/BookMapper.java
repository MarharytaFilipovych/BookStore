package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    private final ModelMapper mapper;

    public BookMapper(ModelMapper mapper) {
        this.mapper = mapper;
        mapper.typeMap(BookDTO.class, Book.class)
                .addMappings(m ->
                        m.skip(Book::setId));
    }

    public BookDTO toDto(Book book){
        return mapper.map(book, BookDTO.class);

    }

    public Book toEntity(BookDTO dto){
        return mapper.map(dto, Book.class);
    }
}
