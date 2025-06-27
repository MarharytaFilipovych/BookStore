import React from 'react';
import styles from './style.module.css';
import { Icon } from '../Icon/Icon';
import classNames from 'classnames'; // if using a classnames library

export const SearchBar: React.FC<{
    value: string;
    onNameChange: (name: string) => void;
    text?: string;
    small?: boolean;
}> = ({ value, onNameChange, text, small }) => {
    return (
        <div className={classNames(styles.searchBar, { [styles.small]: small })}>
            <Icon topic="search" size="mini" />
            <input
                type="text"
                placeholder={text ? text : 'type name ...'}
                value={value}
                onChange={(e) => onNameChange(e.target.value)}
            />
        </div>
    );
};
