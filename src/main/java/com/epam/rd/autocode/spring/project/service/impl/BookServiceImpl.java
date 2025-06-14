package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toDto);
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(bookMapper::toDto).toList();
    }

    @Override
    public BookDTO getBookByName(String name) {
        Optional<Book> book = bookRepository.findByName(name);
        return book.map(bookMapper::toDto).
                orElseThrow(()-> new NotFoundException("The book with a name " + name));
    }

    @Override
    public BookDTO updateBookByName(String name, BookDTO book) {
        return bookRepository.findByName(name).map(existingBook -> {
            Book updated = bookMapper.toEntity(book);
            updated.setId(existingBook.getId());
            return bookMapper.toDto(bookRepository.save(updated));
        }).orElseThrow(() -> new NotFoundException("The book with a name " + name + " was not found"));
    }

    @Override
    public void deleteBookByName(String name) {
        bookRepository.deleteByName(name);
    }

    @Override
    public BookDTO addBook(BookDTO book) {
        try{
            return bookMapper.toDto(bookRepository.save(bookMapper.toEntity(book)));
        }catch (DataIntegrityViolationException e){
            throw new AlreadyExistException("The book with a name " + book.getName());
        }
    }
}
