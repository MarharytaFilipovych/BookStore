import React, {ReactNode, useEffect, useState} from "react";
import {
    Basket,
    BookItem,
    ClientType,
    ForgotPasswordDTO,
    LoginRequest,
    ResetPasswordDTO,
    Role,
    User
} from "./types";
import {apiClient} from "./config/ApiClient";
import { AppContext } from './context';
import { AuthService } from './services/AuthService';
import { ClientService } from './services/ClientService';
import { EmployeeService } from './services/EmployeeService';
import {useStateWithUpdater} from "./hooks/useStateWithUpdater";

type AppState = {
    user: User | null;
    role: Role;
    basket: Basket;
    isLoading: boolean;
}

const initialState: AppState = {
    user: null,
    role: 'CLIENT',
    basket: [],
    isLoading: true,
};

export const AppProvider: React.FC<{children: ReactNode}> = ({ children }) => {
    const [state, updateState] = useStateWithUpdater<AppState>(initialState);

    useEffect(() => {
        const initializeAuth = () => {
            try {
                const storedUser = localStorage.getItem('user');
                const storedRole = localStorage.getItem('role');
                const accessToken = AuthService.getToken();

                if (storedUser && storedRole && accessToken && AuthService.isAuthenticated()) {
                    const parsedUser = JSON.parse(storedUser);
                    updateState({user: parsedUser, role: storedRole as Role, isLoading: false});

                    apiClient.setDefaultHeader('Authorization', `Bearer ${accessToken}`);
                } else {
                    cleanUser();
                }
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
            updateState({user: newUser});
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
        AuthService.clearTokens();
        apiClient.setDefaultHeader('Authorization', '');

        updateState({
            user: null,
            role: 'CLIENT',
            basket: [],
            isLoading: false, // Set loading to false after cleanup
        });
    };

    const login = async (request: LoginRequest): Promise<void> => {
        console.log('🔐 AppProvider: Starting login process...', {
            email: request.email,
            role: request.role
        });

        try {
            // Step 1: Get tokens from backend
            console.log('📡 AppProvider: Calling AuthService.login...');
            const tokenResponse = await AuthService.login(request);
            console.log('✅ AppProvider: Tokens received', {
                hasAccessToken: !!tokenResponse.access_token,
                hasRefreshToken: !!tokenResponse.refresh_token,
                expiresIn: tokenResponse.expires_in
            });

            // Step 2: Set authorization header
            console.log('🔑 AppProvider: Setting authorization header...');
            apiClient.setDefaultHeader('Authorization', `Bearer ${tokenResponse.access_token}`);
            console.log('✅ AppProvider: Authorization header set');

            // Step 3: Fetch user data
            console.log('👤 AppProvider: Fetching user data...');
            let userResponse;
            if (request.role === 'CLIENT') {
                console.log('📞 AppProvider: Calling ClientService.getClientByEmail...');
                userResponse = await ClientService.getClientByEmail(request.email);
            } else {
                console.log('📞 AppProvider: Calling EmployeeService.getEmployeeByEmail...');
                userResponse = await EmployeeService.getEmployeeByEmail(request.email);
            }
            console.log('✅ AppProvider: User data received', {
                userName: userResponse?.name,
                userEmail: userResponse?.email
            });

            // Step 4: Set user and role in context
            console.log('💾 AppProvider: Setting user in context...');
            const userData = userResponse;
            setUser(userData);
            console.log('✅ AppProvider: User set in context:', userData);

            console.log('🏷️ AppProvider: Setting role in context...');
            setRole(request.role);
            console.log('✅ AppProvider: Role set in context:', request.role);

            console.log('🎉 AppProvider: Login process completed successfully!');

        } catch (error) {
            console.error('❌ AppProvider: Login failed at some step:', error);

            // More detailed error logging
            if (error instanceof Error) {
                console.error('❌ AppProvider: Error details:', {
                    message: error.message,
                    stack: error.stack
                });
            }

            throw new Error('Login failed. Please check your credentials.');
        }
    };

    const logout = async (): Promise<void> => {
        try {
            if (state.user?.email && state.role) {
                await AuthService.logout({
                    email: state.user.email,
                    role: state.role,
                });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            cleanUser();
        }
    };

    const forgotPassword = async (data: ForgotPasswordDTO): Promise<void> => {
        try {
            await AuthService.forgotPassword(data);
        } catch (error) {
            console.error('Forgot password failed:', error);
            throw error;
        }
    };

    const resetPassword = async (data: ResetPasswordDTO): Promise<void> => {
        try {
            await AuthService.resetPassword(data);
        } catch (error) {
            console.error('Reset password failed:', error);
            throw error;
        }
    };

    const registerClient = async (client: ClientType): Promise<void> => {
        try {
            await AuthService.registerClient(client);
        } catch (error) {
            console.error('Client registration failed:', error);
            throw new Error('Registration failed. Please try again.');
        }
    };

    const addToBasket = (name: string, quantity: number = 1) => {
        const existingItem = state.basket.find(b => b.book_name === name);

        const updatedBasket = existingItem
            ? state.basket.map(item =>
                item.book_name ===name
                    ? { ...item, quantity: item.quantity + quantity }
                    : item
            )
            : [...state.basket, {book_name: name, quantity: quantity}];

        updateState({ basket: updatedBasket });
    };

    const removeFromBasket = (bookName: string) => {
        updateState({
            basket: state.basket.filter(book => book.book_name !== bookName)
        });
    };

    const checkQuantity =  (name: string): number =>{
        const book = state.basket.find(b => b.book_name === name);
        if(book)return book.quantity;
        return 0;
    }

    const clearBasket = () => {
        updateState({ basket: [] });
    };

    const refreshAuthToken = async (): Promise<boolean> => {
        try {
            const refreshToken = AuthService.getRefreshToken();
            if (!refreshToken || !state.user?.email || !state.role) {
                return false;
            }

            const tokenResponse = await AuthService.refreshToken({
                refresh_token: refreshToken,
                email: state.user.email,
                role: state.role
            });

            apiClient.setDefaultHeader('Authorization', `Bearer ${tokenResponse.access_token}`);
            return true;
        } catch (error) {
            console.error('Token refresh failed:', error);
            cleanUser();
            return false;
        }
    };

    const contextValue = {
        user: state.user,
        role: state.role,
        basket: state.basket,
        isLoading: state.isLoading,

        login,
        logout,
        forgotPassword,
        resetPassword,
        registerClient,
        refreshAuthToken,

        setUser,
        setRole,
        cleanUser,

        addToBasket,
        removeFromBasket,
        clearBasket,
        checkQuantity
    };

    return (
        <AppContext.Provider value={contextValue}>
            {children}
        </AppContext.Provider>
    );
};