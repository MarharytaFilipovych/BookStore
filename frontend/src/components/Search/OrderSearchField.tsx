import React from 'react';
import styles from './style.module.css';
import { SearchBar } from './SearchBar';
import { Filter } from './Filter';
import {ForWhomOrder, OrderFilterState, Role} from "../../types";

type OrderSearchFieldProps = {
    sortOptions: string[];
    filter: OrderFilterState;
    onFilterChange: (key: keyof OrderFilterState, value: string) => void;
    forWhom: ForWhomOrder;
}

export const OrderSearchField: React.FC<OrderSearchFieldProps> = ({
                                                                      sortOptions,
                                                                      filter,
                                                                      onFilterChange, forWhom
                                                                  }) => {
    return (
        <div className={styles.search}>
            <div className={styles.search}>
                {(forWhom === 'all' || forWhom == 'employee') && (
                    <SearchBar
                        value={filter.clientEmail}
                        onNameChange={(value) => onFilterChange('clientEmail', value)}
                        text='type client email...'
                        noMargin={true}
                    />
                )}
                {(forWhom === 'all' || forWhom == 'client') && (
                    <SearchBar
                        value={filter.employeeEmail}
                        onNameChange={(value) => onFilterChange('employeeEmail', value)}
                        text='type employee email...'
                        noMargin={true}
                    />
                )}
                <Filter
                    options={sortOptions}
                    value={filter.sort}
                    onSelectOption={(sort: string) => onFilterChange('sort', sort)}
                />
            </div>
        </div>
    );
};