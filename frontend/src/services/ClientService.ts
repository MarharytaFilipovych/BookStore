import { apiClient } from '../config/ApiClient';
import {ClientType, PaginatedResponseDTO, ClientSortField, SortOrder, OrderType} from '../types';
import {API_ENDPOINTS, PAGE_SIZE} from "../BusinessData";

export class ClientService {
    static async getClients(page = 0, size = PAGE_SIZE, sortBy?: ClientSortField, sortOrder: SortOrder = 'asc'):
        Promise<PaginatedResponseDTO<ClientType>> {
          try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) params.append('sort', `${sortBy},${sortOrder}`);
            const response = await apiClient.get<PaginatedResponseDTO<ClientType>>(
                `${API_ENDPOINTS.clients.getAll}?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error('❌ ClientService: Failed to get sorted clients', error);
            throw error;
        }
    }

    static async getClientByEmail(email: string): Promise<ClientType> {
        try {
            const response = await apiClient.get<ClientType>
            (API_ENDPOINTS.clients.getByEmail(email));
            return response.data;
        } catch (error) {
            console.error('❌ ClientService: Failed to get client', { email, error });
            throw error;
        }
    }

    static async updateClient(email: string, updates: Partial<ClientType>): Promise<ClientType> {
        try {
            await apiClient.put<void>(API_ENDPOINTS.clients.update(email), updates);
            const updatedClient: ClientType = {
                name: updates.name!,
                balance: updates.balance!,
                email: email
            };
            return updatedClient;
        } catch (error) {
            console.error('❌ ClientService: Failed to update client', {clientEmail: email, updates, error});
            throw error;
        }
    }

    static async deleteClient(email: string): Promise<void> {
        try {
            await apiClient.delete(API_ENDPOINTS.clients.delete(email));
        } catch (error) {
            console.error('❌ ClientService: Failed to delete client', {clientEmail: email, error});
            throw error;
        }
    }

    static async blockClient(email: string, reason?: string): Promise<void> {
        try {
            const blockData = reason ? { reason } : {};
            await apiClient.post(API_ENDPOINTS.clients.block(email), blockData);
        } catch (error) {
            console.error('❌ ClientService: Failed to block client', {
                clientEmail: email, reason, error});
            throw error;
        }
    }

    static async unblockClient(email: string): Promise<void> {
        try {
            await apiClient.delete(API_ENDPOINTS.clients.unblock(email));
        } catch (error) {
            console.error('❌ ClientService: Failed to unblock client', {clientEmail: email, error});
            throw error;
        }
    }

    static async getBlockedClientsList(): Promise<ClientType[]> {
        try {
            const response = await apiClient.get<ClientType[]>(
                API_ENDPOINTS.clients.getAllBlockedList());
            return response.data || [];
        } catch (error) {
            console.error('❌ ClientService: Failed to get blocked clients', { error });
            throw error;
        }
    }

    static async getBlockedClients(page = 0, size = PAGE_SIZE, sortBy?: ClientSortField, sortOrder: SortOrder = 'asc'):
        Promise<PaginatedResponseDTO<ClientType>> {
        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) params.append('sort', `${sortBy},${sortOrder}`);
            const response = await apiClient.get<PaginatedResponseDTO<ClientType>>(
                `${API_ENDPOINTS.clients.getAllBlocked()}?${params.toString()}`
            );
            return response.data;
        } catch (error) {
            console.error('❌ ClientService: Failed to get sorted clients', error);
            throw error;
        }
    }

    static async getClientOrders(clientEmail: string, page = 0, size = PAGE_SIZE,
                                 sortBy?: 'orderDate' | 'price' | 'employee_name' | 'employee_email',
                                sortOrder: SortOrder = 'desc'):
        Promise<PaginatedResponseDTO<OrderType>> {
        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) params.append('sort', `${sortBy},${sortOrder}`);
            const response = await apiClient.get<PaginatedResponseDTO<import('../types').OrderType>>(
                `${API_ENDPOINTS.orders.getByClient(clientEmail)}?${params.toString()}`);
            return response.data;

        } catch (error) {
            console.error('❌ ClientService: Failed to get client orders', {
                clientEmail, sortBy, error});
            throw error;
        }
    }
}
