import { apiClient } from '../config/ApiClient';
import {
    Client,
    PaginatedResponseDTO,
    API_ENDPOINTS,
    ClientSortField,
    SortOrder
} from '../types';

export class ClientService {

    static async getClients(
        page = 0,
        size = 10,
        sortBy?: ClientSortField,
        sortOrder: SortOrder = 'asc'
    ): Promise<PaginatedResponseDTO<Client>> {
        console.log('👥 ClientService: Getting clients with sorting...', {
            page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 ClientService: Sorting by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<Client>>(
                `${API_ENDPOINTS.clients.getAll}?${params.toString()}`
            );

            console.log('✅ ClientService: Clients retrieved successfully', {
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default',
                totalClients: response.data.meta?.total_count || 0,
                clientsOnPage: response.data.clients?.length || 0
            });

            return response.data;

        } catch (error) {
            console.error('❌ ClientService: Failed to get sorted clients', error);
            throw error;
        }
    }

    static async getClientByEmail(email: string): Promise<Client> {
        console.log('👤 ClientService: Getting client by email...', { email });

        try {
            const response = await apiClient.get<Client>(
                API_ENDPOINTS.clients.getByEmail(email)
            );

            console.log('✅ ClientService: Client retrieved successfully', {
                clientName: response.data.name,
                email: response.data.email,
                balance: response.data.balance
            });

            return response.data;

        } catch (error) {
            console.error('❌ ClientService: Failed to get client', { email, error });
            throw error;
        }
    }

    static async createClient(client: Omit<Client, 'balance'> & { password: string }): Promise<Client> {
        console.log('📝 ClientService: Creating new client...', {
            name: client.name,
            email: client.email,
            hasPassword: !!client.password
        });

        try {
            const response = await apiClient.post<Client>(
                API_ENDPOINTS.clients.getAll, // POST to /clients
                client
            );

            console.log('✅ ClientService: Client created successfully', {
                createdClient: response.data.name,
                email: response.data.email,
                balance: response.data.balance
            });

            return response.data;

        } catch (error) {
            console.error('❌ ClientService: Failed to create client', {
                clientEmail: client.email,
                error
            });
            throw error;
        }
    }

    static async updateClient(email: string, updates: Partial<Client>): Promise<Client> {
        console.log('✏️ ClientService: Updating client...', {
            clientEmail: email,
            fieldsToUpdate: Object.keys(updates),
            updateCount: Object.keys(updates).length
        });

        try {
            const response = await apiClient.put<Client>(
                API_ENDPOINTS.clients.update(email),
                updates
            );

            console.log('✅ ClientService: Client updated successfully', {
                updatedClient: response.data.name,
                email: response.data.email,
                fieldsUpdated: Object.keys(updates)
            });

            return response.data;

        } catch (error) {
            console.error('❌ ClientService: Failed to update client', {
                clientEmail: email,
                updates,
                error
            });
            throw error;
        }
    }

    static async deleteClient(email: string): Promise<void> {
        console.log('🗑️ ClientService: Deleting client...', { email });

        try {
            await apiClient.delete(API_ENDPOINTS.clients.delete(email));

            console.log('✅ ClientService: Client deleted successfully', {
                deletedClient: email
            });

        } catch (error) {
            console.error('❌ ClientService: Failed to delete client', {
                clientEmail: email,
                error
            });
            throw error;
        }
    }

    static async blockClient(email: string, reason?: string): Promise<void> {
        console.log('🚫 ClientService: Blocking client...', { email, reason });

        try {
            const blockData = reason ? { reason } : {};

            await apiClient.put(
                `${API_ENDPOINTS.clients.getByEmail(email)}/block`,
                blockData
            );

            console.log('✅ ClientService: Client blocked successfully', {
                blockedClient: email,
                reason
            });

        } catch (error) {
            console.error('❌ ClientService: Failed to block client', {
                clientEmail: email,
                reason,
                error
            });
            throw error;
        }
    }

    static async unblockClient(email: string): Promise<void> {
        console.log('✅ ClientService: Unblocking client...', { email });

        try {
            await apiClient.put(`${API_ENDPOINTS.clients.getByEmail(email)}/unblock`);

            console.log('✅ ClientService: Client unblocked successfully', {
                unblockedClient: email
            });

        } catch (error) {
            console.error('❌ ClientService: Failed to unblock client', {
                clientEmail: email,
                error
            });
            throw error;
        }
    }

    static async getClientOrders(
        clientEmail: string,
        page = 0,
        size = 10,
        sortBy?: 'orderDate' | 'price',
        sortOrder: SortOrder = 'desc'
    ): Promise<PaginatedResponseDTO<import('../types').OrderDTO>> {
        console.log('📦 ClientService: Getting client orders...', {
            clientEmail, page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            // Add sorting if specified
            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 ClientService: Sorting orders by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<import('../types').OrderDTO>>(
                `${API_ENDPOINTS.orders.getByClient(clientEmail)}?${params.toString()}`
            );

            console.log('✅ ClientService: Client orders retrieved successfully', {
                clientEmail,
                totalOrders: response.data.meta?.total_count || 0,
                ordersOnPage: response.data.orders?.length || 0,
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default'
            });

            return response.data;

        } catch (error) {
            console.error('❌ ClientService: Failed to get client orders', {
                clientEmail, sortBy, error
            });
            throw error;
        }
    }
}
