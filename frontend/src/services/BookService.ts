import { apiClient } from '../config/ApiClient';
import {
    Book,
    PaginatedResponseDTO,
    SearchBookDTO,
    UpdateBookRequest,
    API_ENDPOINTS,
    BookSortField,
    SortOrder
} from '../types';

export class BookService {

    static async getBooks(
        page = 0,
        size = 10,
        sortBy?: BookSortField,
        sortOrder: SortOrder = 'asc'
    ): Promise<PaginatedResponseDTO<Book>> {
        console.log('📚 BookService: Getting books with sorting...', {
            page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 BookService: Sorting by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<Book>>(
                `${API_ENDPOINTS.books.getAll}?${params.toString()}`
            );

            console.log('✅ BookService: Books retrieved successfully', {
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default',
                totalBooks: response.data.meta?.total_count || 0,
                booksOnPage: response.data.books?.length || 0
            });

            return response.data;

        } catch (error) {
            console.error('❌ BookService: Failed to get sorted books', error);
            throw error;
        }
    }

    static async searchBooks(
        searchParams: SearchBookDTO,
        page = 0,
        size = 10,
        sortBy?: BookSortField,
        sortOrder: SortOrder = 'asc'
    ): Promise<PaginatedResponseDTO<Book>> {
        console.log('🔍 BookService: Searching books...', {
            searchParams, page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 BookService: Sorting search results by ${sortBy} (${sortOrder})`);
            }

            Object.entries(searchParams).forEach(([key, value]) => {
                if (value !== undefined && value !== '' && value !== null) {
                    params.append(key, value.toString());
                }
            });

            console.log('🔍 BookService: Search parameters prepared', {
                totalFilters: Object.keys(searchParams).length,
                activeFilters: Array.from(params.entries()).filter(([key]) =>
                    !['page', 'size', 'sort'].includes(key)
                ).length,
                hasSorting: !!sortBy
            });

            const response = await apiClient.get<PaginatedResponseDTO<Book>>(
                `${API_ENDPOINTS.books.search}?${params.toString()}`
            );

            console.log('✅ BookService: Search completed successfully', {
                resultsFound: response.data.meta?.total_count || 0,
                currentPage: response.data.meta?.page || 0,
                booksOnPage: response.data.books?.length || 0,
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default'
            });

            return response.data;

        } catch (error) {
            console.error('❌ BookService: Search failed', { searchParams, sortBy, error });
            throw error;
        }
    }

    static async getBookByName(name: string): Promise<Book> {
        console.log('📖 BookService: Getting book by name...', { name });

        try {
            const response = await apiClient.get<Book>(
                API_ENDPOINTS.books.getByName(name)
            );

            console.log('✅ BookService: Book retrieved successfully', {
                bookName: response.data.name,
                author: response.data.author,
                price: response.data.price
            });

            return response.data;

        } catch (error) {
            console.error('❌ BookService: Failed to get book', { name, error });
            throw error;
        }
    }

    static async createBook(book: Book): Promise<Book> {
        console.log('📝 BookService: Creating new book...', {
            name: book.name,
            author: book.author,
            price: book.price,
            language: book.language
        });

        try {
            const response = await apiClient.post<Book>(
                API_ENDPOINTS.books.create,
                book
            );

            console.log('✅ BookService: Book created successfully', {
                createdBook: response.data.name,
                author: response.data.author,
                price: response.data.price
            });

            return response.data;

        } catch (error) {
            console.error('❌ BookService: Failed to create book', {
                bookName: book.name,
                error
            });
            throw error;
        }
    }

    static async updateBook(name: string, updates: UpdateBookRequest): Promise<Book> {
        console.log('✏️ BookService: Updating book...', {
            bookName: name,
            fieldsToUpdate: Object.keys(updates),
            updateCount: Object.keys(updates).length
        });

        try {
            const response = await apiClient.put<Book>(
                API_ENDPOINTS.books.update(name),
                updates
            );

            console.log('✅ BookService: Book updated successfully', {
                updatedBook: response.data.name,
                previousName: name,
                fieldsUpdated: Object.keys(updates)
            });

            return response.data;

        } catch (error) {
            console.error('❌ BookService: Failed to update book', {
                bookName: name,
                updates,
                error
            });
            throw error;
        }
    }


    static async deleteBook(name: string): Promise<void> {
        console.log('🗑️ BookService: Deleting book...', { name });

        try {
            await apiClient.delete(API_ENDPOINTS.books.delete(name));

            console.log('✅ BookService: Book deleted successfully', {
                deletedBook: name
            });

        } catch (error) {
            console.error('❌ BookService: Failed to delete book', {
                bookName: name,
                error
            });
            throw error;
        }
    }
}
