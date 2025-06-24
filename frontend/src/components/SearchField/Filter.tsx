import React from 'react';
import  styles from './style.module.css';
import classNames from 'classnames';

type FilterProps = {
    options: string[],
    value: string,
    onSelectOption: (selectedOption: string) => void,
}

export const Filter: React.FC<FilterProps> = ({ options, value, onSelectOption }) => {
    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onSelectOption(e.target.value);
    };
    return <select
        name="filters"
        id="filters"
        className={classNames(styles.filterComponent, styles.filterSelect)}
        value={value}
        onChange={handleSelectChange}

    >
        <option value="">Select your filter</option>
        {options.map(op => (
            <option key={op} value={op}>{op}</option>
    ))}
    </select>;


};
