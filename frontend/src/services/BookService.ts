import { apiClient } from '../config/ApiClient';
import {BookType, PaginatedResponseDTO, SearchBook, UpdateBookRequest, BookSortField, SortOrder} from '../types';
import {API_ENDPOINTS, PAGE_SIZE} from "../BusinessData";

export class BookService {
    static async searchBooks(searchParams: SearchBook, page = 0, size = PAGE_SIZE, sortBy?: BookSortField,
        sortOrder: SortOrder = 'asc'): Promise<PaginatedResponseDTO<BookType>> {
        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) params.append('sort', `${sortBy},${sortOrder}`);
            Object.entries(searchParams).forEach(([key, value]) => {
                if (value !== undefined && value !== '' && value !== null) params.append(key, value.toString());});
            const response = await apiClient.get<PaginatedResponseDTO<BookType>>
            (`${API_ENDPOINTS.books.search}?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error('❌ BookService: Search failed', { searchParams, sortBy, error });
            throw error;
        }
    }

    static async getBookByName(name: string): Promise<BookType> {
        try {
            const response = await apiClient.get<BookType>(API_ENDPOINTS.books.getByName(name));
            return response.data;
        } catch (error) {
            console.error('❌ BookService: Failed to get book', { name, error });
            throw error;
        }
    }

    static async createBook(book: BookType): Promise<BookType> {
        try {
            const response = await apiClient.post<BookType>(API_ENDPOINTS.books.create, book);
            return response.data;
        } catch (error) {
            console.error('❌ BookService: Failed to create book', {bookName: book.name, error});
            throw error;
        }
    }

    static async updateBook(name: string, updates: UpdateBookRequest): Promise<BookType> {
        try {
            const response = await apiClient.put<BookType>(API_ENDPOINTS.books.update(name), updates);
            return response.data;
        } catch (error) {
            console.error('❌ BookService: Failed to update book', {bookName: name, updates, error});
            throw error;
        }
    }

    static async deleteBook(name: string): Promise<void> {
        try {
            await apiClient.delete(API_ENDPOINTS.books.delete(name));
        } catch (error) {
            console.error('❌ BookService: Failed to delete book', {bookName: name, error});
            throw error;
        }
    }
}
