import React, {useState} from 'react';
import styles from './style.module.css';
import {AuthorizationButton} from "../../components/AuthorizationButton/AuthorizationButton";
import {Link} from "react-router";
import {State} from "../../types";
import {Icon} from "../../components/Icon/Icon";


export const WelcomePage: React.FC = ()=>{
    return <>
        <div className={styles.page}>
            <div className={styles.heading}>
                <h1>Welcome to the bookstore!</h1>
                <h2>Who are you???</h2>
            </div>
            <div className={styles.sections}>
                <section className={styles.employee}>
                    <h2>Employee!</h2>
                    <Link to={'/login/employee'}><AuthorizationButton type='log-in'/></Link>
                </section>
                <section className={styles.customer}>
                    <h2>Customer!</h2>
                    <div className={styles.buttons}>
                        <Link to={'/login/client'}><AuthorizationButton type='log-in'/></Link>
                        <Link to={'/sign'}><AuthorizationButton type='sign'/></Link>
                    </div>
                </section>
            </div>

        </div>
    </>
}