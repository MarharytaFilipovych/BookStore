import axios from 'axios';
import { apiClient } from '../config/ApiClient';
import {LoginRequest, TokenResponseDTO, ForgotPassword, ResetPassword, RefreshTokenDTO, LogoutDTO, ClientType} from '../types';
import {API_ENDPOINTS} from "../BusinessData";

export class AuthService {
    static async login(credentials: LoginRequest): Promise<TokenResponseDTO> {
        console.log('🔐 AuthService: Starting login process...', {
            email: credentials.email,
            role: credentials.role,
            hasPassword: !!credentials.password
        });

        try {
            const response = await apiClient.post<TokenResponseDTO>(
                API_ENDPOINTS.auth.login, credentials);

            console.log('✅ AuthService: Login successful', {
                hasAccessToken: !!response.data.access_token,
                hasRefreshToken: !!response.data.refresh_token,
                expiresIn: response.data.expires_in,
                role: credentials.role
            });
            if (response.data.access_token) localStorage.setItem('accessToken', response.data.access_token);
            if (response.data.refresh_token) localStorage.setItem('refreshToken', response.data.refresh_token);
            return response.data;
        } catch (error) {
            console.error('❌ AuthService: Login failed', error);
            AuthService.clearTokens();
            throw error;
        }
    }

    static async logout(logoutData: LogoutDTO): Promise<void> {
        console.log('🚪 AuthService: Starting logout process...', {
            email: logoutData.email,
            role: logoutData.role
        });
        try {
            await apiClient.post(API_ENDPOINTS.auth.logout, logoutData);
            console.log('✅ AuthService: Backend logout successful');
        } catch (error) {
            console.warn('⚠️ AuthService: Backend logout failed (continuing with cleanup):', error);
        } finally {
            AuthService.clearTokens();
            console.log('🧹 AuthService: Local cleanup completed');
        }
    }

    static async forgotPassword(data: ForgotPassword): Promise<string> {
        console.log('🔑 AuthService: Initiating password reset...', {
            email: data.email,
            role: data.role
        });
        try {
            const response = await apiClient.post<string>(API_ENDPOINTS.auth.forgotPassword, data);
            console.log('✅ AuthService: Password reset email sent successfully');
            return response.data;
        } catch (error) {
            console.error('❌ AuthService: Password reset request failed', error);
            throw error;
        }
    }

    static async resetPassword(data: ResetPassword): Promise<void> {
        console.log('🔐 AuthService: Resetting password...', {
            email: data.email,
            hasPassword: !!data.password,
            hasResetCode: !!data.reset_code
        });
        try {
            await apiClient.post(API_ENDPOINTS.auth.changePassword, data);
            console.log('✅ AuthService: Password reset successful');
        } catch (error) {
            console.error('❌ AuthService: Password reset failed', error);
            throw error;
        }
    }

    static isAuthenticated(): boolean {
        const token = AuthService.getToken();
        if (!token) {
            console.log('⚠️ AuthService: No access token available');
            return false;
        }
        try {
            const tokenParts = token.split('.');
            if (tokenParts.length !== 3) {
                console.warn('⚠️ AuthService: Invalid token format');
                return false;
            }

            const payload = JSON.parse(atob(tokenParts[1]));
            const currentTime = Math.floor(Date.now() / 1000);

            const BUFFER_SECONDS = 60;
            const effectiveExpiry = payload.exp - BUFFER_SECONDS;

            if (payload.exp && effectiveExpiry < currentTime) {
                console.warn('⚠️ AuthService: Token will expire soon (within buffer time)', {
                    actualExpiry: payload.exp ? new Date(payload.exp * 1000).toISOString() : 'unknown',
                    effectiveExpiry: new Date(effectiveExpiry * 1000).toISOString(),
                    bufferSeconds: BUFFER_SECONDS,
                    timeUntilExpiry: payload.exp - currentTime
                });
                return false;
            }

            console.log('✅ AuthService: Token is valid with buffer', {
                actualExpiry: payload.exp ? new Date(payload.exp * 1000).toISOString() : 'unknown',
                effectiveExpiry: new Date(effectiveExpiry * 1000).toISOString(),
                timeUntilExpiry: payload.exp - currentTime,
                bufferSeconds: BUFFER_SECONDS
            });
            return true;

        } catch (error) {
            console.warn('⚠️ AuthService: Token validation failed:', error);
            return false;
        }
    }

