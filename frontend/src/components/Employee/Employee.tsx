import React, {useContext, useState} from 'react';
import styles from './style.module.css';
import {EmployeeType} from "../../types";

export const EmployeeComponent: React.FC<EmployeeType> = (employee) => {
    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <>
            <div className={styles.employeeContainer}>
                    <h3 className={styles.employeeName}>{employee.name}</h3>
                    <p className={styles.employeeEmail}>{employee.email}</p>
                    <p className={styles.phoneNumber}>{employee.phone}</p>
                <p className={styles.ageNumber}>birthdate ... <strong>{formatDate(employee.birthdate)}</strong></p>
            </div>
        </>
    );
};