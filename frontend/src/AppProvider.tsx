import React, {ReactNode, useEffect, useCallback} from "react";
import {Basket, ClientType, ForgotPassword, LoginRequest, ResetPassword, Role, User} from "./types";
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
            isLoading: false,
        });
    };

    const initializeAuth = async () => {
        try {
            const storedUser = localStorage.getItem('user');
            const storedRole = localStorage.getItem('role');
            const accessToken = AuthService.getToken();
            const refreshToken = localStorage.getItem('refreshToken');

            console.log('🔍 AppProvider: Initializing auth...', {
                hasStoredUser: !!storedUser,
                hasStoredRole: !!storedRole,
                hasAccessToken: !!accessToken,
                hasRefreshToken: !!refreshToken
            });

            if (storedUser && storedRole && (accessToken || refreshToken)) {
                const parsedUser = JSON.parse(storedUser);

                updateState({
                    user: parsedUser,
                    role: storedRole as Role,
                    isLoading: false
                });

                console.log('✅ AppProvider: User data set', {
                    userEmail: parsedUser.email,
                    role: storedRole
                });

                if (accessToken && AuthService.isAuthenticated()) {
                    apiClient.setDefaultHeader('Authorization', `Bearer ${accessToken}`);
                    console.log('✅ AppProvider: Valid token found, user authenticated');
                } else {
                    console.log('🔄 AppProvider: Token expired/missing, attempting refresh...');
                    if (refreshToken) {
                        try {
                            console.log('🔄 AppProvider: Calling refresh with user data', {
                                email: parsedUser.email,
                                role: storedRole,
                                hasRefreshToken: !!refreshToken
                            });

                            const tokenResponse = await AuthService.refreshToken({
                                refresh_token: refreshToken,
                                email: parsedUser.email,
                                role: storedRole as Role
                            });

                            apiClient.setDefaultHeader('Authorization', `Bearer ${tokenResponse.access_token}`);
                            console.log('✅ AppProvider: Token refreshed successfully during initialization');
                        } catch (refreshError) {
                            console.error('❌ AppProvider: Token refresh failed during initialization:', refreshError);
                            cleanUser();
                            return;
                        }
                    } else {
                        console.log('❌ AppProvider: No refresh token available, logging out');
                        cleanUser();
                        return;
                    }
                }
            } else {
                console.log('❌ AppProvider: Missing authentication data, cleaning up');
                cleanUser();
            }
        } catch (error) {
            console.error('❌ AppProvider: Error during auth initialization:', error);
            cleanUser();
        }
    };

    useEffect(() => {
        initializeAuth();
    }, []);

    const login = async (request: LoginRequest): Promise<void> => {
        console.log('🔐 AppProvider: Starting login process...', {
            email: request.email,
            role: request.role
        });

        try {
            const tokenResponse = await AuthService.login(request);
            apiClient.setDefaultHeader('Authorization', `Bearer ${tokenResponse.access_token}`);
            let userResponse;
            if (request.role === 'CLIENT') userResponse = await ClientService.getClientByEmail(request.email);
            else userResponse = await EmployeeService.getEmployeeByEmail(request.email);
            setUser(userResponse);
            setRole(request.role);
            console.log('✅ AppProvider: Login completed successfully');
        } catch (error) {
            console.error('❌ AppProvider: Login failed:', error);
            throw new Error('Login failed. Please check your credentials.');
        }
    };

    const logout = async (): Promise<void> => {
        try {
            if (state.user?.email && state.role) await AuthService.logout({email: state.user.email, role: state.role});
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            cleanUser();
        }
    };

    const forgotPassword = async (data: ForgotPassword): Promise<void> => {
        try {
            await AuthService.forgotPassword(data);
        } catch (error) {
            console.error('Forgot password failed:', error);
            throw error;
        }
    };

    const resetPassword = async (data: ResetPassword): Promise<void> => {
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

        setUser,
        setRole,
        cleanUser,

        addToBasket,
        removeFromBasket,
        clearBasket,
        checkQuantity,
    };

    return (
        <AppContext.Provider value={contextValue}>
            {children}
        </AppContext.Provider>
    );
};