import React, {useContext, useState} from "react";
import {Role} from "../../types";
import {AppContext} from "../../context";
import {useLocation, useNavigate} from "react-router";
import {ForgotPasswordForm} from "../../components/Form/ForgotPasswordForm";
import styles from './style.module.css';

export const ForgotPasswordPage: React.FC = () => {
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState('');

    const context = useContext(AppContext);
    const navigate = useNavigate();
    const location = useLocation();

    const currentRole: Role = location.state?.role || context.role || 'CLIENT';

    const handleForgotPassword = async (email: string, role: Role): Promise<void> => {
        setProcessing(true);
        setError('');

        try {
            await context.forgotPassword({ email, role });
            navigate('/reset-password', {state: { email, role }});
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message ||
                err?.message ||
                'Failed to send reset email. Please check your email and try again.';
            setError(errorMessage);
        } finally {
            setProcessing(false);
        }
    };

    return (
        <div className={styles.formContainer}>
                <div className={styles.roleInfo}>
                    <p>Resetting password for... <strong>{currentRole === 'CLIENT' ? 'client' : 'employee'}</strong></p>
                </div>

                <ForgotPasswordForm
                    onSubmit={handleForgotPassword}
                    error={error}
                    processing={processing}
                    role={currentRole}
                />
        </div>
    );
};