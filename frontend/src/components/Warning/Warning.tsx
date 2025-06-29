import React from 'react';
import styles from './style.module.css';
import { ActionButton } from '../AuthorizationButton/ActionButton';

type WarningProps = {
    onClick: () => void,
    onCancel: () => void,
    purpose: 'log-out' | 'delete',
    message: string
}

export const Warning: React.FC<WarningProps> = ({ onClick, purpose, onCancel, message }) => {
    return  <div className={styles.warning}>
            <p>{message}</p>
            <div className={styles.buttons}>
                <ActionButton type={purpose} warning={true} onClick={onClick}/>
                <ActionButton type='cancel' warning={true} onClick={onCancel}/>
            </div>
        </div>;
};