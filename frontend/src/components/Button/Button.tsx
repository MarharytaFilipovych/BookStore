import React from 'react';
import styles from './style.module.css';

export const Button: React.FC<{purpose: string, onClick: ()=>void, disabled: boolean}> =({purpose, onClick, disabled})=>{
    return <button className={styles.button} onClick={onClick} disabled={disabled}>{purpose}</button>;
};