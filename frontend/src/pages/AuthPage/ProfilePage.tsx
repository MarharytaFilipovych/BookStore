import React, { useState, useContext } from 'react';
import { ClientType, EmployeeType} from '../../types';
import { ClientService } from '../../services/ClientService';
import { EmployeeService } from '../../services/EmployeeService';
import { ProfileUpdateForm } from '../../components/Form/ProfileUpdateForm';
import { AppContext } from '../../context';
import {useNavigate} from "react-router";
import styles from './style.module.css';

export const ProfilePage: React.FC = () => {
    const context = useContext(AppContext);
    const navigate = useNavigate();
    const [error, setError] = useState<string>('');
    const [isUpdating, setIsUpdating] = useState(false);

    if (!context.user) {
        console.log('❌ ProfilePage: User is null, redirecting to login');
        navigate('/');
        return null;
    }

    const userRole = context.role!;
    const user = context.user as ClientType | EmployeeType;

    const handleUpdateProfile = async (updatedData: Partial<ClientType | EmployeeType>) => {
        try {
            setIsUpdating(true);
            if (userRole === 'CLIENT') {
                const updatedClient = await ClientService.updateClient(user.email, updatedData);
                console.log(updatedClient)
                context.setUser(updatedClient);
            } else {
                const updatedEmployee = await EmployeeService.updateEmployee(user.email, updatedData);
                console.log(updatedEmployee)
                context.setUser(updatedEmployee);
            }

            console.log('✅ Profile updated successfully');

        } catch (err) {
            console.error('❌ Failed to update profile:', err);
            setError('Could not update your profile :(((')
        } finally {
            setIsUpdating(false);
        }
    };

    const handleDeleteAccount = async () => {
        setError('')
        setIsUpdating(true);
        try {
            if (userRole === 'CLIENT') await ClientService.deleteClient(user.email);
            else await EmployeeService.deleteEmployee(user.email);

            console.log('✅ Account deleted successfully');
            await context.logout();
            navigate('/');

        } catch (err) {
            console.error('❌ Failed to delete account:', err);
            setError('Could not delete your account. Please try again.');
        }finally {
            setIsUpdating(false);
        }
    };

    return (
        <div className={styles.formContainer}>
                <ProfileUpdateForm
                    user={user}
                    userRole={userRole}
                    onUpdate={handleUpdateProfile}
                    onDeleteAccount={handleDeleteAccount}
                    processing={isUpdating}
                    error={error}
                />
        </div>
    );
};