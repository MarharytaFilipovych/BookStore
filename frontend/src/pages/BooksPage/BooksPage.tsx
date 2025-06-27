import React, { useEffect } from "react";
import { useLocation} from "react-router";
import styles from './style.module.css';
import { Icon } from "../../components/Icon/Icon";
import { BookType, FilterState, StateWithPagination, SearchBook, Language, AgeGroup } from "../../types";
import { Pagination } from "../../components/Pagination/Pagination";
import { useSearchParams } from "react-router-dom";
import { BookService } from "../../services/BookService";
import { Book } from "../../components/Book/Book";
import { BookSearchField } from "../../components/Search/BookSearchField";
import {sortOptions, ageGroups, languages, genres, PAGE_SIZE} from "../../BusinessData";
import {useStateWithUpdater} from "../../hooks/useStateWithUpdater";
import {setNewPageInQueryParams} from "../../utility/setNewPageInQueryParams";
import {setNewQueryParams} from "../../utility/setNewQueryParams";

type PageState = {
    totalPages: number,
    totalResults: number,
    books: BookType[],
} & StateWithPagination;

export const BooksPage: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const location = useLocation();
    const [state, updateState] = useStateWithUpdater<PageState>({
        loading: true,
        error: false,
        totalPages: 0,
        totalResults: 0,
        pageToFetch: Number(searchParams.get('page') ?? 1),
        books: [],
        currentPage: Number(searchParams.get('page') ?? 1)
    });

    const getFilterState = (searchParams: URLSearchParams): FilterState=> ({
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

    const convertFilterToSearchBook = (filter: FilterState): SearchBook => {
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

    const fetchData = async () => {
        const pageToFetch = Number(searchParams.get('page') ?? 1);

        updateState(({loading: true, pageToFetch, currentPage: pageToFetch }));

        try {
            const filterState = getFilterState(searchParams);
            const searchCriteria = convertFilterToSearchBook(filterState);
            const sorting = sortOptions.get(filterState.sort);

            const response = await BookService.searchBooks(
                searchCriteria,
                pageToFetch - 1,
                PAGE_SIZE,
                sorting?.sortBy,
                sorting?.sortOrder
            );

            updateState({
                totalPages: response.meta.totalPages,
                totalResults: response.meta.total_count,
                error: false,
                loading: false,
                books: response.books || []
            });
        } catch (err) {
            console.error('Error fetching books:', err);
            updateState({ error: true, loading: false });
        }
    };

    useEffect(() => {
        fetchData();
    }, [location]);

    return (
        <>
            {state.loading && (<Icon topic='loading' size='big' />)}
            {state.error && (<Icon topic='error' size='big' />)}
            {!state.error && (
                <div className={styles.page}>
                    <BookSearchField
                        genres={genres}
                        languages={languages}
                        ageGroups={ageGroups}
                        sortOptions={Array.from(sortOptions.keys())}
                        filter={getFilterState(searchParams)}
                        onFilterChange={(key, value) => {
                            setSearchParams(setNewQueryParams<FilterState>(key, value, searchParams));
                        }}
                    />
                    {state.totalResults > 0 && (
                        <h2>Found {state.totalResults} books!</h2>
                    )}

                    {state.books.length === 0 && !state.loading && (
                        <h2 className={styles.message}>No books found! Try adjusting your search criteria!</h2>
                    )}

                    {state.books.length > 0 && (
                        <div className={styles.booksContainer}>
                            {state.books.map((book, index) => (
                                <Book
                                    key={`${book.name}-${index}`}
                                    name={book.name}
                                    author={book.author}
                                    genre={book.genre}
                                    age_group={book.age_group}
                                    price={book.price}
                                    publication_date={book.publication_date}
                                    pages={book.pages}
                                    characteristics={book.characteristics}
                                    description={book.description}
                                    language={book.language}
                                />
                            ))}
                        </div>
                    )}

                    {state.totalPages > 1 && (
                        <Pagination
                            pageCount={state.totalPages}
                            onPageSelect={(page: number) => {
                                setSearchParams(setNewPageInQueryParams(page, searchParams));
                                window.scrollTo({
                                    top: 0,
                                    behavior: 'smooth'
                                });
                            }}
                            onClick={() => setSearchParams(setNewPageInQueryParams(Math.min(state.pageToFetch + 1, state.totalPages), searchParams))}
                            page={state.currentPage}
                            disabledShowMoreButton={state.totalPages === state.pageToFetch}
                        />
                    )}
                </div>
            )}
        </>
    );
};