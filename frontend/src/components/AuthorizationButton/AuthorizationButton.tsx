import React from 'react';
import styles from './style.module.css';
import classNames from 'classnames';

type AuthorizationButtonProps = {
    type: 'log-in' | 'sign' | 'log-out' | 'delete everything' | 'cancel' | 'forgot',
    warning?: boolean
    form?: boolean,
    onClick?: ()=>void;
    disabled?: boolean;
}

export const AuthorizationButton: React.FC<AuthorizationButtonProps> = ({warning, type, form, onClick, disabled}) => {

    let buttonText;
    switch (type) {
        case 'log-in':
            buttonText = 'Log In';
            break;
        case 'sign':
            buttonText = 'Sign Up';
            break;
        case 'log-out':
            buttonText = 'Log Out';
            break;
        case 'delete everything':
            buttonText = 'Delete';
            break;
        case 'cancel':
            buttonText = 'Cancel';
            break;
        case 'forgot':
            buttonText = "Forgot Password"
            break;
    }

    return <button disabled={disabled}
                   className={classNames(styles.button, {
                       [styles.logIn]: type === 'log-in',
                       [styles.signIn]: type === 'sign',
                       [styles.logOut]: type === 'log-out',
                       [styles.delete]: type === 'delete everything',
                       [styles.cancel]: type === 'cancel',
                       [styles.forgot]: type === 'forgot',
                       [styles.warning]: warning
                   })}
                   type={form ? 'submit' : 'button'}
                   onClick={onClick}
    >
        {buttonText}
    </button>;
};