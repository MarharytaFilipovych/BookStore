import React, {useCallback, useContext, useState} from "react";
import {OrderType, OrderFilterState, OrderSortField, SortOrder, ForWhomOrder} from "../../types";
import { OrderService } from "../../services/OrderService";
import { OrderComponent } from "../../components/Order/Order";
import { OrderSearchField } from "../../components/Search/OrderSearchField";
import { EmployeeSelectionDialog } from "../../components/EmployeeSelectionDialog/EmployeeSelectionDialog";
import {clientOrderSortOptions, employeeOrderSortOptions, orderSortOptions} from "../../BusinessData";
import { GenericSearchablePage } from "./GenereicSearchablePage";
import {AppContext} from "../../context";
import {ClientService} from "../../services/ClientService";
import {EmployeeService} from "../../services/EmployeeService";

export const OrdersPage: React.FC<{forWhom: ForWhomOrder}> = ({forWhom}) => {
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState<OrderType | null>(null);
    const [refreshTrigger, setRefreshTrigger] = useState(0);

    const context = useContext(AppContext);

    const getFilterState = (searchParams: URLSearchParams): OrderFilterState => {
        switch (forWhom) {
            case 'all':
                return {
                    clientEmail: searchParams.get('clientEmail') ?? '',
                    employeeEmail: searchParams.get('employeeEmail') ?? '',
                    sort: searchParams.get('sort') ?? ''
                };
            case 'client':
                return {
                    clientEmail: '',
                    employeeEmail: searchParams.get('employeeEmail') ?? '',
                    sort: searchParams.get('sort') ?? ''
                };
            case 'employee':
            default:
                return {
                    employeeEmail: '',
                    clientEmail: searchParams.get('clientEmail') ?? '',
                    sort: searchParams.get('sort') ?? ''
                };
        }
    };
    const fetchClientOrders = async (
        userEmail: string,
        page: number,
        pageSize: number,
        sorting?: { sortBy: OrderSortField; sortOrder: SortOrder }
    ) => {
        console.log('📦 Fetching client orders for:', userEmail);
        return await ClientService.getClientOrders(
            userEmail,
            page,
            pageSize,
            sorting?.sortBy as 'orderDate' | 'price',
            sorting?.sortOrder
        );
    };

    const fetchEmployeeOrders = async (
        userEmail: string,
        page: number,
        pageSize: number,
        sorting?: { sortBy: OrderSortField; sortOrder: SortOrder }
    ) => {
        console.log('📦 Fetching employee orders for:', userEmail);
        return await EmployeeService.getEmployeeOrders(
            userEmail,
            page,
            pageSize,
            sorting?.sortBy as 'orderDate' | 'price' | 'client_email' | 'client_name',
            sorting?.sortOrder
        );
    };

    const fetchOrders = async (
        page: number,
        pageSize: number,
        filter: OrderFilterState,
        sorting?: { sortBy: OrderSortField; sortOrder: SortOrder }
    ): Promise<{ meta: { totalPages: number; total_count: number }; items: OrderType[] }> => {
        let response;
        switch (forWhom){
            case 'client':
                response = await fetchClientOrders(context.user!.email, page, pageSize, sorting);
               break;
            case 'employee':
                response = await fetchEmployeeOrders(context.user!.email, page, pageSize, sorting);
                break;
            case "all":
            default:
                const filterState = getFilterState(new URLSearchParams(window.location.search));
                if (filterState.employeeEmail) {
                    console.log('📦 Fetching orders by employee email:', filterState.employeeEmail);
                    response = await fetchEmployeeOrders(filterState.employeeEmail, page, pageSize, sorting);
                } else if (filterState.clientEmail) {
                    response = await fetchClientOrders(filterState.clientEmail, page, pageSize, sorting);
                } else {
                    console.log('📦 Fetching all orders');
                    response = await OrderService.getOrders(page, pageSize, sorting?.sortBy, sorting?.sortOrder);
                }
                break;
        }
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
            sortOptions={Array.from(getSortOptions().keys())}
            filter={filter}
            onFilterChange={onFilterChange}
            forWhom={forWhom}
        />
    );

    const handleEmployeeSelection = async (employeeEmail: string) => {
        if (!selectedOrder) return;
         console.log('🔄 Assigning employee to order...', {
                orderDate: selectedOrder.order_date,
                clientEmail: selectedOrder.client_email,
                assignedTo: employeeEmail
            });
        await OrderService.confirmOrder(selectedOrder, employeeEmail);
        console.log('✅ Person assigned successfully');
        setRefreshTrigger(prev => prev + 1);
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

    const getSortOptions = () => {
        switch (forWhom) {
            case 'employee': return employeeOrderSortOptions;
            case 'client': return clientOrderSortOptions;
            case 'all': return orderSortOptions;
        }
    };

    return (
        <>
            <GenericSearchablePage<OrderType, OrderFilterState, OrderSortField>
                fetchData={fetchOrders}
                getFilterFromParams={getFilterState}
                sortOptions={getSortOptions()}
                searchComponent={renderSearchComponent}
                renderItem={renderOrder}
                noResultsMessage="No orders found! Try adjusting your search criteria!"
                showResultsCount={true}
                resultsCountText={(count) => `Found ${count} ${count === 1 ? 'order' : 'orders'}!`}
                refreshTrigger={refreshTrigger}
            />

            <EmployeeSelectionDialog
                isOpen={isDialogOpen}
                onClose={() => {
                    setIsDialogOpen(false);
                    setSelectedOrder(null);
                }}
                onSelectEmployee={handleEmployeeSelection}
            />
        </>
    );
};
