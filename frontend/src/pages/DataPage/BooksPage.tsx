import React from "react";
import {BookType, BookFilterState, SearchBook, Language, AgeGroup, BookSortField, SortOrder} from "../../types";
import { BookService } from "../../services/BookService";
import { Book } from "../../components/Book/Book";
import { BookSearchField } from "../../components/Search/BookSearchField";
import { bookSortOptions, ageGroups, languages, genres } from "../../BusinessData";
import styles from './style.module.css';
import {GenericSearchablePage} from "./GenereicSearchablePage";

export const BooksPage: React.FC = () => {
    const getFilterState = (searchParams: URLSearchParams): BookFilterState => ({
        name: searchParams.get('name') ?? '',
        genre: searchParams.get('genre') ?? '',
        author: searchParams.get('author') ?? '',
        language: searchParams.get('language') ?? '',
        ageGroup: searchParams.get('ageGroup') ?? '',
        minPrice: searchParams.get('minPrice') ?? '',
        maxPrice: searchParams.get('maxPrice') ?? '',
        minPages: searchParams.get('minPages') ?? '',
        maxPages: searchParams.get('maxPages') ?? '',
        publicationYear: searchParams.get('publicationYear') ?? '',
        sort: searchParams.get('sort') ?? ''
    });

    const convertFilterToSearchBook = (filter: BookFilterState): SearchBook => {
        const searchDTO: SearchBook = {};
        if (filter.name) searchDTO.name = filter.name;
        if (filter.genre) searchDTO.genre = filter.genre;
        if (filter.author) searchDTO.author = filter.author;
        if (filter.language) searchDTO.language = filter.language.toUpperCase() as Language;
        if (filter.ageGroup) searchDTO.ageGroup = filter.ageGroup.toUpperCase() as AgeGroup;
        if (filter.minPrice) searchDTO.minPrice = parseFloat(filter.minPrice);
        if (filter.maxPrice) searchDTO.maxPrice = parseFloat(filter.maxPrice);
        if (filter.minPages) searchDTO.minPages = parseInt(filter.minPages);
        if (filter.maxPages) searchDTO.maxPages = parseInt(filter.maxPages);
        if (filter.publicationYear) searchDTO.publicationYear = parseInt(filter.publicationYear);
        return searchDTO;
    };

    const fetchBooks = async (
        page: number,
        pageSize: number,
        filter: BookFilterState,
        sorting?: { sortBy: BookSortField; sortOrder: SortOrder }
    ) => {
        const searchCriteria = convertFilterToSearchBook(filter);
        const response = await BookService.searchBooks(
            searchCriteria,
            page,
            pageSize,
            sorting?.sortBy,
            sorting?.sortOrder
        );

        return {
            meta: response.meta,
            items: response.books || []
        };
    };

    const renderSearchComponent = ({ filter, onFilterChange }: {
        filter: BookFilterState;
        onFilterChange: (key: keyof BookFilterState, value: string) => void;
    }) => (
        <BookSearchField
            genres={genres}
            languages={languages}
            ageGroups={ageGroups}
            sortOptions={Array.from(bookSortOptions.keys())}
            filter={filter}
            onFilterChange={onFilterChange}
        />
    );

    const renderBook = (book: BookType, index: number) => (
        <Book
            key={`${book.name}-${index}`}
            {...book}
        />
    );

    return (
        <GenericSearchablePage<BookType, BookFilterState, BookSortField>
            fetchData={fetchBooks}
            getFilterFromParams={getFilterState}
            sortOptions={bookSortOptions}
            searchComponent={renderSearchComponent}
            renderItem={renderBook}
            itemsContainerClassName={styles.booksContainer}
            noResultsMessage="No books found! Try adjusting your search criteria!"
            showResultsCount={true}
            resultsCountText={(count) => `Found ${count} books!`}
        />
    );
};