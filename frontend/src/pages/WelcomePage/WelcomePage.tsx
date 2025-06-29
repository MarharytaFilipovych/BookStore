import React, {useContext, useEffect} from 'react';
import styles from './style.module.css';
import {ActionButton} from "../../components/AuthorizationButton/ActionButton";
import {Link, useNavigate} from "react-router";
import {AppContext} from "../../context";

export const WelcomePage: React.FC = ()=>{
    const navigate = useNavigate();
    const context = useContext(AppContext);
    useEffect(() => {
        if (context.user) {
            console.log('✅ WelcomePage: User found, navigating to /books');
            navigate('/books');
        }
    }, [context.user, navigate]);
    return <>
        <div className={styles.page}>
            <div className={styles.heading}>
                <h1>Welcome to the bookstore!</h1>
                <h2>Who are you???</h2>
            </div>
            <div className={styles.sections}>
                <section className={styles.employee}>
                    <h2>Employee!</h2>
                    <Link to={'/login/employee'}><ActionButton type='log-in'/></Link>
                </section>
                <section className={styles.customer}>
                    <h2>Customer!</h2>
                    <div className={styles.buttons}>
                        <Link to={'/login/client'}><ActionButton type='log-in'/></Link>
                        <Link to={'/sign'}><ActionButton type='sign'/></Link>
                    </div>
                </section>
            </div>
        </div>
    </>
}