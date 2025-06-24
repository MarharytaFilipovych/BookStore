import React, {useState} from 'react';
import styles from './style.module.css';
import {AuthorizationButton} from "../../components/AuthorizationButton/AuthorizationButton";
import {Link} from "react-router";
import {State} from "../../types";
import {Icon} from "../../components/Icon/Icon";


const WelcomePage: React.FC = ()=>{
    return <>
        <div className='page'>
            <h1>Welcome to the bookstore!</h1>
            <h2>Who are you???</h2>
            <section className='employee'>
                <h2>Employee!</h2>
                <Link to={'/login/employee'}><AuthorizationButton type='log-in'/></Link>
            </section>
            <section className='customer'>
                <h2>Customer!</h2>
                <div className='buttons'></div>
                <Link to={'/login/client'}><AuthorizationButton type='log-in'/></Link>
                <Link to={'/sign'}><AuthorizationButton type='sign'/></Link>
            </section>
        </div>
    </>
}