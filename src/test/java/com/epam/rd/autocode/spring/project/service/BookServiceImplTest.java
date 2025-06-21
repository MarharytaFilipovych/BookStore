package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.SearchBookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mappers.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import com.querydsl.core.BooleanBuilder;
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

import java.math.BigDecimal;
import java.time.Year;
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

    @Mock
    private SortMappingService sortMappingService;

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
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Book> bookPage = new PageImpl<>(books, mappedPageable, books.size());

        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(books.size(), result.getTotalElements());
        assertEquals(bookDTOs, result.getContent());
        verify(sortMappingService).applyMappings(pageable, "book");
        verify(bookRepository).findAll(mappedPageable);
        verify(bookMapper, times(books.size())).toDto(any(Book.class));
    }

    @Test
    void getAllBooks_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Book> bookPage = new PageImpl<>(books, mappedPageable, books.size());

        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "book");
        verify(bookRepository).findAll(mappedPageable);
    }

    @Test
    void getAllBooks_WithSortByPriceDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Page<Book> bookPage = new PageImpl<>(books, mappedPageable, books.size());

        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "book");
        verify(bookRepository).findAll(mappedPageable);
    }

    @Test
    void getAllBooks_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange
        Sort multiSort = Sort.by("name").and(Sort.by("price").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Pageable mappedPageable = PageRequest.of(0, 10, multiSort);
        Page<Book> bookPage = new PageImpl<>(books, mappedPageable, books.size());

        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "book");
        verify(bookRepository).findAll(mappedPageable);
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
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Book> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);

        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(emptyPage);

        // Act
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(bookRepository).findAll(mappedPageable);
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    void getBookByName_WhenBookExists_ShouldReturnBookDTO() {
        // Arrange
        String bookName = book.getName();
        when(bookRepository.findByName(bookName)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        // Act
        BookDTO result = bookService.getBookByName(bookName);

        // Assert
        assertNotNull(result);
        assertEquals(bookDTO, result);
        verify(bookRepository).findByName(bookName);
        verify(bookMapper).toDto(book);
    }

    @Test
    void getBookByName_WhenBookDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String bookName = "Non-existent Book";
        when(bookRepository.findByName(bookName)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.getBookByName(bookName));
        assertTrue(exception.getMessage().contains("The book with a name " + bookName));
        verify(bookRepository).findByName(bookName);
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
        verify(bookRepository).save(argThat(b -> b.getId().equals(book.getId())));
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

    @Test
    void getAllBooksWithSearchCondition_WithValidCriteria_ShouldReturnMatchingBooks() {
        // Arrange
        SearchBookDTO searchCriteria = new SearchBookDTO();
        searchCriteria.setName("Adventure");
        searchCriteria.setGenre("Fantasy");
        searchCriteria.setAuthor("John");
        searchCriteria.setAgeGroup(AgeGroup.CHILD);
        searchCriteria.setLanguage(Language.ENGLISH);
        searchCriteria.setMinPrice(new BigDecimal("10.00"));
        searchCriteria.setMaxPrice(new BigDecimal("20.00"));
        searchCriteria.setMinPages(200);
        searchCriteria.setMaxPages(300);
        searchCriteria.setPublicationYear(Year.of(2023));

        List<Book> filteredBooks = List.of(book); // Mock filtered result
        List<BookDTO> filteredDTOs = List.of(bookDTO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(searchCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(filteredBooks.size(), result.getTotalElements());
        assertEquals(filteredDTOs, result.getContent());
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
        verify(bookMapper).toDto(book);
    }

    @Test
    void getAllBooksWithSearchCondition_WithEmptyResults_ShouldReturnEmptyPage() {
        // Arrange
        SearchBookDTO searchCriteria = new SearchBookDTO();
        searchCriteria.setName("NonExistentBook");
        searchCriteria.setMinPrice(new BigDecimal("1000.00"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(emptyPage);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(searchCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    void getAllBooksWithSearchCondition_WithNullCriteria_ShouldHandleGracefully() {
        // Arrange
        SearchBookDTO emptyCriteria = new SearchBookDTO(); // All fields null
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(bookPage);
        for (int i = 0; i < books.size(); i++) {
            when(bookMapper.toDto(books.get(i))).thenReturn(bookDTOs.get(i));
        }

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(emptyCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(books.size(), result.getTotalElements());
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
    }

    @Test
    void getAllBooksWithSearchCondition_WithPartialCriteria_ShouldUseOnlyProvidedFilters() {
        // Arrange
        SearchBookDTO partialCriteria = new SearchBookDTO();
        partialCriteria.setName("Adventure");
        partialCriteria.setLanguage(Language.ENGLISH);
        // Other fields remain null

        List<Book> filteredBooks = List.of(book);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(partialCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(bookDTO, result.getContent().get(0));
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
    }

    @Test
    void getAllBooksWithSearchCondition_WithPriceRangeOnly_ShouldFilterByPrice() {
        // Arrange
        SearchBookDTO priceCriteria = new SearchBookDTO();
        priceCriteria.setMinPrice(new BigDecimal("15.00"));
        priceCriteria.setMaxPrice(new BigDecimal("20.00"));

        List<Book> filteredBooks = List.of(book);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(priceCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));

        ArgumentCaptor<BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(BooleanBuilder.class);
        verify(bookRepository).findAll(predicateCaptor.capture(), eq(pageable));
        BooleanBuilder capturedPredicate = predicateCaptor.getValue();
        assertNotNull(capturedPredicate);
    }

    @Test
    void getAllBooksWithSearchCondition_WithPageRangeOnly_ShouldFilterByPages() {
        // Arrange
        SearchBookDTO pageCriteria = new SearchBookDTO();
        pageCriteria.setMinPages(200);
        pageCriteria.setMaxPages(300);

        List<Book> filteredBooks = List.of(book);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDTO);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(pageCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
    }
}