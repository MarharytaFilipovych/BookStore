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

            if (response.data.access_token) {
                localStorage.setItem('accessToken', response.data.access_token);
                console.log('💾 AuthService: Access token stored');
            }

            if (response.data.refresh_token) {
                localStorage.setItem('refreshToken', response.data.refresh_token);
                console.log('💾 AuthService: Refresh token stored');
            }
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

            if (payload.exp && payload.exp < currentTime) {
                console.warn('⚠️ AuthService: Token has expired', {
                    expiredAt: payload.exp ? new Date(payload.exp * 1000).toISOString() : 'unknown'
                });
                return false;
            }

            console.log('✅ AuthService: Token is valid', {
                expiresAt: payload.exp ? new Date(payload.exp * 1000).toISOString() : 'unknown'
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
}
