import React, {useContext, useState, useEffect} from 'react';
//import {AppContext} from '../../context';
import {useNavigate} from 'react-router';
import {LogInForm} from "../../components/AuthForm/LogInForm";
import {useParams} from "react-router-dom";
import {User} from "../../types";


export const LoginPage: React.FC = () => {
    const { user } = useParams<{ user: string }>();
    const userType: User = user?.toUpperCase() === 'EMPLOYEE' ? 'EMPLOYEE' : 'CLIENT';
    const navigate = useNavigate();
    //const context = useContext(AppContext);
    const [loginError, setLoginError] = useState<boolean>(false);
    const [processing, setProcessing] = useState<boolean>(false);

    // useEffect(() => {
    //     if (context.user?._id)navigate('/');
    // }, [context.user, navigate]);

    const login = async (email: string, password: string, user: User) => {
        setLoginError(false);
        setProcessing(true);
        // try {
        //     const user = await context.userAPI.loginUser(email, password);
        //     context.setUser(user);
        // } catch (e) {
        //     setLoginError(true);
        // } finally {
        //     setProcessing(false);
        // }
    };
    return <>
        <LogInForm onSubmit={login} passError={loginError} processing={processing} user={userType}/>
    </>;
};