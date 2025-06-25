import React, {FormEvent, useRef} from 'react';
import styles from './style.module.css';
import {AuthorizationButton} from '../AuthorizationButton/AuthorizationButton';
import {Role} from "../../types";
import {useNavigate} from "react-router";

export const ForgotPasswordForm: React.FC<{
    onSubmit: (email: string, role: Role) => void,
    error: string,
    processing: boolean,
    role: Role
}> = ({onSubmit, error, processing, role}) => {
    const formRef = useRef<HTMLFormElement>(null);
    const navigate = useNavigate();

    const submit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if(!formRef.current) return;
        const formData: FormData = new FormData(formRef.current);
        const email = formData.get('email')?.toString() || '';
        onSubmit(email, role);
    };

    return (
        <form className={styles.form} onSubmit={submit} ref={formRef}>
            {error && (<p className={styles.errorMessage}>{error}</p>)}

            <h2>Reset your password!</h2>
            <p>Enter your email address and we'll redirect you to the change password form:)</p>

            <input
                name='email'
                type='email'
                id='email'
                placeholder='your email...'
                required
                aria-label='Email'
            />

            <AuthorizationButton
                warning={false}
                type='forgot'
                form={true}
                disabled={processing}
            />

            <AuthorizationButton
                warning={false}
                type='cancel'
                form={false}
                disabled={processing}
                onClick={() => navigate('/login')}
            />
        </form>
    );
};