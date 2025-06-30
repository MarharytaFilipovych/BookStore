import { apiClient } from '../config/ApiClient';
import {EmployeeType, PaginatedResponseDTO, EmployeeSortField, SortOrder, OrderType} from '../types';
import {API_ENDPOINTS, PAGE_SIZE} from "../BusinessData";

export class EmployeeService {
    static async getEmployees(page = 0, size = PAGE_SIZE, sortBy?: EmployeeSortField, sortOrder: SortOrder = 'asc'):
        Promise<PaginatedResponseDTO<EmployeeType>> {
        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) {params.append('sort', `${sortBy},${sortOrder}`);}
            const response = await apiClient.get<PaginatedResponseDTO<EmployeeType>>(
                `${API_ENDPOINTS.employees.getAll}?${params.toString()}`
            );
            return response.data;
        } catch (error) {
            console.error('❌ EmployeeService: Failed to get sorted employees', error);
            throw error;
        }
    }

    static async getEmployeeByEmail(email: string): Promise<EmployeeType> {
        try {
            const response = await apiClient.get<EmployeeType>(
                API_ENDPOINTS.employees.getByEmail(email));
            return response.data;
        } catch (error) {
            console.error('❌ EmployeeService: Failed to get employee', { email, error });
            throw error;
        }
    }

    static async updateEmployee(email: string, updates: Partial<EmployeeType>): Promise<EmployeeType> {
        try {
            await apiClient.put<void>(API_ENDPOINTS.employees.update(email), updates);
            const updatedEmployee: EmployeeType = {
                email: email,
                phone: updates.phone!,
                birthdate: updates.birthdate!,
                name: updates.name!
            };
            return updatedEmployee;
        } catch (error) {
            console.error('❌ EmployeeService: Failed to update employee', {
                employeeEmail: email, updates, error});
            throw error;
        }
    }

    static async deleteEmployee(email: string): Promise<void> {
        try {
            await apiClient.delete(API_ENDPOINTS.employees.delete(email));
        } catch (error) {
            console.error('❌ EmployeeService: Failed to delete employee', {
                employeeEmail: email, error});
            throw error;
        }
    }

    static async getEmployeeOrders(employeeEmail: string, page = 0, size = PAGE_SIZE,
        sortBy?: 'orderDate' | 'price' | 'client_email' | 'client_name',
        sortOrder: SortOrder = 'desc'
    ): Promise<PaginatedResponseDTO<import('../types').OrderType>> {
         try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) params.append('sort', `${sortBy},${sortOrder}`);
            const response = await apiClient.get<PaginatedResponseDTO<OrderType>>(
                `${API_ENDPOINTS.orders.getByEmployee(employeeEmail)}?${params.toString()}`
            );
            return response.data;
        } catch (error) {
            console.error('❌ EmployeeService: Failed to get employee orders', {
                employeeEmail, sortBy, error});
            throw error;
        }
    }
}
