import { apiClient } from '../config/ApiClient';
import {OrderType, PaginatedResponseDTO, OrderSortField, SortOrder} from '../types';
import {API_ENDPOINTS} from "../BusinessData";

export class OrderService {

    static async getOrders(
        page = 0,
        size = 10,
        sortBy?: OrderSortField,
        sortOrder: SortOrder = 'desc'
    ): Promise<PaginatedResponseDTO<OrderType>> {
        console.log('📦 OrderService: Getting orders with sorting...', {
            page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 OrderService: Sorting by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<OrderType>>(
                `${API_ENDPOINTS.orders.getAll}?${params.toString()}`
            );

            console.log('✅ OrderService: Orders retrieved successfully', {
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default',
                totalOrders: response.data.meta?.total_count || 0,
                ordersOnPage: response.data.orders?.length || 0
            });

            return response.data;

        } catch (error) {
            console.error('❌ OrderService: Failed to get sorted orders', error);
            throw error;
        }
    }

    static async createOrder(orderData: Omit<OrderType, 'order_date'>): Promise<OrderType> {
        console.log('📝 OrderService: Creating new order...', {
            clientEmail: orderData.client_email,
            employeeEmail: orderData.employee_email,
            price: orderData.price,
            itemCount: orderData.book_items.length
        });

        try {
            const response = await apiClient.post<OrderType>(API_ENDPOINTS.orders.create, orderData);
            console.log('✅ OrderService: Order created successfully', {
                orderDate: response.data.order_date,
                clientEmail: response.data.client_email,
                totalPrice: response.data.price,
                items: response.data.book_items.length
            });
            return response.data;
        } catch (error) {
            console.error('❌ OrderService: Failed to create order', {
                clientEmail: orderData.client_email,
                error
            });
            throw error;
        }
    }

    static async confirmOrder(orderToConfirm: OrderType, employeeEmail: string): Promise<void> {
        console.log('✅ OrderService: Confirming order...', {
            orderDate: orderToConfirm.order_date,
            clientEmail: orderToConfirm.client_email,
            employeeEmail
        });

        try {
            const orderData: OrderType = {...orderToConfirm, employee_email: employeeEmail};
            await apiClient.put<void>(API_ENDPOINTS.orders.getAll, orderData);
            console.log('✅ OrderService: Order confirmed successfully', {
                orderDate: orderToConfirm.order_date,
                clientEmail: orderToConfirm.client_email,
                confirmedBy: employeeEmail
            });
        } catch (error) {
            console.error('❌ OrderService: Failed to confirm order', {
                orderDate: orderToConfirm.order_date,
                clientEmail: orderToConfirm.client_email,
                employeeEmail,
                error
            });
            throw error;
        }
    }
}
