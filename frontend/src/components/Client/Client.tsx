import React from 'react';
import styles from './style.module.css';
import {Client} from "../../types";
import {MiniButton} from "../MiniButton/MiniButton";
import {ClientService} from "../../services/ClientService";

export const ClientComponent: React.FC<Client> = (client) => {
    return (
        <>
            <div className={styles.clientContainer}>
                    <h3 className={styles.clientName}>{client.name}</h3>
                    <p className={styles.clientEmail}>{client.email}</p>
                    <p className={styles.balance}>{client.balance}</p>
                    <MiniButton
                        topic='ban'
                        size='medium'
                        onClick={async () => {await ClientService.blockClient(client.email);}}
                    />
            </div>
        </>
    );
};