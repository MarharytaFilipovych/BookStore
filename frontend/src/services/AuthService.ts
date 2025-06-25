import { apiClient } from '../config/ApiClient';
import {
    LoginRequest,
    TokenResponseDTO,
    ForgotPasswordDTO,
    ResetPasswordDTO,
    RefreshTokenDTO,
    LogoutDTO,
    API_ENDPOINTS
} from '../types';

export class AuthService {

    static async login(credentials: LoginRequest): Promise<TokenResponseDTO> {
        console.log('🔐 AuthService: Starting login process...', {
            email: credentials.email,
            role: credentials.role,
            hasPassword: !!credentials.password
        });

        try {
            const response = await apiClient.post<TokenResponseDTO>(
                API_ENDPOINTS.auth.login,
                credentials
            );

            console.log('✅ AuthService: Login successful', {
                hasAccessToken: !!response.data.accessToken,
                hasRefreshToken: !!response.data.refreshToken,
                expiresIn: response.data.expiresIn,
                role: credentials.role
            });

            if (response.data.accessToken) {
                localStorage.setItem('accessToken', response.data.accessToken);
                console.log('💾 AuthService: Access token stored');
            }

            if (response.data.refreshToken) {
                localStorage.setItem('refreshToken', response.data.refreshToken);
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

    static async forgotPassword(data: ForgotPasswordDTO): Promise<string> {
        console.log('🔑 AuthService: Initiating password reset...', {
            email: data.email,
            role: data.role
        });

        try {
            const response = await apiClient.post<string>(API_ENDPOINTS.auth.forgotPassword, data);
            console.log('✅ AuthService: Password reset email sent successfully');
            return response.data; // Returns the reset code UUID
        } catch (error) {
            console.error('❌ AuthService: Password reset request failed', error);
            throw error;
        }
    }

    static async resetPassword(data: ResetPasswordDTO): Promise<void> {
        console.log('🔐 AuthService: Resetting password...', {
            email: data.email,
            hasPassword: !!data.password,
            hasResetCode: !!data.resetCode
        });

        try {
            await apiClient.post(API_ENDPOINTS.auth.changePassword, data);
            console.log('✅ AuthService: Password reset successful');
        } catch (error) {
            console.error('❌ AuthService: Password reset failed', error);
            throw error;
        }
    }

    static async refreshToken(data: RefreshTokenDTO): Promise<TokenResponseDTO> {
        console.log('🔄 AuthService: Refreshing access token...', {
            email: data.email,
            role: data.role,
            hasRefreshToken: !!data.refreshToken
        });

        try {
            const response = await apiClient.post<TokenResponseDTO>(
                API_ENDPOINTS.auth.refresh,
                data
            );

            console.log('✅ AuthService: Token refresh successful', {
                hasAccessToken: !!response.data.accessToken,
                hasRefreshToken: !!response.data.refreshToken,
                expiresIn: response.data.expiresIn
            });

            if (response.data.accessToken) {
                localStorage.setItem('accessToken', response.data.accessToken);
                console.log('💾 AuthService: New access token stored');
            }

            if (response.data.refreshToken) {
                localStorage.setItem('refreshToken', response.data.refreshToken);
                console.log('💾 AuthService: New refresh token stored');
            }

            return response.data;

        } catch (error) {
            console.error('❌ AuthService: Token refresh failed', error);
            AuthService.clearTokens();
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

        if (token) {
            console.log('🎫 AuthService: Access token retrieved');
        } else {
            console.log('⚠️ AuthService: No access token available');
        }

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

        if (!token) {
            return null;
        }

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

    static debugAuthState(): void {
        console.log('🔍 AuthService: Current authentication state:', {
            hasAccessToken: !!AuthService.getToken(),
            hasRefreshToken: !!AuthService.getRefreshToken(),
            isAuthenticated: AuthService.isAuthenticated(),
            tokenInfo: AuthService.getTokenInfo(),
        });
    }
}

if (process.env.NODE_ENV === 'development') {
    (window as any).debugAuth = () => AuthService.debugAuthState();
}