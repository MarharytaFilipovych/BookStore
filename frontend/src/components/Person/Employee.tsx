import React from 'react';
import styles from './style.module.css';
import {EmployeeType} from "../../types";
import classNames from "classnames";

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
            <div className={classNames(styles.container, styles.employeeContainer)}>
                    <h3 className={styles.name}>{employee.name}</h3>
                    <a href={`mailto:${employee.email}`} className={styles.email}>{employee.email}</a>
                    <a href={`tel:${employee.phone}`} className={styles.number}>{employee.phone}</a>
                <p className={styles.birth}>birthdate ... <strong className={styles.addInfo}>{formatDate(employee.birthdate)}</strong></p>
            </div>
        </>
    );
};
