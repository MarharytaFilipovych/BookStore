import {BookSortField, ContactProp, Links, SortOrder} from './types';

export const links: Links = {
    employeeLinks:[
        {link: 'clients', name: 'our clients'},
        {link: 'orders', name:  'orders'},
        {link: 'books', name:  'books'},
        {link: 'colleagues', name: 'my colleagues'},
        {link: 'profile', name: 'my profile'}
    ],
    clientLinks:[
        {link: 'books', name: 'books'},
        {link: 'orders', name:  'my orders'},
        {link: 'profile', name: 'my profile'}
    ]
};

export const myContacts: ContactProp[] = [
    { typeOfContact: 'email', contact: 'margarit.fil@gmail.com' },
    { typeOfContact: 'email', contact: 'mfilipovych@kse.org.ua' },
    { typeOfContact: 'call', contact: '+38 097 151 9327' },
];

export const sortOptions = new Map<string, {sortBy: BookSortField, sortOrder: SortOrder}>([
    ['Title A-Z', {sortBy: 'name', sortOrder: 'asc'}],
    ['Title Z-A', {sortBy: 'name', sortOrder: 'desc'}],
    ['Author A-Z', {sortBy: 'author', sortOrder: 'asc'}],
    ['Author Z-A', {sortBy: 'author', sortOrder: 'desc'}],
    ['Year (Oldest First)', {sortBy: 'publication_date', sortOrder: 'asc'}],
    ['Year (Newest First)', {sortBy: 'publication_date', sortOrder: 'desc'}],
    ['Price (Low to High)', {sortBy: 'price', sortOrder: 'asc'}],
    ['Price (High to Low)', {sortBy: 'price', sortOrder: 'desc'}],
    ['Pages (Fewest First)', {sortBy: 'pages', sortOrder: 'asc'}],
    ['Pages (Most First)', {sortBy: 'pages', sortOrder: 'desc'}]
]);

export const ageGroups: string[] = ['Children', 'Teen', 'Adult', 'Other'];
export const languages: string[] = ['English', 'Spanish', 'French', 'German', 'Ukrainian', 'Japanese', 'Other'];
export const genres: string[] =  [
    'Fiction', 'Non-Fiction', 'Mystery', 'Romance', 'Fantasy', 'Science Fiction',
    'Thriller', 'Biography', 'History', 'Self-Help', 'Technical', 'Drama',
    'Comedy', 'Horror', 'Adventure', 'Poetry', 'Philosophy', 'Religion',
    'Art', 'Travel', 'Cooking', 'Health', 'Business', 'Education', 'Classic Literature'
];

export const PAGE_SIZE: number = 20;