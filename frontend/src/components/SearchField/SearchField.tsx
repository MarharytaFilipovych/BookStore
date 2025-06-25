import React from 'react';
// import styles from './style.module.css';
// import { SearchBar } from './SearchBar';
// import { FieldFilter } from './FieldFilter';
// import { Year } from './Year';
// import { Filter } from './Filter';
//
//
// type SearchFieldProps = {
//     genres: string[],
//     languages: string[],
//     countries: string[],
//     sortOptions: string[],
//     //filter: FilterState,
//     //onFilterChange: (key: keyof FilterState, value: string) => void,
// }
//
// export const SearchField: React.FC<SearchFieldProps> = ({genres, languages, countries, sortOptions, filter, onFilterChange}) => {
//     return (
//         <div className={styles.search}>
//             <div className={styles.filterContainer}>
//                 <Year
//                     value={filter.year}
//                     onInput={(yearValue: string) => onFilterChange('year', yearValue)}
//                 />
//                 <FieldFilter
//                     typeOfField='genre'
//                     fields={genres}
//                     value={filter.genre}
//                     onInput={(genre: string) => onFilterChange('genre', genre)}
//                 />
//                 <FieldFilter
//                     typeOfField='language'
//                     fields={languages}
//                     value={filter.language}
//                     onInput={(language: string) => onFilterChange('language', language)}
//                 />
//                 <FieldFilter
//                     typeOfField='country'
//                     fields={countries}
//                     value={filter.country}
//                     onInput={(country: string) => onFilterChange('country', country)}
//                 />
//                 <Filter
//                     options={sortOptions}
//                     value={filter.sortOption}
//                     onSelectOption={(sortOption: string) => onFilterChange('sortOption', sortOption)}
//                 />
//             </div>
//             <SearchBar value={filter.name} onNameChange={(value) => onFilterChange('name', value)} />
//         </div>
//     );
// };