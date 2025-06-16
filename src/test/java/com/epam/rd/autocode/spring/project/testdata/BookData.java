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
    public static final String BOOK_NAME_4 = "Romance in Paris";
    public static final String BOOK_NAME_5 = "Children's Fairy Tales";
    public static final String BOOK_NAME_6 = "Advanced Java Concepts";
    public static final String BOOK_NAME_7 = "Teenage Drama Stories";
    public static final String BOOK_NAME_8 = "Horror Night Tales";

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
                        Language.ENGLISH),
                new Book(4L,
                        BOOK_NAME_4,
                        "Romance",
                        AgeGroup.ADULT,
                        new BigDecimal("18.50"),
                        LocalDate.of(2022, 6, 15),
                        "Romance Author",
                        280,
                        "Romantic scenes, beautiful setting",
                        "A love story set in the heart of Paris",
                        Language.FRENCH),
                new Book(5L,
                        BOOK_NAME_5,
                        "Fantasy",
                        AgeGroup.CHILD,
                        new BigDecimal("12.99"),
                        LocalDate.of(2023, 3, 8),
                        "Fairy Tale Writer",
                        180,
                        "Colorful illustrations, moral lessons",
                        "Classic fairy tales for young children",
                        Language.ENGLISH),
                new Book(6L,
                        BOOK_NAME_6,
                        "Technical",
                        AgeGroup.ADULT,
                        new BigDecimal("52.00"),
                        LocalDate.of(2024, 2, 20),
                        "Java Expert",
                        450,
                        "Advanced concepts, design patterns",
                        "Deep dive into advanced Java programming",
                        Language.ENGLISH),
                new Book(7L,
                        BOOK_NAME_7,
                        "Drama",
                        AgeGroup.TEEN,
                        new BigDecimal("16.75"),
                        LocalDate.of(2023, 11, 5),
                        "Teen Author",
                        220,
                        "Coming of age, emotional journey",
                        "Stories about teenage life and challenges",
                        Language.ENGLISH),
                new Book(8L,
                        BOOK_NAME_8,
                        "Horror",
                        AgeGroup.ADULT,
                        new BigDecimal("24.99"),
                        LocalDate.of(2023, 10, 31),
                        "Horror Master",
                        350,
                        "Scary tales, psychological thriller",
                        "Spine-chilling horror stories",
                        Language.ENGLISH)
        ));
    }

    public static Book getBookEntity(){
        return new Book(1L,
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
                        Language.ENGLISH),
                new BookDTO(BOOK_NAME_4, "Romance", AgeGroup.ADULT, new BigDecimal("18.50"),
                        LocalDate.of(2022, 6, 15), "Romance Author", 280,
                        "Romantic scenes, beautiful setting", "A love story set in the heart of Paris",
                        Language.FRENCH),
                new BookDTO(BOOK_NAME_5, "Fantasy", AgeGroup.CHILD, new BigDecimal("12.99"),
                        LocalDate.of(2023, 3, 8), "Fairy Tale Writer", 180,
                        "Colorful illustrations, moral lessons", "Classic fairy tales for young children",
                        Language.ENGLISH),
                new BookDTO(BOOK_NAME_6, "Technical", AgeGroup.ADULT, new BigDecimal("52.00"),
                        LocalDate.of(2024, 2, 20), "Java Expert", 450,
                        "Advanced concepts, design patterns", "Deep dive into advanced Java programming",
                        Language.ENGLISH),
                new BookDTO(BOOK_NAME_7, "Drama", AgeGroup.TEEN, new BigDecimal("16.75"),
                        LocalDate.of(2023, 11, 5), "Teen Author", 220,
                        "Coming of age, emotional journey", "Stories about teenage life and challenges",
                        Language.ENGLISH),
                new BookDTO(BOOK_NAME_8, "Horror", AgeGroup.ADULT, new BigDecimal("24.99"),
                        LocalDate.of(2023, 10, 31), "Horror Master", 350,
                        "Scary tales, psychological thriller", "Spine-chilling horror stories",
                        Language.ENGLISH)
        ));
    }

    public static BookDTO getBookDTO() {
        return new BookDTO(BOOK_NAME_1, "Fantasy", AgeGroup.CHILD, new BigDecimal("15.99"),
                LocalDate.of(2023, 5, 15), "John Adventure", 250,
                "Colorful illustrations, hardcover", "An exciting fantasy adventure for young readers",
                Language.ENGLISH);
    }
}
