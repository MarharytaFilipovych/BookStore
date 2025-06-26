import {ContactProp, Links} from './types';

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


export const bookSortOptions: Map<string, string> = new Map([
    ['year (from oldest)', 'year.asc'],
    ['year (from newest)', 'year.desc'],
    ['name (A-Z)', 'name.asc'],
    ['name (Z-A)', 'name.desc'],
]);


