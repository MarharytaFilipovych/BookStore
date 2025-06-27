import React, { useState } from "react";
import { OrderType, OrderFilterState, OrderSortField, SortOrder } from "../../types";
import { OrderService } from "../../services/OrderService";
import { OrderComponent } from "../../components/Order/Order";
import { OrderSearchField } from "../../components/Search/OrderSearchField";
import { EmployeeSelectionDialog } from "../../components/EmployeeSelectionDialog/EmployeeSelectionDialog";
import { orderSortOptions } from "../../BusinessData";
import styles from './style.module.css';
import { GenericSearchablePage } from "./GenereicSearchablePage";

export const OrdersPage: React.FC = () => {
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState<OrderType | null>(null);

    const getFilterState = (searchParams: URLSearchParams): OrderFilterState => ({
        clientEmail: searchParams.get('clientEmail') ?? '',
        employeeEmail: searchParams.get('employeeEmail') ?? '',
        sort: searchParams.get('sort') ?? ''
    });

    const fetchOrders = async (
        page: number,
        pageSize: number,
        filter: OrderFilterState,
        sorting?: { sortBy: OrderSortField; sortOrder: SortOrder }
    ): Promise<{ meta: { totalPages: number; total_count: number }; items: OrderType[] }> => {
        const response = await OrderService.getOrders(
            page,
            pageSize,
            sorting?.sortBy,
            sorting?.sortOrder
        );

        return {
            meta: {
                totalPages: response.meta.totalPages,
                total_count: response.meta.total_count
            },
            items: response.orders || []
        };
    };

    const renderSearchComponent = ({ filter, onFilterChange }: {
        filter: OrderFilterState;
        onFilterChange: (key: keyof OrderFilterState, value: string) => void;
    }) => (
        <OrderSearchField
            sortOptions={Array.from(orderSortOptions.keys())}
            filter={filter}
            onFilterChange={onFilterChange}
        />
    );

    const handleEmployeeSelection = async (employeeEmail: string) => {
        if (!selectedOrder) return;

        try {
            console.log('🔄 Assigning employee to order...', {
                orderDate: selectedOrder.order_date,
                clientEmail: selectedOrder.client_email,
                assignedTo: employeeEmail
            });

            await OrderService.confirmOrder(selectedOrder, employeeEmail);

            console.log('✅ Person assigned successfully');

            // Refresh the page or update state
            window.location.reload();

        } catch (error) {
            console.error('❌ Failed to assign employee to order:', error);
            // You might want to show an error notification here
        }
    };

    const renderOrder = (order: OrderType, index: number) => (
        <OrderComponent
            key={`${order.order_date}-${index}`}
            {...order}
            onAssignEmployee={(orderId: string) => {
                console.log('Opening employee selection dialog for order:', orderId);
                setSelectedOrder(order);
                setIsDialogOpen(true);
            }}
        />
    );

    return (
        <>
            <GenericSearchablePage<OrderType, OrderFilterState, OrderSortField>
                fetchData={fetchOrders}
                getFilterFromParams={getFilterState}
                sortOptions={orderSortOptions}
                searchComponent={renderSearchComponent}
                renderItem={renderOrder}
                noResultsMessage="No orders found! Try adjusting your search criteria!"
                showResultsCount={true}
                resultsCountText={(count) => `Found ${count} orders!`}
            />

            <EmployeeSelectionDialog
                isOpen={isDialogOpen}
                onClose={() => {
                    setIsDialogOpen(false);
                    setSelectedOrder(null);
                }}
                onSelectEmployee={handleEmployeeSelection}
                orderInfo={selectedOrder ? {
                    orderDate: selectedOrder.order_date,
                    clientEmail: selectedOrder.client_email
                } : undefined}
            />
        </>
    );
};
