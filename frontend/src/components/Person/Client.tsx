import React from 'react';
import styles from './style.module.css';
import { ClientType } from "../../types";
import { MiniButton } from "../MiniButton/MiniButton";

type ClientComponentProps = ClientType & {
    isBlocked?: boolean;
    onBlock: (email: string) => Promise<void>;
    onUnblock: (email: string) => Promise<void>;
}

export const ClientComponent: React.FC<ClientComponentProps> = ({name, email, balance, isBlocked = false, onBlock, onUnblock}) => {
    return (
        <div className={`${styles.container} ${isBlocked ? styles.blocked : ''}`}>
            <h3 className={styles.name}>{name}</h3>
            <a href={`mailto:${email}`} className={styles.email}>{email}</a>
            <p className={styles.addInfo}>${balance}</p>
            <MiniButton
                topic={isBlocked ? 'ban' : 'unban'}
                size='medium'
                onClick={async ()=>{
                    if (isBlocked)await onUnblock(email);
                    else await onBlock(email);
                }}
            />
        </div>
    );
};