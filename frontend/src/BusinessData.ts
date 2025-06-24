import {ContactProp, Links} from './types';

export const links: Links = {
    employeeLinks:[
        {request_type: 'clients', name: 'our clients'},
        {request_type: 'orders', name:  'orders'},
        {request_type: 'books', name:  'books'},
        {request_type: 'colleagues', name: 'my colleagues'},
        {request_type: 'profile', name: 'my profile'}
    ],
    clientLinks:[
        {request_type: 'books', name: 'books'},
        {request_type: 'orders', name:  'my orders'},
        {request_type: 'profile', name: 'my profile'}
    ]
};

export const myContacts: ContactProp[] = [
    { typeOfContact: 'email', contact: 'margarit.fil@gmail.com' },
    { typeOfContact: 'email', contact: 'mfilipovych@kse.org.ua' },
    { typeOfContact: 'call', contact: '+38 097 151 9327' },
];


export const bookSortOptions: Map<string, string> = new Map([
    ['year (from oldest)', 'year.asc'],
    ['year (from newest)', 'year.desc'],
    ['name (A-Z)', 'name.asc'],
    ['name (Z-A)', 'name.desc'],
    ['popularity (low to high)', 'popularity.asc'],
    ['popularity (high to low)', 'popularity.desc'],
    ['rating (low to high)', 'vote_average.asc'],
    ['rating (high to low)', 'vote_average.desc'],
    ['vote count (low to high)', 'vote_count.asc'],
    ['vote count (high to low)', 'vote_count.desc']
]);


