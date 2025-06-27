import React from 'react';
import styles from './style.module.css';
import {ClientType} from "../../types";
import {MiniButton} from "../MiniButton/MiniButton";
import {ClientService} from "../../services/ClientService";

export const ClientComponent: React.FC<ClientType> = (client) => {
    return (
        <>
            <div className={styles.container}>
                    <h3 className={styles.name}>{client.name}</h3>
                    <p className={styles.email}>{client.email}</p>
                    <p className={styles.addInfo}>{client.balance}</p>
                    <MiniButton
                        topic='ban'
                        size='medium'
                        onClick={async () => {await ClientService.blockClient(client.email);}}
                    />
            </div>
        </>
    );
};