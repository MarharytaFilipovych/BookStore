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
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import static com.epam.rd.autocode.spring.project.testdata.BookData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private BookMapper bookMapper;
    @Mock private SortMappingService sortMappingService;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;
    private List<Book> books;
    private List<BookDTO> bookDTOs;

    @BeforeEach
    void setUp() {
        book = getBookEntity();
        bookDTO = getBookDTO();
        books = getBookEntities();
        bookDTOs = getBookDTOs();
    }

    private void mockPageableBookOperations(Pageable pageable, Pageable mappedPageable,
                                            List<Book> bookList, List<BookDTO> bookDTOList) {
        Page<Book> bookPage = new PageImpl<>(bookList, mappedPageable, bookList.size());

        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(bookPage);

        for (int i = 0; i < bookList.size(); i++) {
            when(bookMapper.toDto(bookList.get(i))).thenReturn(bookDTOList.get(i));
        }
    }

    private void mockSimpleBookListOperations(List<Book> bookList, List<BookDTO> bookDTOList) {
        when(bookRepository.findAll()).thenReturn(bookList);
        for (int i = 0; i < bookList.size(); i++) {
            when(bookMapper.toDto(bookList.get(i))).thenReturn(bookDTOList.get(i));
        }
    }

    private void mockEmptyPageableRepository(Pageable pageable, Pageable mappedPageable) {
        Page<Book> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);
        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(mappedPageable)).thenReturn(emptyPage);
    }

    private void mockSuccessfulBookUpdate(String bookName, BookDTO updateData, Book updatedBook) {
        when(bookRepository.findByName(bookName)).thenReturn(Optional.of(book));
        when(bookMapper.toEntity(updateData)).thenReturn(updatedBook);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            assertEquals(book.getId(), saved.getId());
            return saved;
        });
        when(bookMapper.toDto(updatedBook)).thenReturn(updateData);
    }

    private void mockSuccessfulBookCreation(BookDTO dto, Book bookEntity) {
        when(bookMapper.toEntity(dto)).thenReturn(bookEntity);
        when(bookRepository.save(bookEntity)).thenReturn(bookEntity);
        when(bookMapper.toDto(bookEntity)).thenReturn(dto);
    }

    private void mockBookCreationFailure(BookDTO dto, Book bookEntity) {
        when(bookMapper.toEntity(dto)).thenReturn(bookEntity);
        when(bookRepository.save(bookEntity)).thenThrow(new DataIntegrityViolationException("Duplicate entry"));
    }

    private SearchBookDTO createFullSearchCriteria() {
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
        return searchCriteria;
    }

    private SearchBookDTO createPartialSearchCriteria() {
        SearchBookDTO partialCriteria = new SearchBookDTO();
        partialCriteria.setName("Adventure");
        partialCriteria.setLanguage(Language.ENGLISH);
        return partialCriteria;
    }

    private SearchBookDTO createPriceRangeCriteria() {
        SearchBookDTO priceCriteria = new SearchBookDTO();
        priceCriteria.setMinPrice(new BigDecimal("15.00"));
        priceCriteria.setMaxPrice(new BigDecimal("20.00"));
        return priceCriteria;
    }

    private SearchBookDTO createPageRangeCriteria() {
        SearchBookDTO pageCriteria = new SearchBookDTO();
        pageCriteria.setMinPages(200);
        pageCriteria.setMaxPages(300);
        return pageCriteria;
    }

    private void verifyPageableOperations(Pageable originalPageable, Pageable mappedPageable) {
        verify(sortMappingService).applyMappings(originalPageable, "book");
        verify(bookRepository).findAll(mappedPageable);
    }

    private void verifyPagedResults(Page<BookDTO> result, List<BookDTO> expectedContent, int expectedTotalElements) {
        assertNotNull(result);
        assertEquals(expectedTotalElements, result.getTotalElements());
        assertEquals(expectedContent, result.getContent());
    }

    private void verifyListResults(List<BookDTO> result, List<BookDTO> expectedContent) {
        assertNotNull(result);
        assertEquals(expectedContent.size(), result.size());
        assertEquals(expectedContent, result);
    }

    private void verifyEmptyResults(Page<BookDTO> result) {
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    private void verifyEmptyListResults(List<BookDTO> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private void verifyBookUpdateOperations(String bookName, BookDTO updateData, Book updatedBook) {
        verify(bookRepository).findByName(bookName);
        verify(bookMapper).toEntity(updateData);
        verify(bookRepository).save(argThat(b -> b.getId().equals(book.getId())));
        verify(bookMapper).toDto(updatedBook);
    }

    private void verifyNotFoundScenario(String bookName) {
        Exception exception = assertThrows(NotFoundException.class,
                () -> bookService.getBookByName(bookName));
        assertTrue(exception.getMessage().contains("The book with a name " + bookName));
        verify(bookRepository).findByName(bookName);
        verify(bookMapper, never()).toDto(any());
    }

    private void verifyBookCreationOperations(BookDTO dto, Book bookEntity) {
        verify(bookMapper).toEntity(dto);
        verify(bookRepository).save(bookEntity);
        verify(bookMapper).toDto(bookEntity);
    }

    private void verifySearchOperations(Pageable pageable, int expectedResults) {
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(pageable));
        if (expectedResults > 0) {
            verify(bookMapper, times(expectedResults)).toDto(any(Book.class));
        } else {
            verify(bookMapper, never()).toDto(any());
        }
    }

    private void testSortingScenario(Sort sort) {
        Pageable pageable = PageRequest.of(0, 10, sort);
        Pageable mappedPageable = PageRequest.of(0, 10, sort);

        mockPageableBookOperations(pageable, mappedPageable, books, bookDTOs);

        // Act
        bookService.getAllBooks(pageable);

        // Assert
        verifyPageableOperations(pageable, mappedPageable);
    }

    @Test
    void getAllBooks_WithPageable_ShouldReturnPageOfBookDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockPageableBookOperations(pageable, mappedPageable, books, bookDTOs);

        // Act
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Assert
        verifyPagedResults(result, bookDTOs, books.size());
        verifyPageableOperations(pageable, mappedPageable);
        verify(bookMapper, times(books.size())).toDto(any(Book.class));
    }

    @Test
    void getAllBooks_WithSortByNameAsc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("name"));
    }

    @Test
    void getAllBooks_WithSortByPriceDesc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("price").descending());
    }

    @Test
    void getAllBooks_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        Sort multiSort = Sort.by("name").and(Sort.by("price").descending());
        testSortingScenario(multiSort);
    }

    @Test
    void getAllBooks_WithoutPageable_ShouldReturnListOfBookDTOs() {
        // Arrange
        mockSimpleBookListOperations(books, bookDTOs);

        // Act
        List<BookDTO> result = bookService.getAllBooks();

        // Assert
        verifyListResults(result, bookDTOs);
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
        verifyEmptyListResults(result);
        verify(bookRepository).findAll();
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    void getAllBooks_WithPageable_WhenEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockEmptyPageableRepository(pageable, mappedPageable);

        // Act
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Assert
        verifyEmptyResults(result);
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
        verifyNotFoundScenario(bookName);
    }

    @Test
    void updateBookByName_WhenBookExists_ShouldReturnUpdatedBookDTO() {
        // Arrange
        BookDTO updateData = getBookDTO();
        updateData.setName("New Name");
        Book updatedBook = getBookEntity();
        updatedBook.setName("New Name");

        mockSuccessfulBookUpdate(book.getName(), updateData, updatedBook);

        // Act
        BookDTO result = bookService.updateBookByName(book.getName(), updateData);

        // Assert
        assertNotNull(result);
        assertEquals(updateData, result);
        verifyBookUpdateOperations(book.getName(), updateData, updatedBook);
    }

    @Test
    void updateBookByName_WhenBookDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String bookName = "Non-existent Book";
        when(bookRepository.findByName(bookName)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.updateBookByName(bookName, any(BookDTO.class)));
        assertEquals("The book with a name " + bookName + " was not found!", exception.getMessage());
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
        mockSuccessfulBookCreation(bookDTO, book);

        // Act
        BookDTO result = bookService.addBook(bookDTO);

        // Assert
        assertNotNull(result);
        assertEquals(bookDTO, result);
        verifyBookCreationOperations(bookDTO, book);
    }

    @Test
    void addBook_WhenBookAlreadyExists_ShouldThrowAlreadyExistException() {
        // Arrange
        mockBookCreationFailure(bookDTO, book);

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
        SearchBookDTO searchCriteria = createFullSearchCriteria();
        List<Book> filteredBooks = books.stream()
                .filter(b -> b.getName().contains("Adventure") ||
                        b.getGenre().equals("Fantasy") ||
                        b.getAuthor().contains("John"))
                .toList();
        List<BookDTO> filteredDTOs = bookDTOs.stream()
                .filter(dto -> dto.getName().contains("Adventure") ||
                        dto.getGenre().equals("Fantasy") ||
                        dto.getAuthor().contains("John"))
                .toList();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));

        mockSearchOperationWithMapping(pageable, mappedPageable, filteredBooks, filteredDTOs);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(searchCriteria, pageable);

        // Assert
        verifyPagedResults(result, filteredDTOs, filteredBooks.size());
        verifySearchOperations(mappedPageable, filteredDTOs.size());
    }

    @Test
    void getAllBooksWithSearchCondition_WithPartialCriteria_ShouldUseOnlyProvidedFilters() {
        // Arrange
        SearchBookDTO partialCriteria = createPartialSearchCriteria();
        List<Book> filteredBooks = books.stream()
                .filter(b -> b.getName().contains("Adventure") &&
                        b.getLanguage() == Language.ENGLISH)
                .toList();
        List<BookDTO> filteredDTOs = bookDTOs.stream()
                .filter(dto -> dto.getName().contains("Adventure") &&
                        dto.getLanguage() == Language.ENGLISH)
                .toList();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));

        mockSearchOperationWithMapping(pageable, mappedPageable, filteredBooks, filteredDTOs);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(partialCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(filteredBooks.size(), result.getTotalElements());
        assertEquals(filteredDTOs, result.getContent());
        verifySearchOperations(mappedPageable, filteredDTOs.size());
    }

    @Test
    void getAllBooksWithSearchCondition_WithPriceRangeOnly_ShouldFilterByPrice() {
        // Arrange
        SearchBookDTO priceCriteria = createPriceRangeCriteria();
        List<Book> filteredBooks = books.stream()
                .filter(b -> b.getPrice().compareTo(new BigDecimal("15.00")) >= 0 &&
                        b.getPrice().compareTo(new BigDecimal("20.00")) <= 0)
                .toList();
        List<BookDTO> filteredDTOs = bookDTOs.stream()
                .filter(dto -> dto.getPrice().compareTo(new BigDecimal("15.00")) >= 0 &&
                        dto.getPrice().compareTo(new BigDecimal("20.00")) <= 0)
                .toList();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));

        mockSearchOperationWithMapping(pageable, mappedPageable, filteredBooks, filteredDTOs);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(priceCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(filteredBooks.size(), result.getTotalElements());
        verifySearchOperations(mappedPageable, filteredDTOs.size());
        verifyPredicateCapture(mappedPageable);
    }

    @Test
    void getAllBooksWithSearchCondition_WithPageRangeOnly_ShouldFilterByPages() {
        // Arrange
        SearchBookDTO pageCriteria = createPageRangeCriteria();
        List<Book> filteredBooks = books.stream()
                .filter(b -> b.getPages() >= 200 && b.getPages() <= 300)
                .toList();
        List<BookDTO> filteredDTOs = bookDTOs.stream()
                .filter(dto -> dto.getPages() >= 200 && dto.getPages() <= 300)
                .toList();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));

        mockSearchOperationWithMapping(pageable, mappedPageable, filteredBooks, filteredDTOs);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(pageCriteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(filteredBooks.size(), result.getTotalElements());
        verifySearchOperations(mappedPageable, filteredDTOs.size());
    }

    @Test
    void getAllBooksWithSearchCondition_WithNullCriteria_ShouldHandleGracefully() {
        // Arrange
        SearchBookDTO emptyCriteria = new SearchBookDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("name"));
        mockSearchOperationWithMapping(pageable, mappedPageable, books, bookDTOs);

        // Act
        Page<BookDTO> result = bookService.getAllBooksWithSearchCondition(emptyCriteria, pageable);

        // Assert
        verifyPagedResults(result, bookDTOs, books.size());
        verify(bookRepository).findAll(any(BooleanBuilder.class), eq(mappedPageable));
    }

    private void mockSearchOperationWithMapping(Pageable pageable, Pageable mappedPageable,
                                                List<Book> resultBooks, List<BookDTO> resultDTOs) {
        Page<Book> bookPage = new PageImpl<>(resultBooks, mappedPageable, resultBooks.size());
        when(sortMappingService.applyMappings(pageable, "book")).thenReturn(mappedPageable);
        when(bookRepository.findAll(any(BooleanBuilder.class), eq(mappedPageable))).thenReturn(bookPage);

        for (int i = 0; i < resultBooks.size(); i++) {
            when(bookMapper.toDto(resultBooks.get(i))).thenReturn(resultDTOs.get(i));
        }
    }

    private void verifyPredicateCapture(Pageable mappedPageable) {
        ArgumentCaptor<BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(BooleanBuilder.class);
        verify(bookRepository).findAll(predicateCaptor.capture(), eq(mappedPageable));
        BooleanBuilder capturedPredicate = predicateCaptor.getValue();
        assertNotNull(capturedPredicate);
    }
}