import React, {useState, useEffect, useContext} from 'react';
import {useNavigate} from 'react-router';
import {LogInForm} from "../../components/AuthForm/LogInForm";
import {useParams} from "react-router-dom";
import {Role} from "../../types";
import {AppContext} from "../../context";


export const LoginPage: React.FC = () => {
    const { user } = useParams<{ user: string }>();
    const userType: Role = user?.toUpperCase() === 'EMPLOYEE' ? 'EMPLOYEE' : 'CLIENT';
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
    return <>
        <LogInForm onSubmit={login} passError={loginError} processing={processing} user={userType}/>
    </>;
};