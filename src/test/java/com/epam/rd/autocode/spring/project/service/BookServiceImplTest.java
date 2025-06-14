package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static com.epam.rd.autocode.spring.project.testdata.BookData.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;
    private List<Book> books;
    private List<BookDTO> bookDTOs;

    @BeforeEach
    void setUp(){
        book = getBookEntity();
        bookDTO = getBookDTO();
        books = getBookEntities();
        bookDTOs = getBookDTOs();
    }

    @Test
    void getAllBooks_WithPageable_ShouldReturnPageOfBookDTOs(){
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(books.size(), result.getTotalElements());
        assertEquals(bookDTOs, result.getContent());
        verify(bookRepository).findAll(pageable);
        verify(bookMapper, times(books.size())).toDto(any(Book.class));
    }

    @Test
    void getAllBooks_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertNotNull(capturedPageable.getSort());
        assertTrue(capturedPageable.getSort().isSorted());
        Sort.Order nameOrder = capturedPageable.getSort().getOrderFor("name");
        assertNotNull(nameOrder);
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());
        assertEquals("name", nameOrder.getProperty());
    }

    @Test
    void getAllBooks_WithSortByPriceDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order priceOrder = capturedPageable.getSort().getOrderFor("price");
        assertNotNull(priceOrder);
        assertEquals(Sort.Direction.DESC, priceOrder.getDirection());
        assertEquals("price", priceOrder.getProperty());
    }

    @Test
    void getAllBooks_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange
        Sort multiSort = Sort.by("name").and(Sort.by("price").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort capturedSort = capturedPageable.getSort();

        List<Sort.Order> orders = capturedSort.toList();
        assertEquals(2, orders.size());

        Sort.Order nameOrder = orders.get(0);
        assertEquals("name", nameOrder.getProperty());
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());

        Sort.Order priceOrder = orders.get(1);
        assertEquals("price", priceOrder.getProperty());
        assertEquals(Sort.Direction.DESC, priceOrder.getDirection());
    }


    @Test
    void getAllBooks_WithoutPageable_ShouldReturnListOfBookDTOs() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(books);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        List<BookDTO> result = bookService.getAllBooks();

        // Assert
        assertNotNull(result);
        assertEquals(books.size(), result.size());
        assertEquals(bookDTOs, result, "Result should equal expected DTOs");
        verify(bookRepository).findAll();
        verify(bookMapper, times(books.size())).toDto(any(Book.class));
    }

    @Test
    void getAllBooks_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(List.of());

        // Act
        List<BookDTO> result = bookService.getAllBooks();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    void getAllBooks_WithPageable_WhenEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(bookRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(bookRepository).findAll(pageable);
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    void updateBookByName_WhenBookExists_ShouldReturnUpdatedBookDTO() {
        // Arrange
        BookDTO updateData = getBookDTO();
        updateData.setName("New Name");

        Book updatedBook = getBookEntity();
        updatedBook.setName("New Name");

        when(bookRepository.findByName(book.getName())).thenReturn(Optional.of(book));
        when(bookMapper.toEntity(updateData)).thenReturn(updatedBook);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            assertEquals(book.getId(), saved.getId());
            return saved;
        });
        when(bookMapper.toDto(updatedBook)).thenReturn(updateData);

        // Act
        BookDTO result = bookService.updateBookByName(book.getName(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(updateData, result);
        verify(bookRepository).findByName(book.getName());
        verify(bookMapper).toEntity(updateData);
        verify(bookRepository).save(argThat(book -> book.getId().equals(this.book.getId())));
        verify(bookMapper).toDto(updatedBook);
    }

    @Test
    void updateBookByName_WhenBookDoesNotExist_ShouldThrowNotFoundException(){
        // Arrange
        String bookName = "Non-existent Book";
        when(bookRepository.findByName(bookName)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException e = assertThrows(NotFoundException.class,
                ()-> bookService.updateBookByName(bookName, any(BookDTO.class)));
        assertEquals("The book with a name " + bookName + " was not found!", e.getMessage());
        verify(bookRepository).findByName(bookName);
        verify(bookMapper, never()).toEntity(any());
        verify(bookRepository, never()).save(any());
    }


    @Test
    void deleteBookByName_ShouldCallRepositoryDelete() {
        // Arrange
        String bookName = "Test Book";

        // Act
        bookService.deleteBookByName(bookName);

        // Assert
        verify(bookRepository).deleteByName(bookName);
    }

    @Test
    void addBook_WhenBookIsValid_ShouldReturnSavedBookDTO() {
        // Arrange
        when(bookMapper.toEntity(bookDTO)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        // Act
        BookDTO result = bookService.addBook(bookDTO);

        // Assert
        assertNotNull(result);
        assertEquals(bookDTO, result);
        verify(bookMapper).toEntity(bookDTO);
        verify(bookRepository).save(book);
        verify(bookMapper).toDto(book);
    }

    @Test
    void addBook_WhenBookAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        when(bookMapper.toEntity(bookDTO)).thenReturn(book);
        when(bookRepository.save(book)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        AlreadyExistException exception = assertThrows(AlreadyExistException.class,
                () -> bookService.addBook(bookDTO));

        assertEquals("The book with a name " + bookDTO.getName() + " already exists!", exception.getMessage());
        verify(bookMapper).toEntity(bookDTO);
        verify(bookRepository).save(book);
    }
}

