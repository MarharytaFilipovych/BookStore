import React, {FormEvent, useEffect, useRef, useState} from 'react';
import styles from './style.module.css';
import { ActionButton } from '../AuthorizationButton/ActionButton';
import {useNavigate} from "react-router";
import {Icon} from "../Icon/Icon";

type RegisterFormProps = {
    onSubmit: (username: string, email: string, balance: number, password: string)=>void,
    processing: boolean,
    error: string
}

export const RegisterForm: React.FC<RegisterFormProps> = ({onSubmit, processing, error}) => {
    const formRef = useRef<HTMLFormElement>(null);
    const [passwordError, setPasswordError] = useState(error || '');
    const navigate = useNavigate();
    const submit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if(!formRef.current) return;

        const formData: FormData = new FormData(formRef.current);
        const username = formData!.get('username')!.toString();
        const email = formData.get('email')!.toString();
        const password = formData.get('password')!.toString();
        const confirmPassword = formData.get('confirmPassword')!.toString();
        const balance = Number(formData.get('balance')?.toString() || '0');

        if (password !== confirmPassword) {
            setPasswordError('Passwords do not match!');
            return;
        }
        if (!username.trim() || !email.trim() || !password) {
            setPasswordError('Please fill in all required field!');
            return;
        }
        setPasswordError('');
        onSubmit(username, email, balance, password);
    };
    useEffect(() => {
        if (error) setPasswordError(error);
    }, [error]);

    return (<>
        {processing && (<Icon topic='loading' size='big' />)}
        <form className={styles.form} onSubmit={submit} ref={formRef}>
            <div className={styles.instructions}><h2>Register!</h2></div>
            {passwordError && (<p className={styles.errorMessage}>{passwordError}</p>)}
            <input type='text' id='username' name='username' placeholder='username...' required/>
            <input type='email' id='email' name='email' placeholder='email...' required/>
            <input type='number' id='balance' name='balance' placeholder='balance...' min={0}/>
            <input type='password' id='password' name='password' placeholder='password...' minLength={8} required/>
            <input type='password' id='confirmPassword' name='confirmPassword' placeholder='confirm password...' minLength={8} required/>
            <div className={styles.buttons}>
                <ActionButton warning={false} type='sign' form={true} disabled={processing}/>
                <ActionButton warning={false} type='cancel' form={false} disabled={processing} onClick={() => navigate(`/`)}/>
            </div>
        </form>);</>)
};