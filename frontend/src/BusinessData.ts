import {BookSortField, ClientSortField, ContactProp, EmployeeSortField, Links, OrderSortField, SortOrder} from './types';

export const links: Links = {
    employeeLinks: [
        {link: 'people/clients', name: 'our clients'},
        {link: 'people/blocked', name: 'blocked clients'},
        {link: 'orders', name: 'orders'},
        {link: 'my-orders', name: 'my orders'},
        {link: 'books', name: 'books'},
        {link: 'people/colleagues', name: 'my colleagues'},
        {link: 'profile', name: 'my profile'}
    ],
    clientLinks: [
        {link: 'books', name: 'books'},
        {link: 'my-orders', name: 'my orders'},
        {link: 'profile', name: 'my profile'}
    ]
};

export const myContacts: ContactProp[] = [
    { typeOfContact: 'email', contact: 'margarit.fil@gmail.com' },
    { typeOfContact: 'email', contact: 'mfilipovych@kse.org.ua' },
    { typeOfContact: 'call', contact: '+38 097 151 9327' },
];

export const bookSortOptions = new Map<string, {sortBy: BookSortField, sortOrder: SortOrder}>([
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
export const orderSortOptions = new Map<string, {sortBy: OrderSortField, sortOrder: SortOrder}>([
    ['Date (Newest First)', {sortBy: 'order_date', sortOrder: 'desc'}],
    ['Date (Oldest First)', {sortBy: 'order_date', sortOrder: 'asc'}],
    ['Price (High to Low)', {sortBy: 'price', sortOrder: 'desc'}],
    ['Price (Low to High)', {sortBy: 'price', sortOrder: 'asc'}],
    ['Client Name A-Z', {sortBy: 'client_name', sortOrder: 'asc'}],
    ['Client Name Z-A', {sortBy: 'client_name', sortOrder: 'desc'}],
    ['Client Email A-Z', {sortBy: 'client_email', sortOrder: 'asc'}],
    ['Client Email Z-A', {sortBy: 'client_email', sortOrder: 'desc'}],
    ['Employee Name A-Z', {sortBy: 'employee_name', sortOrder: 'asc'}],
    ['Employee Name Z-A', {sortBy: 'employee_name', sortOrder: 'desc'}],
    ['Employee Email A-Z', {sortBy: 'employee_email', sortOrder: 'asc'}],
    ['Employee Email Z-A', {sortBy: 'employee_email', sortOrder: 'desc'}]
]);

export const employeeOrderSortOptions = new Map<string, {sortBy: OrderSortField, sortOrder: SortOrder}>([
    ['Date (Newest First)', {sortBy: 'order_date', sortOrder: 'desc'}],
    ['Date (Oldest First)', {sortBy: 'order_date', sortOrder: 'asc'}],
    ['Price (High to Low)', {sortBy: 'price', sortOrder: 'desc'}],
    ['Price (Low to High)', {sortBy: 'price', sortOrder: 'asc'}],
    ['Client Name A-Z', {sortBy: 'client_name', sortOrder: 'asc'}],
    ['Client Name Z-A', {sortBy: 'client_name', sortOrder: 'desc'}],
    ['Client Email A-Z', {sortBy: 'client_email', sortOrder: 'asc'}],
    ['Client Email Z-A', {sortBy: 'client_email', sortOrder: 'desc'}],
]);

export const clientOrderSortOptions = new Map<string, {sortBy: OrderSortField, sortOrder: SortOrder}>([
    ['Date (Newest First)', {sortBy: 'order_date', sortOrder: 'desc'}],
    ['Date (Oldest First)', {sortBy: 'order_date', sortOrder: 'asc'}],
    ['Price (High to Low)', {sortBy: 'price', sortOrder: 'desc'}],
    ['Price (Low to High)', {sortBy: 'price', sortOrder: 'asc'}],
    ['Employee Name A-Z', {sortBy: 'employee_name', sortOrder: 'asc'}],
    ['Employee Name Z-A', {sortBy: 'employee_name', sortOrder: 'desc'}],
    ['Employee Email A-Z', {sortBy: 'employee_email', sortOrder: 'asc'}],
    ['Employee Email Z-A', {sortBy: 'employee_email', sortOrder: 'desc'}]
]);

export const employeeSortOptionsWithMappings = new Map<string, {sortBy: EmployeeSortField, sortOrder: SortOrder}>([
    ['Name A-Z', {sortBy: 'name', sortOrder: 'asc'}],
    ['Name Z-A', {sortBy: 'name', sortOrder: 'desc'}],
    ['Email A-Z', {sortBy: 'email', sortOrder: 'asc'}],
    ['Email Z-A', {sortBy: 'email', sortOrder: 'desc'}],
    ['Birth Date (Oldest First)', {sortBy: 'birthdate', sortOrder: 'asc'}], // maps to birthDate
    ['Birth Date (Youngest First)', {sortBy: 'birthdate', sortOrder: 'desc'}]
]);

export const clientSortOptionsWithMappings = new Map<string, {sortBy: ClientSortField, sortOrder: SortOrder}>([
    ['Name A-Z', {sortBy: 'name', sortOrder: 'asc'}],
    ['Name Z-A', {sortBy: 'name', sortOrder: 'desc'}],
    ['Email A-Z', {sortBy: 'email', sortOrder: 'asc'}],
    ['Email Z-A', {sortBy: 'email', sortOrder: 'desc'}],
    ['Balance (High to Low)', {sortBy: 'balance', sortOrder: 'desc'}],
    ['Balance (Low to High)', {sortBy: 'balance', sortOrder: 'asc'}]
]);

export const ageGroups: string[] = ['Child', 'Teen', 'Adult', 'Other'];
export const languages: string[] = ['English', 'Spanish', 'French', 'German', 'Ukrainian', 'Japanese', 'Other'];
export const genres: string[] =  [
    'Fiction', 'Non-Fiction', 'Mystery', 'Romance', 'Fantasy', 'Science Fiction',
    'Thriller', 'Biography', 'History', 'Self-Help', 'Technical', 'Drama',
    'Comedy', 'Horror', 'Adventure', 'Poetry', 'Philosophy', 'Religion',
    'Art', 'Travel', 'Cooking', 'Health', 'Business', 'Education', 'Classic Literature','Historical Fiction',
    'Literary Fiction', 'Action', 'Cyberpunk', 'Paranormal', 'Young Adult', 'Steampunk'
];

export const PAGE_SIZE: number = 20;

export const API_ENDPOINTS = {
    books: {
        getAll: '/books',
        getByName: (name: string) => `/books/${encodeURIComponent(name)}`,
        create: '/books',
        update: (name: string) => `/books/${encodeURIComponent(name)}`,
        delete: (name: string) => `/books/${encodeURIComponent(name)}`,
        search: '/books/search',
    },
    orders: {
        getAll: '/orders',
        getByClient: (email: string) => `/clients/${encodeURIComponent(email)}/orders`,
        getByEmployee: (email: string) => `/employees/${encodeURIComponent(email)}/orders`,
        create: '/orders',
    },
    clients: {
        getAll: '/clients',
        getByEmail: (email: string) => `/clients/${encodeURIComponent(email)}`,
        update: (email: string) => `/clients/${encodeURIComponent(email)}`,
        delete: (email: string) => `/clients/${encodeURIComponent(email)}`,
        isBlocked: (email: string) => `/clients/blocked/${encodeURIComponent(email)}`,
        block: (email: string) => `/clients/blocked/${encodeURIComponent(email)}`,
        unblock: (email: string) => `/clients/blocked/${encodeURIComponent(email)}`,
        getAllBlockedList: () => '/clients/blocked/list',
        getAllBlocked: () => '/clients/blocked'
    },
    employees: {
        getAll: '/employees',
        getByEmail: (email: string) => `/employees/${encodeURIComponent(email)}`,
        update: (email: string) => `/employees/${encodeURIComponent(email)}`,
        delete: (email: string) => `/employees/${encodeURIComponent(email)}`,
    },
    auth: {
        login: '/auth/login',
        refresh: '/auth/refresh-token',
        logout: '/auth/logout',
        forgotPassword: '/auth/forgot-password',
        changePassword: '/auth/change-password',
        registerClient: '/auth/register/client',
    },
} as const;