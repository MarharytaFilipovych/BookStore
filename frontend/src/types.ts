export type Language = 'ENGLISH' | 'SPANISH' | 'FRENCH' | 'GERMAN' | 'UKRAINIAN' | 'JAPANESE' | 'OTHER';
export type AgeGroup = 'CHILD' | 'TEEN' | 'ADULT' | 'OTHER';
export type Role = 'CLIENT' | 'EMPLOYEE';
export type SortOrder = 'asc' | 'desc';
export type EmployeeSortField = 'name' | 'email' | 'birthdate';
export type ClientSortField = 'name' | 'email' | 'balance';
export type OrderSortField = 'order_date' | 'price' | 'client_email' | 'employee_email' | 'client_name' | 'employee_name';
export type BookSortField = 'name' | 'author' | 'genre' | 'price' | 'publication_date' | 'age_group' | 'pages';
export type SortField = BookSortField | ClientSortField | OrderSortField | EmployeeSortField;
export type User = ClientType | EmployeeType;
export type BookType = {
    name: string;
    genre: string;
    age_group: AgeGroup;
    price: number;
    publication_date: string;
    author: string;
    pages: number;
    characteristics: string;
    description: string;
    language: Language;
}

export type BookItem = {
    bookName: string;
    quantity: number;
}

export type Basket = BookItem[]

export type ClientType = {
    name: string;
    email: string;
    password?: string;
    balance: number;
}

export type EmployeeType = {
    name: string;
    email: string;
    phone: string;
    password?: string;
    birthdate: string;
}

export type OrderType = {
    employee_email?: string;
    client_email: string;
    order_date: string;
    price: number;
    book_items: BookItem[];
}

export type LoginRequest = {
    email: string;
    password: string;
    role: Role;
}

export type SearchBook = {
    name?: string;
    genre?: string;
    author?: string;
    language?: Language;
    ageGroup?: AgeGroup;
    minPrice?: number;
    maxPrice?: number;
    minPages?: number;
    maxPages?: number;
    publicationYear?: number;
}

export type MetaDTO = {
    page: number;
    page_size: number;
    total_count: number;
    totalPages: number;
    has_next: boolean;
    has_previous: boolean;
}

export type PaginatedResponseDTO<T> = {
    meta: MetaDTO;
    books?: T[];
    orders?: T[];
    employees?: T[];
    clients?: T[];
}


export interface UpdateBookRequest extends Partial<BookType> {}

export type IconTopic = 'search' | 'tick' | 'star' | 'vote'
    | 'cross' | 'envelope' | 'call' | 'empty-star'
    | 'black-cross' | 'caret' | 'heart' | 'empty-heart'
    | 'empty-circle' | 'circle' | 'loading' | 'error'
    | 'direction' | 'hidden' | 'plus' | 'basket' | 'bin' | 'ban' | 'update';

export type ContactProp = {
    typeOfContact: 'email' | 'call';
    contact: string;
}

export type LinkItem = {
    name: string;
    link: requestType;
}

export type Links = {
    employeeLinks: LinkItem[];
    clientLinks: LinkItem[];
}

export type ConfigurationData = {
    countries: Map<string, string>;
    languages: Map<string, string>;
    genres: Map<string, string>;
    code_languages: Map<string, string>;
}

export type requestType = 'people/clients' | 'orders' | 'books' | 'people/colleagues' | 'profile';


export type State = {
    loading: boolean;
    error: boolean;
}

export type StateWithPagination = State & {
    totalPages: number,
    totalResults: number,
    pageToFetch: number;
    currentPage: number;
}

export type Optional<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;
export type RequiredFields<T, K extends keyof T> = T & Required<Pick<T, K>>;

export type ForgotPasswordDTO = {
    email: string;
    role: Role;
}

export type ResetPasswordDTO = {
    email: string;
    password: string;
    resetCode: string;
    role: Role;
}

export type RefreshTokenDTO = {
    refreshToken: string;
    email: string;
    role: Role;
}

export type LogoutDTO = {
    email: string;
    role: Role;
    refreshToken: string;
}

export type TokenResponseDTO = {
    accessToken: string;
    refreshToken: string;
    expiresIn: number; // seconds
}

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

export type BookFilterState = {
    name: string;
    genre: string;
    author: string;
    language: string;
    ageGroup: string;
    minPrice: string;
    maxPrice: string;
    minPages: string;
    maxPages: string;
    publicationYear: string;
    sort: string;
}

export type PersonFilterState = {
    email: string;
    sort: string;
}

export type OrderFilterState = {
    clientEmail: string;
    employeeEmail: string;
    sort: string;
}