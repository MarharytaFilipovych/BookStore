import React from 'react';
import styles from './style.module.css';
import { AuthorizationButton } from '../AuthorizationButton/AuthorizationButton';

type WarningProps = {
    onClick: () => void,
    onCancel: () => void,
    purpose: 'log-out' | 'delete everything',
    message: string
}

export const Warning: React.FC<WarningProps> = ({ onClick, purpose, onCancel, message }) => {
    return  <div className={styles.warning}>
            <p>{message}</p>
            <div className='buttons'>
                <AuthorizationButton
                    type={purpose}
                    warning={true}
                    onClick={onClick}
                />
                <AuthorizationButton
                    type='cancel'
                    warning={true}
                    onClick={onCancel}
                />
            </div>
        </div>;
};