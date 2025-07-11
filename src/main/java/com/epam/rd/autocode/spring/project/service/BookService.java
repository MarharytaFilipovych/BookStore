package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.SearchBookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookService {

    Page<BookDTO> getAllBooks(Pageable pageable);

    List<BookDTO> getAllBooks();

    Page<BookDTO> getAllBooksWithSearchCondition(SearchBookDTO dto, Pageable pageable);

    BookDTO getBookByName(String name);

    BookDTO updateBookByName(String name, BookDTO book);

    void deleteBookByName(String name);

    BookDTO addBook(BookDTO book);
}
