import React, { useState, useEffect } from 'react';
import { EmployeeType } from '../../types';
import { EmployeeService } from '../../services/EmployeeService';
import { MiniButton } from '../MiniButton/MiniButton';
import styles from './style.module.css';

interface EmployeeSelectionDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onSelectEmployee: (employeeEmail: string) => void;
    orderInfo?: {
        orderDate: string;
        clientEmail: string;
    };
}

export const EmployeeSelectionDialog: React.FC<EmployeeSelectionDialogProps> = ({
                                                                                    isOpen,
                                                                                    onClose,
                                                                                    onSelectEmployee,
                                                                                    orderInfo
                                                                                }) => {
    const [employees, setEmployees] = useState<EmployeeType[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [selectedEmployee, setSelectedEmployee] = useState<string>('');

    useEffect(() => {
        if (isOpen) {
            fetchEmployees();
        }
    }, [isOpen]);

    const fetchEmployees = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await EmployeeService.getEmployees(0, 100); // Get all employees
            setEmployees(response.employees || []);
        } catch (err) {
            console.error('Failed to fetch employees:', err);
            setError('Failed to load employees. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleConfirm = () => {
        if (selectedEmployee) {
            onSelectEmployee(selectedEmployee);
            handleClose();
        }
    };

    const handleClose = () => {
        setSelectedEmployee('');
        setError(null);
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className={styles.overlay} onClick={handleClose}>
            <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
                <div className={styles.dialogHeader}>
                    <h2>Assign Employee to Order</h2>
                    <MiniButton
                        topic='cross'
                        size='mini'
                        onClick={handleClose}
                    />
                </div>

                {orderInfo && (
                    <div className={styles.orderInfo}>
                        <p><strong>Order Date:</strong> {new Date(orderInfo.orderDate).toLocaleDateString()}</p>
                        <p><strong>Client:</strong> {orderInfo.clientEmail}</p>
                    </div>
                )}

                <div className={styles.dialogContent}>
                    {loading && (
                        <div className={styles.loading}>
                            <p>Loading employees...</p>
                        </div>
                    )}

                    {error && (
                        <div className={styles.error}>
                            <p>{error}</p>
                            <button onClick={fetchEmployees} className={styles.retryButton}>
                                Retry
                            </button>
                        </div>
                    )}

                    {!loading && !error && employees.length === 0 && (
                        <div className={styles.noEmployees}>
                            <p>No employees available</p>
                        </div>
                    )}

                    {!loading && !error && employees.length > 0 && (
                        <div className={styles.employeeList}>
                            <h3>Select an employee:</h3>
                            <div className={styles.employeeOptions}>
                                {employees.map((employee) => (
                                    <label
                                        key={employee.email}
                                        className={`${styles.employeeOption} ${
                                            selectedEmployee === employee.email ? styles.selected : ''
                                        }`}
                                    >
                                        <input
                                            type="radio"
                                            name="employee"
                                            value={employee.email}
                                            checked={selectedEmployee === employee.email}
                                            onChange={(e) => setSelectedEmployee(e.target.value)}
                                        />
                                        <div className={styles.employeeInfo}>
                                            <div className={styles.employeeName}>{employee.name}</div>
                                            <div className={styles.employeeEmail}>{employee.email}</div>
                                            {employee.phone && (
                                                <div className={styles.employeePhone}>{employee.phone}</div>
                                            )}
                                        </div>
                                    </label>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                <div className={styles.dialogActions}>
                    <button
                        onClick={handleClose}
                        className={styles.cancelButton}
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={!selectedEmployee || loading}
                        className={styles.confirmButton}
                    >
                        Assign Employee
                    </button>
                </div>
            </div>
        </div>
    );
};