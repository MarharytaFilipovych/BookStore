import { apiClient } from '../config/ApiClient';
import {
    OrderDTO,
    BookItem,
    PaginatedResponseDTO,
    API_ENDPOINTS,
    OrderSortField,
    SortOrder
} from '../types';
import {BookService} from "./BookService";

export class OrderService {

    static async getOrders(
        page = 0,
        size = 10,
        sortBy?: OrderSortField,
        sortOrder: SortOrder = 'desc'
    ): Promise<PaginatedResponseDTO<OrderDTO>> {
        console.log('📦 OrderService: Getting orders with sorting...', {
            page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 OrderService: Sorting by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<OrderDTO>>(
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

    static async createOrder(orderData: Omit<OrderDTO, 'order_date'>): Promise<OrderDTO> {
        console.log('📝 OrderService: Creating new order...', {
            clientEmail: orderData.client_email,
            employeeEmail: orderData.employee_email,
            price: orderData.price,
            itemCount: orderData.book_items.length
        });

        try {
            const response = await apiClient.post<OrderDTO>(
                API_ENDPOINTS.orders.create,
                orderData
            );

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


    static async createOrderFromBasket(
        clientEmail: string,
        basketItems: BookItem[],
        employeeEmail?: string
    ): Promise<OrderDTO> {
        console.log('🛒 OrderService: Creating order from basket...', {
            clientEmail,
            employeeEmail,
            itemCount: basketItems.length
        });

        try {
            const totalPrice = await OrderService.calculateOrderPrice(basketItems);

            const orderData: Omit<OrderDTO, 'order_date'> = {
                client_email: clientEmail,
                employee_email: employeeEmail,
                price: totalPrice,
                book_items: basketItems
            };

            return OrderService.createOrder(orderData);

        } catch (error) {
            console.error('❌ OrderService: Failed to create order from basket', {
                clientEmail,
                basketItems,
                error
            });
            throw error;
        }
    }

    static async calculateOrderPrice(basketItems: BookItem[]): Promise<number> {
        console.log('💰 OrderService: Calculating order price...', {
            itemCount: basketItems.length
        });

        let totalPrice = 0;

        try {
            // Fetch price for each book in the basket
            for (const item of basketItems) {
                const book = await BookService.getBookByName(item.bookName);
                const itemTotal = book.price * item.quantity;
                totalPrice += itemTotal;

                console.log(`📖 OrderService: ${item.bookName} - $${book.price} x ${item.quantity} = $${itemTotal}`);
            }

            console.log('✅ OrderService: Order price calculated', {
                totalItems: basketItems.reduce((sum, item) => sum + item.quantity, 0),
                totalPrice: totalPrice.toFixed(2)
            });

            return totalPrice;

        } catch (error) {
            console.error('❌ OrderService: Failed to calculate order price', { basketItems, error });
            throw error;
        }
    }


    static async confirmOrder(orderId: string, employeeEmail: string): Promise<OrderDTO> {
        console.log('✅ OrderService: Confirming order...', { orderId, employeeEmail });

        try {
            const response = await apiClient.put<OrderDTO>(
                `${API_ENDPOINTS.orders.getAll}/${orderId}/confirm`,
                { employee_email: employeeEmail }
            );

            console.log('✅ OrderService: Order confirmed successfully', {
                orderId,
                confirmedBy: employeeEmail,
                orderDate: response.data.order_date
            });

            return response.data;

        } catch (error) {
            console.error('❌ OrderService: Failed to confirm order', { orderId, employeeEmail, error });
            throw error;
        }
    }
}
