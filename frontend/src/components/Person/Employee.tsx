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
            <div className={styles.container}>
                    <h3 className={styles.name}>{employee.name}</h3>
                    <p className={styles.email}>{employee.email}</p>
                    <p className={styles.number}>{employee.phone}</p>
                <p >birthdate ... <strong className={styles.addInfo}>{formatDate(employee.birthdate)}</strong></p>
            </div>
        </>
    );
};