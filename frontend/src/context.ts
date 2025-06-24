import  { createContext} from 'react';
import { Role, User, Basket, BookItem, Book } from "./types";

export type AppContext = {
    user: User | null;
    role: Role | null;
    isAuthenticated: boolean;
    setUser: (user: User | null) => void;
    setRole: (role: Role) => void;
    cleanUser: () => void;

    login: (email: string, password: string, role: Role) => Promise<void>;
    logout: () => Promise<void>;

    basket: Basket;
    addToBasket: (bookItem: BookItem) => void;
    removeFromBasket: (bookName: string) => void;
    updateBasketQuantity: (bookItem: BookItem) => void;
    clearBasket: () => void;
};

const defaultContext: AppContext = {
    user: null,
    role: null,
    isAuthenticated: false,
    setUser: () => {},
    setRole: () => {},
    cleanUser: () => {},
    login: async () => {},
    logout: async () => {},
    basket: [],
    addToBasket: () => {},
    removeFromBasket: () => {},
    updateBasketQuantity: () => {},
    clearBasket: () => {}
}

export const AppContext = createContext<AppContext>(defaultContext);


export const createBookItem = (book: Book, quantity: number = 1): BookItem => {
    return {
        bookName: book.name,
        quantity: quantity
    };
};

export const isClient = (user: User | null): user is import('./types').ClientDTO => {
    return user !== null && 'balance' in user;
};

export const isEmployee = (user: User | null): user is import('./types').EmployeeDTO => {
    return user !== null && 'phone' in user && 'birthdate' in user;
};