import React from 'react';
import styles from './style.module.css';

type FieldProps = {
    typeOfField: 'genre' | 'country' | 'language',
    fields: string[],
    value: string,
    onInput: (value: string) => void;
}

export const FieldFilter: React.FC<FieldProps> = ({typeOfField, fields, value, onInput}) => {
    const datalistId = `${typeOfField}-options`;
    const placeholderText = `Type ${typeOfField}...`;

    const handleFieldChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onInput(e.target.value);
    };

    return <div className={styles.filterComponent}>
        <input
            value={value}
            onChange={handleFieldChange}
            placeholder={placeholderText}
            type='text'
            list={datalistId}
            className={styles.filterInput}
        />
        <datalist id={datalistId}>
            {fields.map(f => (
                <option key={f} value={f} />
            ))}
        </datalist>
    </div>;
};