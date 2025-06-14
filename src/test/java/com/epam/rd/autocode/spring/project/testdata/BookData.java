package com.epam.rd.autocode.spring.project.testdata;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookData {
    public static final String BOOK_NAME_1 = "The Great Adventure";
    public static final String BOOK_NAME_2 = "Learning Python Programming";
    public static final String BOOK_NAME_3 = "Mystery of the Lost Kingdom";

    public static List<Book> getBookEntities() {
        return new ArrayList<>(List.of(
                getBookEntity(),
                new Book(2L,
                        BOOK_NAME_2,
                        "Technical",
                        AgeGroup.ADULT,
                        new BigDecimal("45.99"),
                        LocalDate.of(2024, 1, 10),
                        "Tech Expert",
                        400,
                        "Code examples, exercises included",
                        "Comprehensive guide to Python programming",
                        Language.ENGLISH),
                new Book(3L,
                        BOOK_NAME_3,
                        "Mystery",
                        AgeGroup.TEEN,
                        new BigDecimal("21.99"),
                        LocalDate.of(2023, 9, 20),
                        "Mystery Writer",
                        320,
                        "Thriller elements, plot twists",
                        "A captivating mystery for teenage readers",
                        Language.ENGLISH)
        ));
    }

    public static Book getBookEntity(){
        return  new Book(1L,
                BOOK_NAME_1,
                "Fantasy",
                AgeGroup.CHILD,
                new BigDecimal("15.99"),
                LocalDate.of(2023, 5, 15),
                "John Adventure",
                250,
                "Colorful illustrations, hardcover",
                "An exciting fantasy adventure for young readers",
                Language.ENGLISH);
    }

    public static BookDTO getBookDTO() {
        return new BookDTO(BOOK_NAME_1, "Fantasy", AgeGroup.CHILD, new BigDecimal("15.99"),
                LocalDate.of(2023, 5, 15), "John Adventure", 250,
                "Colorful illustrations, hardcover", "An exciting fantasy adventure for young readers",
                Language.ENGLISH);
    }

    public static List<BookDTO> getBookDTOs() {
        return new ArrayList<>(List.of(
                getBookDTO(),
                new BookDTO(BOOK_NAME_2, "Technical", AgeGroup.ADULT, new BigDecimal("45.99"),
                        LocalDate.of(2024, 1, 10), "Tech Expert", 400,
                        "Code examples, exercises included", "Comprehensive guide to Python programming",
                        Language.ENGLISH),
                new BookDTO(BOOK_NAME_3, "Mystery", AgeGroup.TEEN, new BigDecimal("21.99"),
                        LocalDate.of(2023, 9, 20), "Mystery Writer", 320,
                        "Thriller elements, plot twists", "A captivating mystery for teenage readers",
                        Language.ENGLISH)
        ));
    }
}
