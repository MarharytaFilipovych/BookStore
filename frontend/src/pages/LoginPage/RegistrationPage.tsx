import React, {useContext, useState} from 'react';
import {useNavigate} from 'react-router';
//import {AppContext} from '../../context';
import {RegisterForm} from "../../components/AuthForm/RegisterForm";


export const RegistrationPage: React.FC = ()=>{
    const navigate = useNavigate();
    //const context = useContext(AppContext);
    const [registerError, setRegisterError] = useState<string>('');
    const [processing, setProcessing] = useState<boolean>(false);
    const register = async (username: string, email: string, balance: number, password:string)=>{
        setRegisterError('');
        setProcessing(true);
        try{
            //await context.userAPI.registerUser(username, email, password);
            alert('Registration was successful!!!');
            navigate('/login?user=client');
        }catch(e){
            setRegisterError(e instanceof Error ? e.message : String(e));
        }finally {
            setProcessing(false);
        }
    };
    return <>
        <RegisterForm onSubmit={register} error={registerError} processing={processing}/>
    </>;
};