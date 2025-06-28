import React, {useCallback, useContext, useState} from "react";
import {BookType, BookFilterState, SearchBook, Language, AgeGroup, BookSortField, SortOrder} from "../../types";
import { BookService } from "../../services/BookService";
import { Book } from "../../components/Book/Book";
import { BookSearchField } from "../../components/Search/BookSearchField";
import { bookSortOptions, ageGroups, languages, genres } from "../../BusinessData";
import {GenericSearchablePage} from "./GenereicSearchablePage";
import {AppContext} from "../../context";
import {Button} from "../../components/Button/Button";
import {BookForm} from "../../components/BookForm/BookForm";
import styles from './style.module.css';

export const BooksPage: React.FC = () => {
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [isCreateBookDialogOpen, setIsCreateBookDialogOpen] = useState(false);
    const [createBookError, setCreateBookError] = useState('');
    const [isCreatingBook, setIsCreatingBook] = useState(false);
    const context = useContext(AppContext);

    const getFilterState = useCallback((searchParams: URLSearchParams): BookFilterState => ({
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
    }), []);

    const convertFilterToSearchBook = useCallback((filter: BookFilterState): SearchBook => {
        const searchDTO: SearchBook = {};
        if (filter.name) searchDTO.name = filter.name;
        if (filter.genre) searchDTO.genre = filter.genre;
        if (filter.author) searchDTO.author = filter.author;
        if (filter.language) searchDTO.language = filter.language.toUpperCase() as Language;
        if (filter.ageGroup) searchDTO.age_group = filter.ageGroup.toUpperCase() as AgeGroup;
        if (filter.minPrice) searchDTO.min_price = parseFloat(filter.minPrice);
        if (filter.maxPrice) searchDTO.max_price = parseFloat(filter.maxPrice);
        if (filter.minPages) searchDTO.min_pages = parseInt(filter.minPages);
        if (filter.maxPages) searchDTO.max_pages = parseInt(filter.maxPages);
        if (filter.publicationYear) searchDTO.publication_year = parseInt(filter.publicationYear);
        return searchDTO;
    }, []);

    const fetchBooks = useCallback(async (
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
    }, [convertFilterToSearchBook]);

    const renderSearchComponent = useCallback(({ filter, onFilterChange }: {
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
    ), []);

    const handleBookDelete = useCallback(async (bookName: string) => {
        console.log('🗑️ BooksPage: Handling book deletion...', { bookName });
        try {
            await BookService.deleteBook(bookName);
            context.removeFromBasket(bookName);
            console.log('✅ BooksPage: Book deleted successfully, refreshing page...');
            setRefreshTrigger(prev => prev + 1);
        } catch (error) {
            console.error('❌ BooksPage: Failed to delete book:', error);
            alert('Failed to delete book. Please try again.');
        }
    }, []);

    const handleBookUpdate = useCallback(async (bookName: string, updatedBook: BookType) => {
        console.log('✏️ BooksPage: Handling book update...', { bookName });

        try {
            await BookService.updateBook(bookName, updatedBook);
            console.log('✅ BooksPage: Book updated successfully, refreshing page...');
            setRefreshTrigger(prev => prev + 1);
        } catch (error) {
            console.error('❌ BooksPage: Failed to update book:', error);
        }
    }, []);

    const handleBookCreate = useCallback(async (book: BookType) => {
        console.log('📚 BooksPage: Handling book creation...', book);
        setIsCreatingBook(true);
        setCreateBookError('');

        try {
            await BookService.createBook(book);
            console.log('✅ BooksPage: Book created successfully, refreshing page...');

            // Close dialog and refresh
            setIsCreateBookDialogOpen(false);
            setRefreshTrigger(prev => prev + 1);

        } catch (error: any) {
            console.error('❌ BooksPage: Failed to create book:', error);
            setCreateBookError(error.response?.data?.message || 'Failed to create book. Please try again.');
        } finally {
            setIsCreatingBook(false);
        }
    }, []);

    const handleCancelCreate = useCallback(() => {
        setIsCreateBookDialogOpen(false);
        setCreateBookError('');
        setIsCreatingBook(false);
    }, []);

    const renderBook = useCallback((book: BookType, index: number) => (
        <Book
            key={`${book.name}-${index}`}
            {...book}
            onDelete={handleBookDelete}
            onUpdate={handleBookUpdate}
        />
    ), [handleBookDelete, handleBookUpdate]);

    return (
        <>
            {context.role === 'EMPLOYEE' && (
                <div className={styles.button}>
                    <Button
                        purpose='Create new book!'
                        onClick={() => setIsCreateBookDialogOpen(true)}
                        disabled={isCreatingBook}
                    />
                </div>
            )}

                <GenericSearchablePage<BookType, BookFilterState, BookSortField>
                    fetchData={fetchBooks}
                    getFilterFromParams={getFilterState}
                    sortOptions={bookSortOptions}
                    searchComponent={renderSearchComponent}
                    renderItem={renderBook}
                    noResultsMessage="No books found! Try adjusting your search criteria!"
                    showResultsCount={true}
                    resultsCountText={(count) => `Found ${count} ${count === 1 ? 'book' : 'books'}!`}
                    refreshTrigger={refreshTrigger}
                />

                {isCreateBookDialogOpen && (
                    <BookForm
                        onSubmit={handleBookCreate}
                        onCancel={handleCancelCreate}
                        error={createBookError}
                        processing={isCreatingBook}
                    />
                )}
        </>
    );
};