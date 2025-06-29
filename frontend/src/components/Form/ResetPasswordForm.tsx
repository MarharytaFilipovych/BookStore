import React, {FormEvent, useRef, useState} from 'react';
import styles from './style.module.css';
import {ActionButton} from '../AuthorizationButton/ActionButton';
import {Role} from "../../types";
import {useNavigate, useLocation} from "react-router";
import {Icon} from "../Icon/Icon";

export const ResetPasswordForm: React.FC<{
    onSubmit: (email: string, resetCode: string, newPassword: string, role: Role) => Promise<void>,
    error: string,
    processing: boolean,
    sendAgain: (email: string, role: Role) => Promise<void>
}> = ({onSubmit, error, processing, sendAgain}) => {
    const formRef = useRef<HTMLFormElement>(null);
    const navigate = useNavigate();
    const location = useLocation();
    const [sendAgainMessage, setSendAgainMessage] = useState('');

    const { email, role } = location.state || {};

    const submit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if(!formRef.current) return;

        const formData: FormData = new FormData(formRef.current);
        const resetCode = formData.get('resetCode')?.toString() || '';
        const newPassword = formData.get('newPassword')?.toString() || '';
        const confirmPassword = formData.get('confirmPassword')?.toString() || '';

        if (newPassword !== confirmPassword) {
            alert('Passwords do not match!');
            return;
        }

        await onSubmit(email, resetCode, newPassword, role);
        navigate(`/login/${role}`, {
            state: { message: 'Password reset successful! Please log in with your new password.' }
        });

    };

    const handleSendAgain = async () => {
        if (!email || !role) return;
        await sendAgain(email, role);
        setSendAgainMessage('Reset code sent again! Please check your email.');
        setTimeout(() => setSendAgainMessage(''), 5000);
    };

    if (!email || !role) {
        navigate('/forgot');
        return null;
    }

    return (<>
        {processing && (<Icon topic='loading' size='big' />)}
        <form className={styles.form} onSubmit={submit} ref={formRef}>
            {error && (<p className={styles.errorMessage}>{error}</p>)}
            {sendAgainMessage && (<p className={styles.successMessage}>{sendAgainMessage}</p>)}
            <div className={styles.instructions}>
                <h2>Reset your password!</h2>
                <p>We have sent a reset code to <strong>{email}</strong></p>
                <p>Enter the code from your email along with your new password:</p>
            </div>
            <input name='resetCode' type='text' placeholder='Enter reset code from your email...' required aria-label='Reset Code' autoComplete='off'/>
            <input name='newPassword' type='password' placeholder='New password...' minLength={8} required aria-label='New Password'/>
            <input name='confirmPassword' type='password' placeholder='Confirm new password...' minLength={8} required aria-label='Confirm Password'/>
            <div className={styles.buttons}>
                <ActionButton warning={false} type='reset' form={true} disabled={processing}/>
                <ActionButton warning={false} type='cancel' form={false} disabled={processing}
                              onClick={() => navigate('/forgot')}/>
                <ActionButton warning={false} type='send-again' form={false} disabled={processing}
                              onClick={() => handleSendAgain()}/>
            </div>

        </form></>)
};