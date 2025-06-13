package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@AllArgsConstructor
//@Table(name = "books", uniqueConstraints = {
//        @UniqueConstraint(
//                name = "unique_book_author_publish_date",
//                columnNames = {"author", "publication_year", "name"}
//        )
//})
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)//?
    private String name;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(nullable = false, name = "age_group")
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, name = "publication_year")
    private LocalDate publicationDate;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, name = "number_of_pages")
    private Integer pages;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String characteristics;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Language language;
}
