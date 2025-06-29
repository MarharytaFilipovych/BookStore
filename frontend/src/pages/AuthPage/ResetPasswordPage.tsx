import React, { useState, useContext } from 'react';
import {Role} from "../../types";
import {AppContext} from "../../context";
import {ResetPasswordForm} from "../../components/Form/ResetPasswordForm";
import styles from './style.module.css';

export const ResetPasswordPage: React.FC = () => {
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState('');
    const context = useContext(AppContext);

    const handleResetPassword = async (
        email: string,
        resetCode: string,
        newPassword: string,
        role: Role
    ): Promise<void> => {
        setProcessing(true);
        setError('');

        try {
            await context.resetPassword({
                email,
                password: newPassword,
                reset_code: resetCode,
                role
            });
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message ||
                err?.message ||
                'Invalid or expired reset code. Please check your email or request a new code.';
            setError(errorMessage);
        } finally {
            setProcessing(false);
        }
    };

    const handleSendAgain = async (email: string, role: Role): Promise<void> => {
        setProcessing(true);
        setError('');

        try {
            await context.forgotPassword({ email, role });
            console.log('✅ Reset code sent again successfully');
        } catch (err: any) {
            console.error('❌ Failed to send reset code again:', err);
            const errorMessage = err?.response?.data?.message ||
                err?.message ||
                'Failed to send reset email. Please try again.';
            setError(errorMessage);
        } finally {
            setProcessing(false);
        }
    };

    return (
        <div className={styles.formContainer}>
                <ResetPasswordForm onSubmit={handleResetPassword} error={error} processing={processing} sendAgain={handleSendAgain}/>
        </div>
    );
};