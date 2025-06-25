import React from 'react';
import {
    User,
    Role,
    Basket,
    BookItem,
    Client,
    LoginRequest,
    ForgotPasswordDTO,
    ResetPasswordDTO
} from './types';

export type AppContextType = {
    user: User | null;
    role: Role | null;
    basket: Basket;

    login: (request: LoginRequest) => Promise<void>;
    logout: () => Promise<void>;
    forgotPassword: (data: ForgotPasswordDTO) => Promise<void>;
    resetPassword: (data: ResetPasswordDTO) => Promise<void>;
    registerClient: (client: Client) => Promise<void>;
    refreshAuthToken: () => Promise<boolean>;

    setUser: (user: User | null) => void;
    setRole: (role: Role) => void;
    cleanUser: () => void;

    addToBasket: (book: BookItem) => void;
    removeFromBasket: (bookName: string) => void;
    clearBasket: () => void;
}

const defaultContextValue: AppContextType = {
    user: null,
    role: null,
    basket: [],

    login: async () => { throw new Error('AppContext not initialized') },
    logout: async () => { throw new Error('AppContext not initialized') },
    forgotPassword: async () => { throw new Error('AppContext not initialized') },
    resetPassword: async () => { throw new Error('AppContext not initialized') },
    registerClient: async () => { throw new Error('AppContext not initialized') },
    refreshAuthToken: async () => { throw new Error('AppContext not initialized') },

    setUser: () => { throw new Error('AppContext not initialized') },
    setRole: () => { throw new Error('AppContext not initialized') },
    cleanUser: () => { throw new Error('AppContext not initialized') },

    addToBasket: () => { throw new Error('AppContext not initialized') },
    removeFromBasket: () => { throw new Error('AppContext not initialized') },
    clearBasket: () => { throw new Error('AppContext not initialized') }
};

export const AppContext = React.createContext<AppContextType>(defaultContextValue);