    static getToken(): string | null {
        const token = localStorage.getItem('accessToken');
        if (token) console.log('🎫 AuthService: Access token retrieved');
        else console.log('⚠️ AuthService: No access token available');
        return token;
    }

    static getRefreshToken(): string | null {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
            console.log('🎫 AuthService: Refresh token retrieved');
        } else {
            console.log('⚠️ AuthService: No refresh token available');
        }
        return refreshToken;
    }

    static clearTokens(): void {
        console.log('🧹 AuthService: Clearing authentication tokens...');
        const tokensToRemove = ['accessToken', 'refreshToken'];
        let removedCount = 0;
        tokensToRemove.forEach(tokenKey => {
            if (localStorage.getItem(tokenKey)) {
                localStorage.removeItem(tokenKey);
                removedCount++;
                console.log(`🗑️ AuthService: Removed ${tokenKey}`);
            }
        });
        console.log(`✅ AuthService: Cleared ${removedCount} tokens`);
    }

    static getTokenInfo(): any | null {
        const token = AuthService.getToken();
        if (!token) return null;
        try {
            const tokenParts = token.split('.');
            if (tokenParts.length !== 3) {
                console.warn('⚠️ AuthService: Invalid token format for info extraction');
                return null;
            }
            const payload = JSON.parse(atob(tokenParts[1]));
            console.log('🔍 AuthService: Token info extracted', {
                subject: payload.sub,
                issuer: payload.iss,
                expiresAt: payload.exp ? new Date(payload.exp * 1000).toISOString() : 'unknown',
                issuedAt: payload.iat ? new Date(payload.iat * 1000).toISOString() : 'unknown'
            });
            return payload;
        } catch (error) {
            console.warn('⚠️ AuthService: Failed to extract token info:', error);
            return null;
        }
    }

    static async registerClient(client: ClientType): Promise<void> {
        console.log('📝 AuthService: Registering new client...', {
            email: client.email,
            name: client.name,
            hasPassword: !!client.password,
            balance: client.balance
        });
        try {
            await apiClient.post(API_ENDPOINTS.auth.registerClient, client);
            console.log('✅ AuthService: Client registration successful', {
                clientEmail: client.email,
                clientName: client.name
            });
        } catch (error) {
            console.error('❌ AuthService: Client registration failed', {
                clientEmail: client.email, error});
            throw error;
        }
    }

    static async refreshToken(data: RefreshTokenDTO): Promise<TokenResponseDTO> {
        console.log('🔄 AuthService: Refreshing access token...', {
            email: data.email,
            role: data.role,
            hasRefreshToken: !!data.refresh_token
        });

        try {
            const baseURL = process.env.REACT_APP_API_URL || 'http://localhost:8084';
            const response = await axios.post<TokenResponseDTO>(
                `${baseURL}${API_ENDPOINTS.auth.refresh}`,
                data,
                {
                    headers: { 'Content-Type': 'application/json' },
                    timeout: 15000
                }
            );

            console.log('✅ AuthService: Token refresh successful', {
                hasAccessToken: !!response.data.access_token,
                hasRefreshToken: !!response.data.refresh_token,
                expiresIn: response.data.expires_in
            });

            if (response.data.access_token) {
                localStorage.setItem('accessToken', response.data.access_token);
                console.log('💾 AuthService: New access token stored');
            }

            if (response.data.refresh_token) {
                localStorage.setItem('refreshToken', response.data.refresh_token);
                console.log('💾 AuthService: New refresh token stored');
            }

            return response.data;

        } catch (error) {
            console.error('❌ AuthService: Token refresh failed', error);
            AuthService.clearTokens();
            throw error;
        }
    }
}