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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.BookData.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooks_ShouldReturnPaginatedBooks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<BookDTO> booksForPage = bookDTOs.stream().limit(10).toList();
        Page<BookDTO> page = new PageImpl<>(booksForPage, pageable, booksForPage.size());
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);
        System.out.println(page == null? null : page.getContent().toString());

        // Act & Assert
        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.total_count").value(booksForPage.size()))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books[0].name").value(BOOK_NAME_1))
                .andExpect(jsonPath("$.books[0].author").value("John Adventure"));

        verify(bookService).getAllBooks(pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getAllBooksWithSearch_ShouldReturnFilteredBooks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        SearchBookDTO searchBookDTO = new SearchBookDTO();
        searchBookDTO.setLanguage(Language.ENGLISH);
        searchBookDTO.setName("P");
        List<BookDTO> filteredBooks = bookDTOs.stream()
                .filter(b -> b.getName().contains("P") && b.getLanguage()==Language.ENGLISH).toList();
        Page<BookDTO> page = new PageImpl<>(filteredBooks, pageable, filteredBooks.size());
        when(bookService.getAllBooksWithSearchCondition(searchBookDTO, pageable))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/books/search")
                        .param("name", "P")
                        .param("language", "ENGLISH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.total_count").value(filteredBooks.size()))
                .andExpect(jsonPath("$.books[0].name").value(filteredBooks.get(0).getName()));

        verify(bookService).getAllBooksWithSearchCondition(searchBookDTO, pageable);
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void getBookByName_ShouldReturnBook() throws Exception {
        // Arrange
        when(bookService.getBookByName(bookDTO.getName())).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(get("/books/{name}", bookDTO.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(bookDTO.getName()))
                .andExpect(jsonPath("$.author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("$.price").value(bookDTO.getPrice()))
                .andExpect(jsonPath("$.description").value(bookDTO.getDescription()))
                .andExpect(jsonPath("$.pages").value(bookDTO.getPages()))
                .andExpect(jsonPath("$.genre").value(bookDTO.getGenre()))
                .andExpect(jsonPath("$.language").value(bookDTO.getLanguage().toString()))
                .andExpect(jsonPath("$.age_group").value(bookDTO.getAgeGroup().toString()));

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
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(bookDTO.getName()))
                .andExpect(jsonPath("$.author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("$.price").value(bookDTO.getPrice()))
                .andExpect(jsonPath("$.description").value(bookDTO.getDescription()))
                .andExpect(jsonPath("$.pages").value(bookDTO.getPages()))
                .andExpect(jsonPath("$.genre").value(bookDTO.getGenre()))
                .andExpect(jsonPath("$.language").value(bookDTO.getLanguage().toString()))
                .andExpect(jsonPath("$.age_group").value(bookDTO.getAgeGroup().toString()));
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isConflict());

        verify(bookService).addBook(bookDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addBook_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        bookDTO.setPrice(new BigDecimal(-9));
        // Act & Assert
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void addBook_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isForbidden());

        verify(bookService, never()).addBook(bookDTO);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void updateBook_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        BookDTO updatedBook = getBookDTO();
        updatedBook.setName("Updated Book Name");
        when(bookService.updateBookByName(bookDTO.getName(), updatedBook)).thenReturn(updatedBook);

        // Act & Assert
        mockMvc.perform(put("/books/{name}", bookDTO.getName())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNoContent());

        verify(bookService).updateBookByName(bookDTO.getName(), updatedBook);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void deleteBook_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/books/{name}", bookDTO.getName())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBookByName(bookDTO.getName());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void deleteBook_WithClientRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/books/{name}", bookDTO.getName())
                        .with(csrf()))
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
}