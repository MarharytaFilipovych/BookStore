import React, { useState, useContext } from 'react';
import {Role} from "../../types";
import {AppContext} from "../../context";
import {ResetPasswordForm} from "../../components/AuthForm/ResetPasswordForm";



export const ResetPasswordPage: React.FC = () => {
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState('');

    const context = useContext(AppContext);

    if (!context) {
        throw new Error('ResetPasswordPage must be used within AppProvider');
    }

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
                resetCode,
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

    return (
        <ResetPasswordForm
            onSubmit={handleResetPassword}
            error={error}
            processing={processing}
        />
    );
};