import React, {useState, useEffect, useContext} from 'react';
import {useNavigate} from 'react-router';
import {LogInForm} from "../../components/Form/LogInForm";
import {useParams} from "react-router-dom";
import {Role} from "../../types";
import {AppContext} from "../../context";
import styles from './style.module.css';
import {AuthService} from "../../services/AuthService";

export const LoginPage: React.FC = () => {
    const userType: Role = useParams().user === 'employee' ? 'EMPLOYEE' : 'CLIENT';
    const navigate = useNavigate();
    const context = useContext(AppContext);
    const [loginError, setLoginError] = useState<boolean>(false);
    const [processing, setProcessing] = useState<boolean>(false);

    if (context.user) {
        console.log('✅ LoginPage: User found, navigating to /books');
        navigate('/books');
    }

    const login = async (email: string, password: string, role: Role) => {
        console.log('🔐 LoginPage: Login form submitted', {
            email,
            role,
            hasPassword: !!password
        });

        setLoginError(false);
        setProcessing(true);

        try {
            console.log('📡 LoginPage: Calling context.login...');
            await context.login({email, password, role});
            console.log('✅ LoginPage: context.login completed successfully');

            // Check if user was set
            console.log('🔍 LoginPage: Checking context state after login:', {
                hasUser: !!context.user,
                userEmail: context.user?.email,
                userRole: context.role
            });

        } catch (e) {
            console.error('❌ LoginPage: Login failed:', e);
            setLoginError(true);
        } finally {
            setProcessing(false);
        }
    };

    console.log('🔄 LoginPage: Component render', {
        userType,
        hasContextUser: !!context.user,
        contextUserEmail: context.user?.email,
        contextRole: context.role,
        loginError,
        processing
    });

    return (
        <div className={styles.formContainer}>
            <div className={styles.roleInfo}>
                <p>Logging in as ... <strong>{ userType === 'CLIENT' ? 'client' : 'employee'}</strong> </p>
            </div>
            <LogInForm onSubmit={login} passError={loginError} processing={processing} user={userType}/>
        </div>
    );
};
