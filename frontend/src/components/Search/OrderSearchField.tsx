import React from 'react';
import styles from './style.module.css';
import { SearchBar } from './SearchBar';
import { Filter } from './Filter';
import { OrderFilterState } from "../../types";

type OrderSearchFieldProps = {
    sortOptions: string[];
    filter: OrderFilterState;
    onFilterChange: (key: keyof OrderFilterState, value: string) => void;
}

export const OrderSearchField: React.FC<OrderSearchFieldProps> = ({
                                                                      sortOptions,
                                                                      filter,
                                                                      onFilterChange
                                                                  }) => {
    return (
        <div className={styles.search}>
            <div className={styles.filterContainer}>
                <SearchBar
                    value={filter.clientEmail}
                    onNameChange={(value) => onFilterChange('clientEmail', value)}
                />

                <SearchBar
                    value={filter.employeeEmail}
                    onNameChange={(value) => onFilterChange('employeeEmail', value)}
                />

                <Filter
                    options={sortOptions}
                    value={filter.sort}
                    onSelectOption={(sort: string) => onFilterChange('sort', sort)}
                />
            </div>
        </div>
    );
};