import React from 'react';
import styles from './style.module.css';
import { SearchBar } from './SearchBar';
import { FieldFilter } from './FieldFilter';
import { Year } from './Year';
import { Filter } from './Filter';
import {BookFilterState} from "../../types";

type SearchFieldProps = {
    genres: string[];
    languages: string[];
    ageGroups: string[];
    sortOptions: string[];
    filter: BookFilterState;
    onFilterChange: (key: keyof (BookFilterState), value: string) => void;
}

export const BookSearchField: React.FC<SearchFieldProps> = ({
                                                            genres,
                                                            languages,
                                                            ageGroups,
                                                            sortOptions,
                                                            filter,
                                                            onFilterChange
                                                        }) => {
    return (
        <div className={styles.search}>
            <div className={styles.filterContainer}>
                <Year
                    value={filter.publicationYear}
                    onInput={(yearValue: string) => onFilterChange('publicationYear', yearValue)}
                />
                <FieldFilter
                    typeOfField='genre'
                    fields={genres}
                    value={filter.genre}
                    onInput={(genre: string) => onFilterChange('genre', genre)}
                />
                <FieldFilter
                    typeOfField='language'
                    fields={languages}
                    value={filter.language}
                    onInput={(language: string) => onFilterChange('language', language)}
                />

                {/* Age Group Filter */}
                <div className={styles.filterComponent}>
                    <select
                        value={filter.ageGroup}
                        onChange={(e) => onFilterChange('ageGroup', e.target.value)}
                        className={styles.filterSelect}
                    >
                        <option value="">All Age Groups</option>
                        {ageGroups.map(ageGroup => (
                            <option key={ageGroup} value={ageGroup}>
                                {ageGroup}
                            </option>
                        ))}
                    </select>
                </div>

                <SearchBar
                    value={filter.author}
                    onNameChange={(value) => onFilterChange('author', value)}
                />

                <div className={styles.filterComponent}>
                    <input
                        type="number"
                        placeholder="Min Price"
                        value={filter.minPrice}
                        onChange={(e) => onFilterChange('minPrice', e.target.value)}
                        min="0"
                        step="0.01"
                    />
                </div>
                <div className={styles.filterComponent}>
                    <input
                        type="number"
                        placeholder="Max Price"
                        value={filter.maxPrice}
                        onChange={(e) => onFilterChange('maxPrice', e.target.value)}
                        min="0"
                        step="0.01"
                    />
                </div>

                <div className={styles.filterComponent}>
                    <input
                        type="number"
                        placeholder="Min Pages"
                        value={filter.minPages}
                        onChange={(e) => onFilterChange('minPages', e.target.value)}
                        min="1"
                    />
                </div>
                <div className={styles.filterComponent}>
                    <input
                        type="number"
                        placeholder="Max Pages"
                        value={filter.maxPages}
                        onChange={(e) => onFilterChange('maxPages', e.target.value)}
                        min="1"
                    />
                </div>

                <Filter
                    options={sortOptions}
                    value={filter.sort}
                    onSelectOption={(sort: string) => onFilterChange('sort', sort)}
                />
            </div>

            <SearchBar
                value={filter.name}
                onNameChange={(value) => onFilterChange('name', value)}
            />
        </div>
    );
};