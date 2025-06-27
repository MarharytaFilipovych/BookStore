import React, { useContext } from 'react';
import { AppContext } from '../../context';
import { MiniButton } from '../MiniButton/MiniButton';
import styles from './style.module.css';

export const BasketButton: React.FC<{onClick: () => void}> = ({ onClick }) => {
    const context = useContext(AppContext);
    const totalItems = context.basket.reduce((total, item) => total + item.quantity, 0);
    return (
        <div className={styles.basketButtonContainer}>
            <MiniButton topic='basket' size='premedium' onClick={onClick} />
            {totalItems > 0 && (
                <span className={styles.basketBadge}>{totalItems}</span>
            )}
        </div>
    );
};