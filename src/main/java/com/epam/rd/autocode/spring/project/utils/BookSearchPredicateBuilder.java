package com.epam.rd.autocode.spring.project.utils;

import com.epam.rd.autocode.spring.project.model.QBook;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.querydsl.core.BooleanBuilder;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class BookSearchPredicateBuilder {
    private final QBook book = QBook.book;
    private final BooleanBuilder predicate = new BooleanBuilder();

    public static BookSearchPredicateBuilder create() {
        return new BookSearchPredicateBuilder();
    }

    public BookSearchPredicateBuilder withName(String name){
        Optional.ofNullable(name)
                .filter(StringUtils::hasText)
                .ifPresent(n -> predicate.and(book.name.containsIgnoreCase(n)));
        return this;
    }

    public BookSearchPredicateBuilder withGenre(String genre) {
        Optional.ofNullable(genre)
                .filter(StringUtils::hasText)
                .ifPresent(g -> predicate.and(book.genre.containsIgnoreCase(g)));
        return this;
    }

    public BookSearchPredicateBuilder withAuthor(String author) {
        Optional.ofNullable(author)
                .filter(StringUtils::hasText)
                .ifPresent(a -> predicate.and(book.author.containsIgnoreCase(a)));
        return this;
    }

    public BookSearchPredicateBuilder withAgeGroup(AgeGroup ageGroup) {
        Optional.ofNullable(ageGroup)
                .ifPresent(ag -> predicate.and(book.ageGroup.eq(ag)));
        return this;
    }

    public BookSearchPredicateBuilder withLanguage(Language language) {
        Optional.ofNullable(language)
                .ifPresent(l -> predicate.and(book.language.eq(l)));
        return this;
    }

    public BookSearchPredicateBuilder withMinPrice(BigDecimal minPrice) {
        Optional.ofNullable(minPrice)
                .ifPresent(mp -> predicate.and(book.price.goe(mp)));
        return this;
    }

    public BookSearchPredicateBuilder withMaxPrice(java.math.BigDecimal maxPrice) {
        Optional.ofNullable(maxPrice)
                .ifPresent(mp -> predicate.and(book.price.loe(mp)));
        return this;
    }

    public BookSearchPredicateBuilder withPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return this.withMinPrice(minPrice).withMaxPrice(maxPrice);
    }

    public BookSearchPredicateBuilder withMinPages(Integer minPages) {
        Optional.ofNullable(minPages)
                .ifPresent(mp -> predicate.and(book.pages.goe(mp)));
        return this;
    }

    public BookSearchPredicateBuilder withMaxPages(Integer maxPages) {
        Optional.ofNullable(maxPages)
                .ifPresent(mp -> predicate.and(book.pages.loe(mp)));
        return this;
    }

    public BookSearchPredicateBuilder withPageRange(Integer minPages, Integer maxPages) {
        return this.withMinPages(minPages).withMaxPages(maxPages);
    }

    public BookSearchPredicateBuilder withPublicationYear(java.time.Year year) {
        int NUMBER_OF_MONTHS = 12;
        int NUMBER_OF_DAYS_IN_MONTH = 31;
        Optional.ofNullable(year)
                .ifPresent(y -> {
                    LocalDate startOfYear = LocalDate.of(y.getValue(), 1, 1);
                    LocalDate endOfYear = LocalDate.of(y.getValue(), NUMBER_OF_MONTHS, NUMBER_OF_DAYS_IN_MONTH);
                    predicate.and(book.publicationDate.between(startOfYear, endOfYear));
                });
        return this;
    }

    public BooleanBuilder build() {
        return predicate;
    }
}
