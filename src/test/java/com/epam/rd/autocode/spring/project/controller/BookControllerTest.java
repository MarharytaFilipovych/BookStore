package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.SearchBookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.BookData.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookDTO bookDTO;
    private List<BookDTO> bookDTOs;

    @BeforeEach
    void setUp() {
        bookDTO = getBookDTO();
        bookDTOs = getBookDTOs();
    }

    private void verifyBookJsonResponse(ResultActions resultActions, String jsonPath, BookDTO expectedBook) throws Exception {
        resultActions
                .andExpect(jsonPath(jsonPath + ".name").value(expectedBook.getName()))
                .andExpect(jsonPath(jsonPath + ".genre").value(expectedBook.getGenre()))
                .andExpect(jsonPath(jsonPath + ".age_group").value(expectedBook.getAgeGroup().toString()))
                .andExpect(jsonPath(jsonPath + ".price").value(expectedBook.getPrice().doubleValue()))
                .andExpect(jsonPath(jsonPath + ".publication_date").value(expectedBook.getPublicationDate().toString()))
                .andExpect(jsonPath(jsonPath + ".author").value(expectedBook.getAuthor()))
                .andExpect(jsonPath(jsonPath + ".pages").value(expectedBook.getPages()))
                .andExpect(jsonPath(jsonPath + ".characteristics").value(expectedBook.getCharacteristics()))
                .andExpect(jsonPath(jsonPath + ".description").value(expectedBook.getDescription()))
                .andExpect(jsonPath(jsonPath + ".language").value(expectedBook.getLanguage().toString()));
    }

    private void verifyAllBooksInPaginatedResponse(ResultActions resultActions, List<BookDTO> expectedBooks) throws Exception {
        for (int i = 0; i < expectedBooks.size(); i++) {
            verifyBookJsonResponse(resultActions, "$.books[" + i + "]", expectedBooks.get(i));
        }
    }

    private void verifyPaginatedMetadata(ResultActions resultActions, int expectedTotalCount, int expectedPage, int expectedSize) throws Exception {
        resultActions
                .andExpect(jsonPath("$.meta.total_count").value(expectedTotalCount))
                .andExpect(jsonPath("$.meta.page").value(expectedPage))
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books", hasSize(expectedSize)));
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooks_ShouldReturnPaginatedBooks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10)
                .withSort(Sort.by("name").ascending());
        List<BookDTO> booksForPage = bookDTOs.stream()
                .sorted(Comparator.comparing(BookDTO::getName))
                .limit(10)
                .toList();
        Page<BookDTO> page = new PageImpl<>(booksForPage, pageable, booksForPage.size());
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, booksForPage.size(), 0, booksForPage.size());
        verifyAllBooksInPaginatedResponse(resultActions, booksForPage);
        verify(bookService).getAllBooks(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooksWithSearch_ShouldReturnFilteredBooks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("name").ascending());
        SearchBookDTO searchBookDTO = new SearchBookDTO();
        searchBookDTO.setLanguage(Language.ENGLISH);
        searchBookDTO.setName("P");
        List<BookDTO> filteredBooks = bookDTOs.stream()
                .filter(b -> b.getName().contains("P") && b.getLanguage() == Language.ENGLISH)
                .sorted(Comparator.comparing(BookDTO::getName))
                .limit(10)
                .toList();
        Page<BookDTO> page = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());
        when(bookService.getAllBooksWithSearchCondition(searchBookDTO, pageable))
                .thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books/search")
                        .param("name", "P")
                        .param("language", "ENGLISH"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, filteredBooks.size(), 0, filteredBooks.size());
        verifyAllBooksInPaginatedResponse(resultActions, filteredBooks);
        verify(bookService).getAllBooksWithSearchCondition(searchBookDTO, pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooks_WithSecondPageAndCustomSize_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(1, 3).withSort(Sort.by("name").ascending());
        List<BookDTO> sortedBooks = bookDTOs.stream()
                .sorted(Comparator.comparing(BookDTO::getName))
                .toList();
        List<BookDTO> booksForPage = sortedBooks.stream()
                .skip(3)
                .limit(3)
                .toList();
        Page<BookDTO> page = new PageImpl<>(booksForPage, pageable, sortedBooks.size());
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books")
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk());
        verifyPaginatedMetadata(resultActions, sortedBooks.size(), 1, booksForPage.size());
        verifyAllBooksInPaginatedResponse(resultActions, booksForPage);
        verify(bookService).getAllBooks(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooks_WithPriceDescendingSort_ShouldReturnBooksSortedByPriceDesc() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10).withSort(Sort.by("price").descending());
        List<BookDTO> booksForPage = bookDTOs.stream()
                .sorted(Comparator.comparing(BookDTO::getPrice).reversed())
                .limit(10)
                .toList();
        Page<BookDTO> page = new PageImpl<>(booksForPage, pageable, booksForPage.size());
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk());

        verifyPaginatedMetadata(resultActions, booksForPage.size(), 0, booksForPage.size());
        verifyAllBooksInPaginatedResponse(resultActions, booksForPage);
        verify(bookService).getAllBooks(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooksWithSearch_WithDifferentPageAndAuthorSort_ShouldReturnFilteredSortedBooks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5).withSort(Sort.by("author").ascending());
        SearchBookDTO searchBookDTO = new SearchBookDTO();
        searchBookDTO.setLanguage(Language.ENGLISH);
        List<BookDTO> filteredBooks = bookDTOs.stream()
                .filter(b -> b.getLanguage() == Language.ENGLISH)
                .sorted(Comparator.comparing(BookDTO::getAuthor)) // Sort by author ascending
                .limit(5)
                .toList();
        Page<BookDTO> page = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());
        when(bookService.getAllBooksWithSearchCondition(searchBookDTO, pageable))
                .thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books/search")
                        .param("language", "ENGLISH")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "author,asc"))
                .andExpect(status().isOk());

        verifyPaginatedMetadata(resultActions, filteredBooks.size(), 0, filteredBooks.size());
        verifyAllBooksInPaginatedResponse(resultActions, filteredBooks);
        verify(bookService).getAllBooksWithSearchCondition(searchBookDTO, pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooksWithSearch_WithMultipleSortCriteria_ShouldReturnCorrectlyOrderedBooks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10)
                .withSort(Sort.by("genre").ascending()
                        .and(Sort.by("price").descending()));
        SearchBookDTO searchBookDTO = new SearchBookDTO();
        searchBookDTO.setName("a");
        List<BookDTO> filteredBooks = bookDTOs.stream()
                .filter(b -> b.getName().toLowerCase().contains("a"))
                .sorted(Comparator.comparing(BookDTO::getGenre)
                        .thenComparing(BookDTO::getPrice, Comparator.reverseOrder()))
                .limit(10)
                .toList();
        Page<BookDTO> page = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());
        when(bookService.getAllBooksWithSearchCondition(searchBookDTO, pageable))
                .thenReturn(page);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books/search")
                        .param("name", "a")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "genre,asc")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk());

        verifyPaginatedMetadata(resultActions, filteredBooks.size(), 0, filteredBooks.size());
        verifyAllBooksInPaginatedResponse(resultActions, filteredBooks);
        verify(bookService).getAllBooksWithSearchCondition(searchBookDTO, pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getBookByName_ShouldReturnBook() throws Exception {
        // Arrange
        when(bookService.getBookByName(bookDTO.getName())).thenReturn(bookDTO);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(get("/books/{name}", bookDTO.getName()))
                .andExpect(status().isOk());
        verifyBookJsonResponse(resultActions, "$", bookDTO);
        verify(bookService).getBookByName(bookDTO.getName());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getBookByName_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        String nonExistentBook = "Non-existent Book";
        when(bookService.getBookByName(nonExistentBook))
                .thenThrow(new NotFoundException("The book with a name " + nonExistentBook));

        // Act & Assert
        mockMvc.perform(get("/books/{name}", nonExistentBook))
                .andExpect(status().isNotFound());
        verify(bookService).getBookByName(nonExistentBook);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addBook_WhenUserIsEmployee_ShouldCreateBook() throws Exception {
        // Arrange
        when(bookService.addBook(bookDTO)).thenReturn(bookDTO);

        // Act & Assert
        ResultActions resultActions = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isCreated());
        verifyBookJsonResponse(resultActions, "$", bookDTO);
        verify(bookService).addBook(bookDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addBook_WhenAlreadyExists_ShouldReturn409() throws Exception {
        // Arrange
        when(bookService.addBook(bookDTO))
                .thenThrow(new AlreadyExistException("The book with a name " + bookDTO.getName()));

        // Act & Assert
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isConflict());
        verify(bookService).addBook(bookDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addBook_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        BookDTO invalidBookDTO = getBookDTO();
        invalidBookDTO.setPrice(new BigDecimal(-9));

        // Act & Assert
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addBook_WithInvalidDataMultipleFields_ShouldReturn400() throws Exception {
        // Arrange
        BookDTO invalidBookDTO = getBookDTO();
        invalidBookDTO.setPrice(null);
        invalidBookDTO.setPages(-1);
        invalidBookDTO.setName("");
        invalidBookDTO.setDescription("");

        // Act & Assert
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void addBook_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isForbidden());
        verify(bookService, never()).addBook(bookDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void updateBook_WhenUserIsEmployee_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        BookDTO updatedBook = getBookDTO();
        updatedBook.setName("Updated Book Name");
        updatedBook.setGenre("Updated Genre");
        updatedBook.setPrice(new BigDecimal("29.99"));
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPages(350);
        updatedBook.setCharacteristics("Updated characteristics");
        updatedBook.setDescription("Updated description");

        when(bookService.updateBookByName(bookDTO.getName(), updatedBook)).thenReturn(updatedBook);

        // Act & Assert
        mockMvc.perform(put("/books/{name}", bookDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNoContent());
        verify(bookService).updateBookByName(bookDTO.getName(), updatedBook);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void updateBook_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        BookDTO updatedBook = getBookDTO();
        String nonExistentBookName = "Non-existent Book";
        when(bookService.updateBookByName(nonExistentBookName, updatedBook))
                .thenThrow(new NotFoundException("The book with a name " + nonExistentBookName));

        // Act & Assert
        mockMvc.perform(put("/books/{name}", nonExistentBookName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNotFound());
        verify(bookService).updateBookByName(nonExistentBookName, updatedBook);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void updateBook_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/books/{name}", bookDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isForbidden());
        verify(bookService, never()).updateBookByName(bookDTO.getName(), bookDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void deleteBook_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/books/{name}", bookDTO.getName()))
                .andExpect(status().isNoContent());
        verify(bookService).deleteBookByName(bookDTO.getName());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void deleteBook_WhenNotFound_ShouldReturn404() throws Exception {
        // Arrange
        String nonExistentBookName = "Non-existent Book";
        doThrow(new NotFoundException("The book with a name " + nonExistentBookName))
                .when(bookService).deleteBookByName(nonExistentBookName);

        // Act & Assert
        mockMvc.perform(delete("/books/{name}", nonExistentBookName))
                .andExpect(status().isNotFound());
        verify(bookService).deleteBookByName(nonExistentBookName);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void deleteBook_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/books/{name}", bookDTO.getName()))
                .andExpect(status().isForbidden());
        verify(bookService, never()).deleteBookByName(bookDTO.getName());
    }

    @Test
    void getAllBooks_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/books"))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).getAllBooks(any(Pageable.class));
    }

    @Test
    void getBookByName_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/books/{name}", bookDTO.getName()))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).getBookByName(bookDTO.getName());
    }

    @Test
    void searchBooks_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/books/search")
                        .param("name", "Test"))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).getAllBooksWithSearchCondition(any(SearchBookDTO.class), any(Pageable.class));
    }

    @Test
    void addBook_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).addBook(bookDTO);
    }

    @Test
    void updateBook_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/books/{name}", bookDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).updateBookByName(bookDTO.getName(), bookDTO);
    }

    @Test
    void deleteBook_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/books/{name}", bookDTO.getName()))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).deleteBookByName(bookDTO.getName());
    }
}