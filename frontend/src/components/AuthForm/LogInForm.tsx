import React, {FormEvent, useRef} from 'react';
import styles from './style.module.css';
import {AuthorizationButton} from '../AuthorizationButton/AuthorizationButton';
import {Role} from "../../types";
import {useNavigate} from "react-router";
import {Icon} from "../Icon/Icon";

export const LogInForm: React.FC<{
    onSubmit: (email: string, password: string, user: Role) => void,
    passError: boolean,
    processing: boolean,
    user: Role
}> = ({onSubmit, passError, processing, user}) => {
    const formRef = useRef<HTMLFormElement>(null);
    const navigate = useNavigate();
    const submit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if(!formRef.current) return;
        const formData: FormData = new FormData(formRef.current);
        const email = formData.get('email')?.toString() || '';
        const password = formData.get('password')?.toString() || '';
        onSubmit(email, password, user);
    };

    return (<>
        {processing && (<Icon topic='loading' size='big' />)}
        <form className={styles.form} onSubmit={submit} ref={formRef}>
            <div className={styles.instructions}>
                <h2>Login!</h2>
            </div>
            {passError && (<p className={styles.errorMessage}>Login or password is incorrect!</p>)}
            <input
                name='email'
                type='email'
                id='email'
                placeholder='your email...'
                required
                aria-label='Email'
            />
            <input
                name='password'
                type='password'
                id='password'
                placeholder='password...'
                minLength={5}
                required
                aria-label='Password'
            />
            <div className={styles.buttons}>
                <AuthorizationButton
                    warning={false}
                    type='log-in'
                    form={true}
                    disabled={processing}
                />
                <AuthorizationButton
                    warning={false}
                    type='forgot'
                    form={true}
                    disabled={processing}
                    onClick={() => navigate('/forgot', { state: { role: user } })}
                />
            </div>
        </form>
        );
    </>)
};