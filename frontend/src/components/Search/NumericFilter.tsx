import React from "react";
import styles from './style.module.css';
type FilterProps = {
    min: number;
    value: string;
    onInput: (value: string) => void;
    placeholder: string;
    step:number;
}
export const NumericFilter: React.FC<FilterProps> = ({min, value, onInput, placeholder, step})=>{
    const handleFieldChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onInput(e.target.value);
    };

    return  <div className={styles.filterComponent}>
        <input
            type="number"
            placeholder={placeholder}
            value={value}
            onChange={handleFieldChange}
            min={min}
            step={step}
        />
    </div>
}