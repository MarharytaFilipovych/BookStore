import { apiClient } from '../config/ApiClient';
import {OrderType, PaginatedResponseDTO, OrderSortField, SortOrder} from '../types';
import {API_ENDPOINTS, PAGE_SIZE} from "../BusinessData";

export class OrderService {

    static async getOrders(page = 0, size = PAGE_SIZE, sortBy?: OrderSortField, sortOrder: SortOrder = 'desc'):
        Promise<PaginatedResponseDTO<OrderType>> {
        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) params.append('sort', `${sortBy},${sortOrder}`);
            const response = await apiClient.get<PaginatedResponseDTO<OrderType>>(
                `${API_ENDPOINTS.orders.getAll}?${params.toString()}`
            );
            return response.data;
        } catch (error) {
            console.error('❌ OrderService: Failed to get sorted orders', error);
            throw error;
        }
    }

    static async createOrder(orderData: Omit<OrderType, 'order_date'>): Promise<OrderType> {
        try {
            const response = await apiClient.post<OrderType>
            (API_ENDPOINTS.orders.create, orderData);
            return response.data;
        } catch (error) {
            console.error('❌ OrderService: Failed to create order', {
                clientEmail: orderData.client_email, error});
            throw error;
        }
    }

    static async confirmOrder(orderToConfirm: OrderType, employeeEmail: string): Promise<void> {
        try {
            const orderData: OrderType = {...orderToConfirm, employee_email: employeeEmail};
            await apiClient.put<void>(API_ENDPOINTS.orders.getAll, orderData);
        } catch (error) {
            console.error('❌ OrderService: Failed to confirm order', {
                orderDate: orderToConfirm.order_date,
                clientEmail: orderToConfirm.client_email,
                employeeEmail, error});
            throw error;
        }
    }
}
