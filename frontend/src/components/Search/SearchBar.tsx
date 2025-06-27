import React from 'react';
import styles from './style.module.css';
import {Icon} from '../Icon/Icon';

export const SearchBar: React.FC<{ value: string, onNameChange: (name: string) => void }> = ({value, onNameChange}) => {
    return <div className={styles.searchBar}>
        <Icon topic='search' size='mini'/>
        <input
            type='text'
            placeholder='type name ...'
            value={value}
            onChange={(e) => onNameChange(e.target.value)}
        />
    </div>;
};