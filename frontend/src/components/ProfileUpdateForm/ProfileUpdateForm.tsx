import React, { useState } from 'react';
import { ClientType, EmployeeType, Role } from '../../types';
import { MiniButton } from '../MiniButton/MiniButton';
import styles from './style.module.css';

interface ProfileUpdateFormProps {
    user: ClientType | EmployeeType;
    userRole: Role;
    onUpdate: (updatedData: Partial<ClientType | EmployeeType>) => Promise<void>;
    onDeleteAccount: () => void;
    isUpdating: boolean;
}

export const ProfileUpdateForm: React.FC<ProfileUpdateFormProps> = ({
                                                                        user,
                                                                        userRole,
                                                                        onUpdate,
                                                                        onDeleteAccount,
                                                                        isUpdating
                                                                    }) => {
    const [formData, setFormData] = useState(() => {
        if (userRole === 'CLIENT') {
            const client = user as ClientType;
            return {
                name: client.name,
                email: client.email, // Read-only
                balance: client.balance?.toString() || '0'
            };
        } else {
            const employee = user as EmployeeType;
            return {
                name: employee.name,
                email: employee.email, // Read-only
                phone: employee.phone || '',
                birthdate: employee.birthdate || ''
            };
        }
    });

    const [showDeleteWarning, setShowDeleteWarning] = useState(false);
    const [errors, setErrors] = useState<Record<string, string>>({});

    const handleInputChange = (field: string, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        // Clear error when user starts typing
        if (errors[field]) {
            setErrors(prev => ({
                ...prev,
                [field]: ''
            }));
        }
    };

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Name is required';
        }

        if (userRole === 'EMPLOYEE') {
            if (!formData.phone?.trim()) {
                newErrors.phone = 'Phone is required';
            }
            if (!formData.birthdate) {
                newErrors.birthdate = 'Birth date is required';
            }
        }

        if (userRole === 'CLIENT') {
            const balance = parseFloat(formData.balance || '0');
            if (isNaN(balance) || balance < 0) {
                newErrors.balance = 'Balance must be a valid positive number';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            if (userRole === 'CLIENT') {
                await onUpdate({
                    name: formData.name,
                    balance: parseFloat(formData.balance || '0')
                });
            } else {
                await onUpdate({
                    name: formData.name,
                    phone: formData.phone,
                    birthdate: formData.birthdate
                });
            }
        } catch (error) {
            console.error('Failed to update profile:', error);
        }
    };

    const handleDeleteAccount = () => {
        setShowDeleteWarning(false);
        onDeleteAccount();
    };

    return (
        <div className={styles.profileForm}>
            <div className={styles.formHeader}>
                <h2>Update Profile</h2>
                <div className={styles.userRole}>
                    <span className={`${styles.roleBadge} ${styles[userRole.toLowerCase()]}`}>
                        {userRole}
                    </span>
                </div>
            </div>

            <form onSubmit={handleSubmit} className={styles.form}>
                {/* Email - Read Only */}
                <div className={styles.formGroup}>
                    <label className={styles.label}>Email</label>
                    <input
                        type="email"
                        value={formData.email}
                        className={`${styles.input} ${styles.readOnly}`}
                        readOnly
                        disabled
                    />
                    <span className={styles.helperText}>Email cannot be changed</span>
                </div>

                {/* Name */}
                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Name <span className={styles.required}>*</span>
                    </label>
                    <input
                        type="text"
                        value={formData.name}
                        onChange={(e) => handleInputChange('name', e.target.value)}
                        className={`${styles.input} ${errors.name ? styles.error : ''}`}
                        placeholder="Enter your full name"
                    />
                    {errors.name && <span className={styles.errorText}>{errors.name}</span>}
                </div>

                {/* Client-specific fields */}
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
                            placeholder="0.00"
                        />
                        {errors.balance && <span className={styles.errorText}>{errors.balance}</span>}
                    </div>
                )}

                {/* Employee-specific fields */}
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
                                placeholder="Enter your phone number"
                            />
                            {errors.phone && <span className={styles.errorText}>{errors.phone}</span>}
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
                            />
                            {errors.birthdate && <span className={styles.errorText}>{errors.birthdate}</span>}
                        </div>
                    </>
                )}

                {/* Form Actions */}
                <div className={styles.formActions}>
                    <button
                        type="submit"
                        disabled={isUpdating}
                        className={styles.updateButton}
                    >
                        {isUpdating ? 'Updating...' : 'Update Profile'}
                    </button>

                    <button
                        type="button"
                        onClick={() => setShowDeleteWarning(true)}
                        className={styles.deleteButton}
                    >
                        Delete Account
                    </button>
                </div>
            </form>

            {/* Delete Account Warning Modal */}
            {showDeleteWarning && (
                <div className={styles.overlay} onClick={() => setShowDeleteWarning(false)}>
                    <div className={styles.warningModal} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.warningHeader}>
                            <h3>⚠️ Delete Account</h3>
                            <MiniButton
                                topic='cross'
                                size='mini'
                                onClick={() => setShowDeleteWarning(false)}
                            />
                        </div>

                        <div className={styles.warningContent}>
                            <p>Are you sure you want to delete your account?</p>
                            <p className={styles.warningText}>
                                This action cannot be undone. All your data will be permanently removed.
                            </p>
                        </div>

                        <div className={styles.warningActions}>
                            <button
                                onClick={() => setShowDeleteWarning(false)}
                                className={styles.cancelButton}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleDeleteAccount}
                                className={styles.confirmDeleteButton}
                            >
                                Delete Account
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
