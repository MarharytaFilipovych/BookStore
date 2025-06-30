import React from 'react';
import styles from './style.module.css';
import { SearchBar } from './SearchBar';
import { FieldFilter } from './FieldFilter';
import { Year } from './Year';
import { Filter } from './Filter';
import {BookFilterState} from "../../types";
import {NumericFilter} from "./NumericFilter";

type SearchFieldProps = {
    genres: string[];
    languages: string[];
    ageGroups: string[];
    sortOptions: string[];
    filter: BookFilterState;
    onFilterChange: (key: keyof (BookFilterState), value: string) => void;
}

export const BookSearchField: React.FC<SearchFieldProps> =
    ({genres, languages, ageGroups, sortOptions, filter, onFilterChange}) => {
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
                    <FieldFilter
                        typeOfField='age group'
                        fields={ageGroups}
                        value={filter.ageGroup}
                        onInput={(ageGroup: string) => onFilterChange('ageGroup', ageGroup)}
                    />
                    <SearchBar
                        value={filter.author}
                        onNameChange={(value) => onFilterChange('author', value)}
                        text='type author...'
                        small={true}
                    />

                    <NumericFilter
                        min={0}
                        value={filter.minPrice}
                        onInput={(value) => onFilterChange('minPrice', value)}
                        placeholder="Min Price"
                        step={0.01}
                    />
                    <NumericFilter
                        min={0}
                        value={filter.maxPrice}
                        onInput={(value) => onFilterChange('maxPrice', value)}
                        placeholder="Max Price"
                        step={0.01}
                    />
                    <NumericFilter
                        min={1}
                        value={filter.minPages}
                        onInput={(value) => onFilterChange('minPages', value)}
                        placeholder="Min Pages"
                        step={1}
                    />
                    <NumericFilter
                        min={1}
                        value={filter.maxPages}
                        onInput={(value) => onFilterChange('maxPages', value)}
                        placeholder="Max Pages"
                        step={1}
                    />

                    <Filter
                        options={sortOptions}
                        value={filter.sort}
                        onSelectOption={(sort: string) => onFilterChange('sort', sort)}
                    />
                    <SearchBar
                        value={filter.name}
                        onNameChange={(value) => onFilterChange('name', value)}
                        text='type title...'
                    />
                </div>


            </div>
        );
    };