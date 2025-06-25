import React, {ReactNode, useEffect, useState} from "react";
import {
    API_ENDPOINTS,
    Basket,
    BookItem,
    Client,
    ForgotPasswordDTO,
    LoginRequest,
    Role,
    TokenResponseDTO,
    User
} from "./types";
import {apiClient} from "./config/ApiClient";
import { AppContext } from './context';
import {Icon} from "./components/Icon/Icon";

type AppState = {
    user: User | null;
    role: Role | null;
    basket: Basket;
    loading: boolean;
    error: string | null;
}

const initialState: AppState = {
    user: null,
    role: null,
    basket: [],
    loading: true,
    error: null
};

export const AppProvider: React.FC<{children: ReactNode}> = ({ children }) => {
    const [state, setState] = useState<AppState>(initialState);

    const updateState = (updates: Partial<AppState>) => {
        setState(prevState => ({ ...prevState, ...updates }));
    };

    useEffect(() => {
        const initializeAuth = () => {
            try {
                const storedUser = localStorage.getItem('user');
                const storedRole = localStorage.getItem('role');
                const accessToken = localStorage.getItem('accessToken');

                if (storedUser && storedRole && accessToken) {
                    const parsedUser = JSON.parse(storedUser);
                    updateState({
                        user: parsedUser,
                        role: storedRole as Role,
                        loading: false,
                        error: null
                    });

                    apiClient.setDefaultHeader('Authorization', `Bearer ${accessToken}`);
                }else updateState({loading: false});
            } catch (error) {
                console.error('Error initializing auth state:', error);
                cleanUser();
            }
        };

        initializeAuth();
    }, []);

    const setUser = (newUser: User | null) => {
        if (newUser) {
            localStorage.setItem('user', JSON.stringify(newUser));
            updateState({user: newUser, error: null});
        } else {
            localStorage.removeItem('user');
            updateState({user: null});
        }
    };

    const setRole = (newRole: Role) => {
        updateState({role: newRole});
        localStorage.setItem('role', newRole);
    };

    const cleanUser = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        apiClient.setDefaultHeader('Authorization', '');

        updateState({
            user: null,
            role: null,
            basket: [],
            loading: false,
            error: null
        });
    };

    const login = async (request: LoginRequest): Promise<void> => {
        updateState({ loading: true, error: null });
        try {
            const response = await apiClient.post<TokenResponseDTO>('/auth/login', {
                email: request.email,
                password: request.password,
                role: request.role
            });

            const { accessToken, refreshToken} = response.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            apiClient.setDefaultHeader('Authorization', `Bearer ${accessToken}`);

            let userResponse;
            if (request.role === 'CLIENT') userResponse = await apiClient.get(API_ENDPOINTS.clients.getByEmail(request.email));
            else userResponse = await apiClient.get(API_ENDPOINTS.employees.getByEmail(request.email));

            const userData = userResponse.data;
            updateState({user: userData, role: request.role, loading: false,});
        } catch (error) {
            console.error('Login failed:', error);
            updateState({loading: false, error: 'Login failed. Please check your credentials.'});
            throw new Error('Login failed. Please check your credentials.');
        }
    };
    const registerClient = async (client: Client): Promise<void> => {
        updateState({ loading: true, error: null });
        try {
            await apiClient.post(API_ENDPOINTS.auth.registerClient, client);
        } catch (error) {
            console.error('Client registration failed:', error);
            updateState({error: 'Registration failed. Please try again.'});
            throw new Error('Registration failed. Please try again.');
        }finally {
            updateState({ loading: false });
        }
    };

    const logout = async (): Promise<void> => {
        updateState({ loading: true });
        try {
            await apiClient.post(API_ENDPOINTS.auth.logout);
        } catch (error) {
            console.error('Logout request failed:', error);
        }finally {
            cleanUser();
        }
    };

    const forgotPassword = async (data: ForgotPasswordDTO): Promise<void> => {
        updateState({loading: true})
        try{
            await apiClient
        }
    }

    const addToBasket = (bookItem: BookItem) => {
        setState(prevState => {
            const existingItemIndex = prevState.basket
                .findIndex(item => item.bookName === bookItem.bookName);

            let newBasket: Basket;

            if (existingItemIndex !== -1) {
                newBasket = [...prevState.basket];
                newBasket[existingItemIndex].quantity += bookItem.quantity;
            } else newBasket = [...prevState.basket, bookItem]
            return{
                ...prevState,
                basket: newBasket
            }
        });
    };

    const removeFromBasket = (bookName: string) => {
        setState(prevState =>({
            ...prevState,
            basket: prevState.basket.filter(item => item.bookName !== bookName)
        }));
    };

    const updateBasketQuantity = (bookItem: BookItem) => {
        setState(prevState =>({
            ...prevState,
            basket: prevState.basket
                .map(item => item.bookName === bookItem.bookName
                    ? {...item, quantity: bookItem.quantity} : item)
        }));
    };

    const clearBasket = () => {
        updateState({basket: []});
    };

    const contextValue = {
        user: state.user,
        role: state.role,
        setUser,
        setRole,
        cleanUser,
        login,
        logout,
        basket: state.basket,
        addToBasket,
        removeFromBasket,
        updateBasketQuantity,
        clearBasket,
        registerClient
    };

    if (state.loading) return <Icon topic='loading' size='big'/>;

    return (
        <AppContext.Provider value={contextValue}>
            {children}
        </AppContext.Provider>
    );
};
