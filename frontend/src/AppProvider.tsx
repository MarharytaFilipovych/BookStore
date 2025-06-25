import React, {ReactNode, useEffect, useState} from "react";
import {
    Basket,
    BookItem,
    Client,
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

type AppState = {
    user: User | null;
    role: Role | null;
    basket: Basket;
}

const initialState: AppState = {
    user: null,
    role: null,
    basket: [],
};

export const AppProvider: React.FC<{children: ReactNode}> = ({ children }) => {
    const [state, setState] = useState<AppState>(initialState);

    const updateState = (updates: Partial<AppState>) => {
        setState(prevState => ({ ...prevState, ...updates }));
    };

    useEffect(() => {
        const initializeAuth =  () => {
            try {
                const storedUser = localStorage.getItem('user');
                const storedRole = localStorage.getItem('role');
                const accessToken = AuthService.getToken();

                if (storedUser && storedRole && accessToken && AuthService.isAuthenticated()) {
                    const parsedUser = JSON.parse(storedUser);
                    updateState({user: parsedUser, role: storedRole as Role});

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
            role: null,
            basket: [],
        });
    };

    const login = async (request: LoginRequest): Promise<void> => {
        try {
            const tokenResponse = await AuthService.login(request);

            apiClient.setDefaultHeader('Authorization', `Bearer ${tokenResponse.accessToken}`);

            let userResponse;
            if (request.role === 'CLIENT') {
                userResponse = await ClientService.getClientByEmail(request.email);
            } else {
                userResponse = await EmployeeService.getEmployeeByEmail(request.email);
            }

            const userData = userResponse;
            setUser(userData);
            setRole(request.role);
        } catch (error) {
            console.error('Login failed:', error);
            throw new Error('Login failed. Please check your credentials.');
        }
    };

    const logout = async (): Promise<void> => {
        try {
            if (state.user?.email && state.role) {
                await AuthService.logout({
                    email: state.user.email,
                    role: state.role,
                    refreshToken: AuthService.getRefreshToken() || ''
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

    const registerClient = async (client: Client): Promise<void> => {
        try {
            await AuthService.registerClient(client);
        } catch (error) {
            console.error('Client registration failed:', error);
            throw new Error('Registration failed. Please try again.');
        }
    };

    const addToBasket = (book: BookItem) => {
        updateState({
            basket: [...state.basket, book]
        });
    };

    const removeFromBasket = (bookName: string) => {
        updateState({
            basket: state.basket.filter(book => book.bookName !== bookName)
        });
    };

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
                refreshToken,
                email: state.user.email,
                role: state.role
            });

            apiClient.setDefaultHeader('Authorization', `Bearer ${tokenResponse.accessToken}`);
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
    };

    return (
        <AppContext.Provider value={contextValue}>
            {children}
        </AppContext.Provider>
    );
};