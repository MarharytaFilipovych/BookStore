import { apiClient } from '../config/ApiClient';
import {
    EmployeeDTO,
    PaginatedResponseDTO,
    API_ENDPOINTS,
    EmployeeSortField,
    SortOrder
} from '../types';

export class EmployeeService {

    static async getEmployees(
        page = 0,
        size = 10,
        sortBy?: EmployeeSortField,
        sortOrder: SortOrder = 'asc'
    ): Promise<PaginatedResponseDTO<EmployeeDTO>> {
        console.log('👔 EmployeeService: Getting employees with sorting...', {
            page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 EmployeeService: Sorting by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<EmployeeDTO>>(
                `${API_ENDPOINTS.employees.getAll}?${params.toString()}`
            );

            console.log('✅ EmployeeService: Employees retrieved successfully', {
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default',
                totalEmployees: response.data.meta?.total_count || 0,
                employeesOnPage: response.data.employees?.length || 0
            });

            return response.data;

        } catch (error) {
            console.error('❌ EmployeeService: Failed to get sorted employees', error);
            throw error;
        }
    }

    static async getEmployeeByEmail(email: string): Promise<EmployeeDTO> {
        console.log('👔 EmployeeService: Getting employee by email...', { email });

        try {
            const response = await apiClient.get<EmployeeDTO>(
                API_ENDPOINTS.employees.getByEmail(email)
            );

            console.log('✅ EmployeeService: Employee retrieved successfully', {
                employeeName: response.data.name,
                email: response.data.email,
                phone: response.data.phone
            });

            return response.data;

        } catch (error) {
            console.error('❌ EmployeeService: Failed to get employee', { email, error });
            throw error;
        }
    }

    static async createEmployee(employee: EmployeeDTO & { password: string }): Promise<EmployeeDTO> {
        console.log('📝 EmployeeService: Creating new employee...', {
            name: employee.name,
            email: employee.email,
            hasPassword: !!employee.password
        });

        try {
            const response = await apiClient.post<EmployeeDTO>(
                API_ENDPOINTS.employees.getAll, // POST to /employees
                employee
            );

            console.log('✅ EmployeeService: Employee created successfully', {
                createdEmployee: response.data.name,
                email: response.data.email
            });

            return response.data;

        } catch (error) {
            console.error('❌ EmployeeService: Failed to create employee', {
                employeeEmail: employee.email,
                error
            });
            throw error;
        }
    }

    static async updateEmployee(email: string, updates: Partial<EmployeeDTO>): Promise<EmployeeDTO> {
        console.log('✏️ EmployeeService: Updating employee...', {
            employeeEmail: email,
            fieldsToUpdate: Object.keys(updates),
            updateCount: Object.keys(updates).length
        });

        try {
            const response = await apiClient.put<EmployeeDTO>(
                API_ENDPOINTS.employees.update(email),
                updates
            );

            console.log('✅ EmployeeService: Employee updated successfully', {
                updatedEmployee: response.data.name,
                email: response.data.email,
                fieldsUpdated: Object.keys(updates)
            });

            return response.data;

        } catch (error) {
            console.error('❌ EmployeeService: Failed to update employee', {
                employeeEmail: email,
                updates,
                error
            });
            throw error;
        }
    }

    static async deleteEmployee(email: string): Promise<void> {
        console.log('🗑️ EmployeeService: Deleting employee...', { email });

        try {
            await apiClient.delete(API_ENDPOINTS.employees.delete(email));

            console.log('✅ EmployeeService: Employee deleted successfully', {
                deletedEmployee: email
            });

        } catch (error) {
            console.error('❌ EmployeeService: Failed to delete employee', {
                employeeEmail: email,
                error
            });
            throw error;
        }
    }

    static async getEmployeeOrders(
        employeeEmail: string,
        page = 0,
        size = 10,
        sortBy?: 'orderDate' | 'price' | 'client_email' | 'client_name',
        sortOrder: SortOrder = 'desc'
    ): Promise<PaginatedResponseDTO<import('../types').OrderDTO>> {
        console.log('📦 EmployeeService: Getting employee orders...', {
            employeeEmail, page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 EmployeeService: Sorting orders by ${sortBy} (${sortOrder})`);
            }

            const response = await apiClient.get<PaginatedResponseDTO<import('../types').OrderDTO>>(
                `${API_ENDPOINTS.orders.getByEmployee(employeeEmail)}?${params.toString()}`
            );

            console.log('✅ EmployeeService: Employee orders retrieved successfully', {
                employeeEmail,
                totalOrders: response.data.meta?.total_count || 0,
                ordersOnPage: response.data.orders?.length || 0,
                sortedBy: sortBy ? `${sortBy} (${sortOrder})` : 'default'
            });

            return response.data;

        } catch (error) {
            console.error('❌ EmployeeService: Failed to get employee orders', {
                employeeEmail, sortBy, error
            });
            throw error;
        }
    }
}
