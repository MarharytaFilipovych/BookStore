import React, { useState } from 'react';
import { ClientType, EmployeeType, Role } from '../../types';
import styles from './style.module.css';
import { ActionButton } from "../AuthorizationButton/ActionButton";
import { Warning } from "../Warning/Warning";
import { useNavigate } from "react-router";

type ProfileUpdateFormProps = {
    user: ClientType | EmployeeType;
    userRole: Role;
    onUpdate: (updatedData: Partial<ClientType | EmployeeType>) => Promise<void>;
    onDeleteAccount: () => void;
    processing?: boolean;
    error: string;
}

export const ProfileUpdateForm: React.FC<ProfileUpdateFormProps> = ({user, userRole, onUpdate, onDeleteAccount, processing = false, error = ''}) => {
    const [warning, setWarning] = useState<boolean>(false);
    const navigate = useNavigate();

    const [formData, setFormData] = useState(() => {
        if (userRole === 'CLIENT') {
            const client = user as ClientType;
            return {
                name: client.name,
                email: client.email,
                balance: client.balance?.toString() || '0'
            };
        } else {
            const employee = user as EmployeeType;
            return {
                name: employee.name,
                email: employee.email,
                phone: employee.phone || '',
                birthdate: employee.birthdate || ''
            };
        }
    });

    const [errors, setErrors] = useState<Record<string, string>>({});

    const handleInputChange = (field: string, value: string) => {
        setFormData(prev => ({...prev, [field]: value}));
        if (errors[field]) setErrors(prev => ({...prev, [field]: ''}));
    };

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};
        if (!formData.name.trim()) newErrors.name = 'Name is required';
        if (userRole === 'EMPLOYEE') {
            if (!formData.phone?.trim()) newErrors.phone = 'Phone is required';
            if (!formData.birthdate) newErrors.birthdate = 'Birth date is required';
        }
        if (userRole === 'CLIENT') {
            const balance = parseFloat(formData.balance || '0');
            if (isNaN(balance) || balance < 0) newErrors.balance = 'Balance must be a valid positive number';
        }
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;
        try {
            if (userRole === 'CLIENT') await onUpdate({name: formData.name, balance: parseFloat(formData.balance || '0')});
            else await onUpdate({name: formData.name, phone: formData.phone, birthdate: formData.birthdate});
            alert("Your profile was updated!");
        } catch (error) {
            console.error('Failed to update profile:', error);
            error = 'Could not update your profile! ';
        }
    };

    return (
        <>
            {warning && (
                <Warning
                    onClick={()=>{
                        setWarning(false);
                        onDeleteAccount();
                    }}
                    onCancel={() => setWarning(false)}
                    purpose='delete'
                    message={'Are you sure about deleting your account?'}
                />
            )}
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.instructions}><h2>Update your profile!</h2></div>
                {error && (<p className={styles.errorMessage}>{error}</p>)}
                <div className={styles.formGroup}>
                    <input
                        type="email"
                        value={formData.email}
                        className={`${styles.input} ${styles.readOnly}`}
                        readOnly
                        disabled
                    />
                </div>
                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Name <span className={styles.required}>*</span>
                    </label>
                    <input
                        type="text"
                        value={formData.name}
                        onChange={(e) => handleInputChange('name', e.target.value)}
                        className={`${styles.input} ${errors.name ? styles.error : ''}`}
                        placeholder="type your name..."
                        required
                    />
                    {errors.name && <span className={styles.errorMessage}>{errors.name}</span>}
                </div>
                {userRole === 'CLIENT' && (
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Balance</label>
                        <input
                            type="number"
                            step="0.01"
                            min="0"
                            value={formData.balance}
                            onChange={(e) => handleInputChange('balance', e.target.value)}
                            className={`${styles.input} ${errors.balance ? styles.error : ''}`}
                            placeholder="type your balance (0.00)..."
                            required
                        />
                        {errors.balance && <span className={styles.errorMessage}>{errors.balance}</span>}
                    </div>
                )}
                {userRole === 'EMPLOYEE' && (
                    <>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Phone <span className={styles.required}>*</span>
                            </label>
                            <input
                                type="tel"
                                value={formData.phone}
                                onChange={(e) => handleInputChange('phone', e.target.value)}
                                className={`${styles.input} ${errors.phone ? styles.error : ''}`}
                                placeholder="type your number..."
                                required
                            />
                            {errors.phone && <span className={styles.errorMessage}>{errors.phone}</span>}
                        </div>

                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Birth Date <span className={styles.required}>*</span>
                            </label>
                            <input
                                type="date"
                                value={formData.birthdate}
                                onChange={(e) => handleInputChange('birthdate', e.target.value)}
                                className={`${styles.input} ${errors.birthdate ? styles.error : ''}`}
                                placeholder="type your birthdate..."
                                required
                            />
                            {errors.birthdate && <span className={styles.errorMessage}>{errors.birthdate}</span>}
                        </div>
                    </>
                )}
                <div className={styles.buttons}>
                    <ActionButton type='delete' form={true} disabled={processing} onClick={() => setWarning(true)}/>
                    <ActionButton type='submit' form={true} disabled={processing}/>
                    <ActionButton type='forgot' form={false} disabled={processing} onClick={() => navigate('/forgot', { state: { role: userRole } })}/>
                </div>
            </form>
        </>
    );
};