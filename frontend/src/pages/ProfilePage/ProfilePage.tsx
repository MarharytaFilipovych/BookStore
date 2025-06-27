import React, { useState, useEffect, useContext } from 'react';
import { ClientType, EmployeeType, Role } from '../../types';
import { ClientService } from '../../services/ClientService';
import { EmployeeService } from '../../services/EmployeeService';
import { ProfileUpdateForm } from '../../components/ProfileUpdateForm/ProfileUpdateForm';
import { Icon } from '../../components/Icon/Icon';
import { AppContext } from '../../context';
import styles from './style.module.css';

export const ProfilePage: React.FC = () => {
    const context = useContext(AppContext);
    const [user, setUser] = useState<ClientType | EmployeeType | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isUpdating, setIsUpdating] = useState(false);

    const userRole = context.role!;
    const userEmail = context.user!.email!;

    useEffect(() => {
        fetchUserProfile();
    }, []);

    const fetchUserProfile = async () => {
        if (!userEmail || !userRole) {
            setError('User not logged in');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);

            if (userRole === 'CLIENT') {
                const client = await ClientService.getClientByEmail(userEmail);
                setUser(client);
            } else {
                const employee = await EmployeeService.getEmployeeByEmail(userEmail);
                setUser(employee);
            }
        } catch (err) {
            console.error('Failed to fetch user profile:', err);
            setError('Failed to load profile. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateProfile = async (updatedData: Partial<ClientType | EmployeeType>) => {
        if (!userEmail || !userRole) return;

        try {
            setIsUpdating(true);

            if (userRole === 'CLIENT') {
                const updatedClient = await ClientService.updateClient(userEmail, updatedData);
                setUser(updatedClient);
            } else {
                const updatedEmployee = await EmployeeService.updateEmployee(userEmail, updatedData);
                setUser(updatedEmployee);
            }

            // Show success message
            console.log('✅ Profile updated successfully');

        } catch (err) {
            console.error('❌ Failed to update profile:', err);
            throw err; // Re-throw to let form handle the error
        } finally {
            setIsUpdating(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (!userEmail || !userRole) return;

        try {
            if (userRole === 'CLIENT') {
                await ClientService.deleteClient(userEmail);
            } else {
                await EmployeeService.deleteEmployee(userEmail);
            }

            // Logout user and redirect
            console.log('✅ Account deleted successfully');
            // You might want to call context.logout() here
            // and redirect to login page

        } catch (err) {
            console.error('❌ Failed to delete account:', err);
        }
    };

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <Icon topic='loading' size='big' />
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.errorContainer}>
                <Icon topic='error' size='big' />
                <h2>Error</h2>
                <p>{error}</p>
                <button onClick={fetchUserProfile} className={styles.retryButton}>
                    Try Again
                </button>
            </div>
        );
    }

    if (!user) {
        return (
            <div className={styles.errorContainer}>
                <h2>User not found</h2>
                <p>Unable to load profile information.</p>
            </div>
        );
    }

    return (
        <div className={styles.profilePage}>
            <div className={styles.profileHeader}>
                <h1>My Profile</h1>
                <p>Manage your account information and settings</p>
            </div>

            <ProfileUpdateForm
                user={user}
                userRole={userRole}
                onUpdate={handleUpdateProfile}
                onDeleteAccount={handleDeleteAccount}
                isUpdating={isUpdating}
            />
        </div>
    );
};
