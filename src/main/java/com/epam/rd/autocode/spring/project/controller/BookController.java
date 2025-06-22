package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.annotations.BookTitle;
import com.epam.rd.autocode.spring.project.annotations.CorrectSortFields;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.SortableEntity;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    private PaginatedResponseDTO<BookDTO> getPaginatedResponse(Page<BookDTO> page){
        PaginatedResponseDTO<BookDTO> response = new PaginatedResponseDTO<>();
        response.setBooks(page.getContent());
        response.setMeta(new MetaDTO(page));
        return response;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<BookDTO>> getAllBooks
            (@RequestParam(required = false)
             @CorrectSortFields(entityType = SortableEntity.BOOK)
             @PageableDefault(sort = "name") Pageable pageable){
        Page<BookDTO> page = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(getPaginatedResponse(page));
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponseDTO<BookDTO>> getAllBooksWithSearchCondition
            (@RequestParam @Valid SearchBookDTO search,
             @RequestParam(required = false)
             @CorrectSortFields(entityType = SortableEntity.BOOK)
             @PageableDefault(sort = "name") Pageable pageable){
        Page<BookDTO> page = bookService.getAllBooksWithSearchCondition(search, pageable);
        return ResponseEntity.ok(getPaginatedResponse(page));
    }

    @GetMapping("/{name}")
    public ResponseEntity<BookDTO> getBookByName(@BookTitle @PathVariable String name){
        return ResponseEntity.ok(bookService.getBookByName(name));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody BookDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(dto));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PutMapping("/{name}")
    public ResponseEntity<Void> updateBook(@PathVariable @BookTitle String name, @Valid @RequestBody BookDTO dto){
        bookService.updateBookByName(name, dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteBook(@PathVariable @BookTitle String name){
        bookService.deleteBookByName(name);
        return ResponseEntity.noContent().build();
    }
}
