import React from 'react';
import styles from './style.module.css';
import {Icon} from '../Icon/Icon';
import classNames from 'classnames';
import {IconTopic} from '../../types';

type MiniButtonProps = {
    topic: IconTopic;
    size: 'mini' | 'medium' | 'premedium';
    onClick?: () => void;
    isDisabled?: boolean;
    mirror?: boolean;
}

export const MiniButton: React.FC<MiniButtonProps> = ({topic, size, onClick, isDisabled, mirror}) => {
    return (
        <button
            className={classNames(styles.miniButton, {
                [styles.right]: mirror
            })}
            onClick={onClick}
            disabled={isDisabled}
        >
            <Icon topic={topic} size={size} mirror={mirror} />
        </button>
    );
};