import React from 'react';
import styles from './style.module.css';
import { SearchBar } from './SearchBar';
import { Filter } from './Filter';
import { PersonFilterState} from "../../types";

type SearchFieldProps = {
    sortOptions: string[];
    filter: PersonFilterState;
    onFilterChange: (key: keyof PersonFilterState, value: string) => void;
}

export const PersonSearchField: React.FC<SearchFieldProps> = ({
                                                                  sortOptions,
                                                                  filter,
                                                                  onFilterChange
                                                              }) => {
    return (
        <div className={styles.search}>
                <SearchBar
                    value={filter.email}
                    onNameChange={(value) => onFilterChange('email', value)}
                    noMargin={true}
                    text='type email...'
                />

                <Filter
                    options={sortOptions}
                    value={filter.sort}
                    onSelectOption={(sort: string) => onFilterChange('sort', sort)}
                />
        </div>
    );
};
