import React, { useState } from 'react';
import { EmployeeService } from '../../services/EmployeeService';
import { MiniButton } from '../MiniButton/MiniButton';
import styles from './style.module.css';
import {AuthorizationButton} from "../AuthorizationButton/AuthorizationButton";

interface EmployeeSelectionDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onSelectEmployee: (employeeEmail: string) => void;
}

export const EmployeeSelectionDialog: React.FC<EmployeeSelectionDialogProps> = ({isOpen, onClose, onSelectEmployee}) => {
    const [employeeEmail, setEmployeeEmail] = useState<string>('');
    const [isValidating, setIsValidating] = useState(false);
    const [error, setError] = useState<string>('');

    const handleConfirm = async () => {
        if (!employeeEmail.trim()) return;
        setIsValidating(true);
        setError('');

        try {
            await EmployeeService.getEmployeeByEmail(employeeEmail);
            onSelectEmployee(employeeEmail);
            handleClose();
        } catch (error: any) {
            if (error.response?.status === 404) setError('Employee does not exist');
            else setError('Invalid email');
        } finally {
            setIsValidating(false);
        }
    };

    const handleClose = () => {
        setEmployeeEmail('');
        setError('');
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className={styles.overlay} onClick={handleClose}>
            <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
                <div className={styles.dialogHeader}>
                    <h2>Type employee email...</h2>
                    <MiniButton topic='cross' size='premedium' onClick={handleClose} />
                </div>

                <div className={styles.dialogContent}>
                    <input
                        type="email"
                        className={`${styles.emailInput} ${error ? styles.emailInputError : ''}`}
                        placeholder="employee@gmail.com"
                        value={employeeEmail}
                        onChange={(e) => {
                            setEmployeeEmail(e.target.value);
                            setError('');
                        }}
                        onKeyDown={(e) => e.key === 'Enter' && handleConfirm()}
                        disabled={isValidating}
                        autoFocus
                    />
                    {error && <div className={styles.errorMessage}>{error}</div>}
                </div>

                <div className={styles.dialogActions}>
                    <AuthorizationButton type='cancel' onClick={handleClose}/>
                    <AuthorizationButton type='submit' onClick={handleConfirm} disabled={!employeeEmail.trim() || isValidating}/>
                </div>
            </div>
        </div>
    );
};