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

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;
    private List<Book> books;
    private List<BookDTO> bookDTOs;
    private SearchBookDTO searchBookDTO;

    @BeforeEach
    void setUp(){
        book = getBookEntity();
        bookDTO = getBookDTO();
        books = getBookEntities();
        bookDTOs = getBookDTOs();
        searchBookDTO = new SearchBookDTO();
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

    @Test
    void getAllBooksWithSearchCondition_WithAllCriteria_ShouldReturnMatchingBooks() {
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

        List<Book> expectedBooks = books.stream()
                .filter(book -> book.getName().toLowerCase().contains("adventure") &&
                        book.getGenre().toLowerCase().contains("fantasy") &&
                        book.getAuthor().toLowerCase().contains("john") &&
                        book.getAgeGroup() == AgeGroup.CHILD &&
                        book.getLanguage() == Language.ENGLISH &&
                        book.getPrice().compareTo(new BigDecimal("10.00")) >= 0 &&
                        book.getPrice().compareTo(new BigDecimal("20.00")) <= 0 &&
                        book.getPages() >= 200 &&
                        book.getPages() <= 300 &&
                        book.getPublicationDate().getYear() == 2023)
                .toList();

        List<BookDTO> expectedDTOs = bookDTOs.stream()
                .filter(dto -> dto.getName().toLowerCase().contains("adventure") &&
                        dto.getGenre().toLowerCase().contains("fantasy") &&
                        dto.getAuthor().toLowerCase().contains("john") &&
                        dto.getAgeGroup() == AgeGroup.CHILD &&
                        dto.getLanguage() == Language.ENGLISH &&
                        dto.getPrice().compareTo(new BigDecimal("10.00")) >= 0 &&
                        dto.getPrice().compareTo(new BigDecimal("20.00")) <= 0 &&
                        dto.getPages() >= 200 &&
                        dto.getPages() <= 300 &&
                        dto.getPublicationDate().getYear() == 2023)
                .toList();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(expectedBooks, pageable, expectedBooks.size());

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(bookPage);
        for (int i = 0; i < expectedBooks.size(); i++) {
            when(bookMapper.toDto(expectedBooks.get(i))).thenReturn(expectedDTOs.get(i));
        }

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(searchCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(expectedBooks.size(), result.getTotalElements());
        assertEquals(expectedBooks.size(), result.getContent().size());
        assertEquals(expectedDTOs, result.getContent());

        assertTrue(result.getContent().stream().allMatch(book ->
                book.getName().toLowerCase().contains("adventure") &&
                        book.getGenre().toLowerCase().contains("fantasy") &&
                        book.getAuthor().toLowerCase().contains("john") &&
                        book.getAgeGroup() == AgeGroup.CHILD &&
                        book.getLanguage() == Language.ENGLISH &&
                        book.getPrice().compareTo(new BigDecimal("10.00")) >= 0 &&
                        book.getPrice().compareTo(new BigDecimal("20.00")) <= 0 &&
                        book.getPages() >= 200 &&
                        book.getPages() <= 300 &&
                        book.getPublicationDate().getYear() == 2023
        ));

        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
        verify(bookMapper, times(expectedBooks.size())).toDto(any(Book.class));

        ArgumentCaptor<BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(BooleanBuilder.class);
        verify(bookRepository).findAll(predicateCaptor.capture(), eq(pageable));
        BooleanBuilder capturedPredicate = predicateCaptor.getValue();
        assertNotNull(capturedPredicate);
        assertTrue(capturedPredicate.hasValue());
    }

    @Test
    void getAllBooksWithSearchCondition_WithNoMatchingCriteria_ShouldReturnEmptyResults() {
        // Arrange
        SearchBookDTO noMatchCriteria = new SearchBookDTO();
        noMatchCriteria.setName("NonExistentBook");
        noMatchCriteria.setGenre("Science Fiction");
        noMatchCriteria.setAuthor("Unknown Author");
        noMatchCriteria.setLanguage(Language.SPANISH);
        noMatchCriteria.setAgeGroup(AgeGroup.OTHER);
        noMatchCriteria.setMinPrice(new BigDecimal("1000.00"));
        noMatchCriteria.setMaxPrice(new BigDecimal("1500.00"));
        noMatchCriteria.setMinPages(5000);
        noMatchCriteria.setMaxPages(6000);
        noMatchCriteria.setPublicationYear(Year.of(1900));

        List<Book> expectedBooks = books.stream()
                .filter(book -> book.getName().toLowerCase().contains("nonexistentbook") &&
                        book.getGenre().toLowerCase().contains("science fiction") &&
                        book.getAuthor().toLowerCase().contains("unknown author") &&
                        book.getLanguage() == Language.SPANISH &&
                        book.getAgeGroup() == AgeGroup.OTHER &&
                        book.getPrice().compareTo(new BigDecimal("1000.00")) >= 0 &&
                        book.getPrice().compareTo(new BigDecimal("1500.00")) <= 0 &&
                        book.getPages() >= 5000 &&
                        book.getPages() <= 6000 &&
                        book.getPublicationDate().getYear() == 1900)
                .toList();

        List<BookDTO> expectedDTOs = bookDTOs.stream()
                .filter(dto -> dto.getName().toLowerCase().contains("nonexistentbook") &&
                        dto.getGenre().toLowerCase().contains("science fiction") &&
                        dto.getAuthor().toLowerCase().contains("unknown author") &&
                        dto.getLanguage() == Language.SPANISH &&
                        dto.getAgeGroup() == AgeGroup.OTHER &&
                        dto.getPrice().compareTo(new BigDecimal("1000.00")) >= 0 &&
                        dto.getPrice().compareTo(new BigDecimal("1500.00")) <= 0 &&
                        dto.getPages() >= 5000 &&
                        dto.getPages() <= 6000 &&
                        dto.getPublicationDate().getYear() == 1900)
                .toList();

        assertTrue(expectedBooks.isEmpty(), "Test data should not contain books matching these criteria");
        assertTrue(expectedDTOs.isEmpty(), "Test data should not contain DTOs matching these criteria");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(bookRepository.findAll(any(BooleanBuilder.class), eq(pageable))).thenReturn(emptyPage);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(noMatchCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        assertTrue(result.getContent().isEmpty());

        assertTrue(result.getContent().isEmpty(), "No books should match the specified criteria");

        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
        verify(bookMapper, never()).toDto(any(Book.class));

        ArgumentCaptor<BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(BooleanBuilder.class);
        verify(bookRepository).findAll(predicateCaptor.capture(), eq(pageable));
        BooleanBuilder capturedPredicate = predicateCaptor.getValue();
        assertNotNull(capturedPredicate);
        assertTrue(capturedPredicate.hasValue());
    }
}

