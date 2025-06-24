import React from 'react';
import styles from './style.module.css';
import classNames from 'classnames';

type YearProps = {
    value: string,
    onInput: (value: string) => void;
}
export const Year: React.FC<YearProps> = ({value, onInput})=>{
    const handleYearChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onInput(e.target.value);
    };
    return <input className={classNames(styles.filterComponent, styles.year)} value={value} onInput={handleYearChange} placeholder='year...' type="number" min='1941' max={new Date().getFullYear() + 3}/>;
};
