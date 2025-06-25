import React, {useState, useEffect, useContext} from 'react';
import {useNavigate} from 'react-router';
import {LogInForm} from "../../components/AuthForm/LogInForm";
import {useParams} from "react-router-dom";
import {Role} from "../../types";
import {AppContext} from "../../context";
import styles from './style.module.css';

export const LoginPage: React.FC = () => {
    const userType: Role = useParams().user === 'employee' ? 'EMPLOYEE' : 'CLIENT';
    const navigate = useNavigate();
    const context = useContext(AppContext);
    const [loginError, setLoginError] = useState<boolean>(false);
    const [processing, setProcessing] = useState<boolean>(false);

    useEffect(() => {
        if (context.user)navigate('/books');
    }, [context.user, navigate]);

    const login = async (email: string, password: string, role: Role) => {
        setLoginError(false);
        setProcessing(true);
        try {
            await context.login({email, password, role});
        } catch (e) {
            setLoginError(true);
        } finally {
            setProcessing(false);
        }
    };


    console.log('Final userType:', userType);
    return (
        <div className={styles.formContainer}>
            <div className={styles.roleInfo}>
                <p>Logging in as ... <strong>{ userType === 'CLIENT' ? 'client' : 'employee'}</strong> </p>
            </div>
                <LogInForm onSubmit={login} passError={loginError} processing={processing} user={userType}/>
        </div>
    );
};