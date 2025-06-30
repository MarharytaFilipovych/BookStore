import React from 'react';
import styles from './style.module.css';
import { Icon } from '../Icon/Icon';
import classNames from 'classnames';

export const SearchBar: React.FC<{
    value: string;
    onNameChange: (name: string) => void;
    text?: string;
    small?: boolean;
    noMargin?: boolean;
}> = ({ value, onNameChange, text, small, noMargin }) => {
    return (
        <div className={classNames(styles.searchBar, { [styles.small]: small, [styles.noMargin] : noMargin})}>
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
