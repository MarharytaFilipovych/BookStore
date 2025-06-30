export type Language = 'ENGLISH' | 'SPANISH' | 'FRENCH' | 'GERMAN' | 'UKRAINIAN' | 'JAPANESE' | 'OTHER';
export type AgeGroup = 'CHILD' | 'TEEN' | 'ADULT' | 'OTHER';
export type Role = 'CLIENT' | 'EMPLOYEE';
export type SortOrder = 'asc' | 'desc';
export type EmployeeSortField = 'name' | 'email' | 'birthdate';
export type ClientSortField = 'name' | 'email' | 'balance';
export type OrderSortField = 'order_date' | 'price' | 'client_email' | 'employee_email' | 'client_name' | 'employee_name';
export type BookSortField = 'name' | 'author' | 'genre' | 'price' | 'publication_date' | 'age_group' | 'pages';
export type ForWhomOrder = 'all' | 'employee' | 'client';
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
    book_name: string;
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
    total_pages: number;
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

export type requestType = 'people/clients' | 'orders' | 'books' | 'people/colleagues' | 'profile' | 'my-orders' | 'people/blocked';

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

export type ForgotPasswordDTO = {
    email: string;
    role: Role;
}

export type ResetPasswordDTO = {
    email: string;
    password: string;
    reset_code: string;
    role: Role;
}

export type RefreshTokenDTO = {
    refresh_token: string;
    email: string;
    role: Role;
}

export type LogoutDTO = {
    email: string;
    role: Role;
}

export type TokenResponseDTO = {
    access_token: string;
    refresh_token: string;
    expires_in: number;
}

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