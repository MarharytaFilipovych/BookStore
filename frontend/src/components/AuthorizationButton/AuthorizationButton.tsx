import React from 'react';
import styles from './style.module.css';
import classNames from 'classnames';

type AuthorizationButtonProps = {
    type: 'log-in' | 'sign' | 'log-out' | 'delete everything' | 'cancel' | 'forgot' | 'reset' | 'send-again',
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
        case 'reset':
            buttonText = "Reset Password"
            break;
        case 'send-again':
            buttonText = "Send Again"
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
                       [styles.reset]: type === 'reset',
                       [styles.sendAgain]: type === 'send-again',
                       [styles.warning]: warning
                   })}
                   type={form ? 'submit' : 'button'}
                   onClick={onClick}
    >
        {buttonText}
    </button>;
};