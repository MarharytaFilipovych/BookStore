import { apiClient } from '../config/ApiClient';
import {EmployeeType, PaginatedResponseDTO, EmployeeSortField, SortOrder, OrderType} from '../types';
import {API_ENDPOINTS} from "../BusinessData";

export class EmployeeService {

    static async getEmployees(
        page = 0,
        size = 10,
        sortBy?: EmployeeSortField,
        sortOrder: SortOrder = 'asc'
    ): Promise<PaginatedResponseDTO<EmployeeType>> {
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

            const response = await apiClient.get<PaginatedResponseDTO<EmployeeType>>(
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

    static async getEmployeeByEmail(email: string): Promise<EmployeeType> {
        console.log('👔 EmployeeService: Getting employee by email...', { email });

        try {
            const response = await apiClient.get<EmployeeType>(
                API_ENDPOINTS.employees.getByEmail(email)
            );

            console.log('✅ EmployeeService: Person retrieved successfully', {
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

    static async createEmployee(employee: EmployeeType & { password: string }): Promise<EmployeeType> {
        console.log('📝 EmployeeService: Creating new employee...', {
            name: employee.name,
            email: employee.email,
            hasPassword: !!employee.password
        });

        try {
            const response = await apiClient.post<EmployeeType>(
                API_ENDPOINTS.employees.getAll,
                employee
            );
            console.log('✅ EmployeeService: Person created successfully', {
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

    static async updateEmployee(email: string, updates: Partial<EmployeeType>): Promise<EmployeeType> {
        console.log('✏️ EmployeeService: Updating employee...', {
            employeeEmail: email,
            fieldsToUpdate: Object.keys(updates),
            updateCount: Object.keys(updates).length
        });

        try {
            await apiClient.put<void>(API_ENDPOINTS.employees.update(email), updates);
            console.log('✅ EmployeeService: Employee updated successfully (status code received)');
            const updatedEmployee: EmployeeType = {
                email: email,
                phone: updates.phone!,
                birthdate: updates.birthdate!,
                name: updates.name!
            };
            console.log('🔄 EmployeeService: Constructed updated employee object', updatedEmployee);
            return updatedEmployee;

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
            console.log('✅ EmployeeService: Person deleted successfully', {
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
    ): Promise<PaginatedResponseDTO<import('../types').OrderType>> {
        console.log('📦 EmployeeService: Getting employee orders...', {
            employeeEmail, page, size, sortBy, sortOrder
        });

        try {
            const params = new URLSearchParams({page: page.toString(), size: size.toString(),});
            if (sortBy) {
                params.append('sort', `${sortBy},${sortOrder}`);
                console.log(`📊 EmployeeService: Sorting orders by ${sortBy} (${sortOrder})`);
            }
            const response = await apiClient.get<PaginatedResponseDTO<OrderType>>(
                `${API_ENDPOINTS.orders.getByEmployee(employeeEmail)}?${params.toString()}`
            );

            console.log('✅ EmployeeService: Person orders retrieved successfully', {
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
