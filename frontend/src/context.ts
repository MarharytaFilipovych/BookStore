import React from 'react';
import {User, Role, Basket, ClientType, LoginRequest, ForgotPassword, ResetPassword} from './types';

export type AppContextType = {
    user: User | null;
    role: Role;
    basket: Basket;
    isLoading: boolean;

    login: (request: LoginRequest) => Promise<void>;
    logout: () => Promise<void>;
    forgotPassword: (data: ForgotPassword) => Promise<void>;
    resetPassword: (data: ResetPassword) => Promise<void>;
    registerClient: (client: ClientType) => Promise<void>;

    setUser: (user: User | null) => void;
    setRole: (role: Role) => void;
    cleanUser: () => void;

    addToBasket: (name: string, quantity?: number) => void;
    removeFromBasket: (bookName: string) => void;
    clearBasket: () => void;
    checkQuantity: (name: string) => number;
}

const defaultContextValue: AppContextType = {
    user: null,
    role: 'CLIENT',
    basket: [],
    isLoading: true,

    login: async () => { throw new Error('AppContext not initialized') },
    logout: async () => { throw new Error('AppContext not initialized') },
    forgotPassword: async () => { throw new Error('AppContext not initialized') },
    resetPassword: async () => { throw new Error('AppContext not initialized') },
    registerClient: async () => { throw new Error('AppContext not initialized') },

    setUser: () => { throw new Error('AppContext not initialized') },
    setRole: () => { throw new Error('AppContext not initialized') },
    cleanUser: () => { throw new Error('AppContext not initialized') },

    addToBasket: () => { throw new Error('AppContext not initialized') },
    removeFromBasket: () => { throw new Error('AppContext not initialized') },
    clearBasket: () => { throw new Error('AppContext not initialized') },
    checkQuantity: ()=> { throw new Error('AppContext not initialized') }
};

export const AppContext = React.createContext<AppContextType>(defaultContextValue);