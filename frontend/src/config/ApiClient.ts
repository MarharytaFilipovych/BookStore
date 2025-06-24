import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { TokenResponseDTO } from '../types';

class ApiClient {
    private readonly client: AxiosInstance;

    constructor() {
        console.log('🏗️ Initializing ApiClient...');

        this.client = axios.create({
            baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8084',
            timeout: 15000,
            headers: {
                'Content-Type': 'application/json',
            },
        });

        console.log(`🌐 ApiClient configured with baseURL: ${this.client.defaults.baseURL}`);
        this.setupInterceptors();
    }

    private setupInterceptors() {
        console.log('⚙️ Setting up request/response interceptors...');

        this.client.interceptors.request.use(
            (config) => {
                const token = localStorage.getItem('accessToken');

                console.log(`🚀 Making ${config.method?.toUpperCase()} request to: ${config.baseURL}${config.url}`);

                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                    console.log('🔑 JWT token attached to request');
                } else {
                    console.log('⚠️ No JWT token found in localStorage');
                }

                if (config.data) {
                    console.log('📦 Request data:', config.data);
                }

                if (config.params) {
                    console.log('🔍 Request params:', config.params);
                }

                return config;
            },
            (error) => {
                console.error('❌ Request interceptor error:', error);
                return Promise.reject(error);
            }
        );

        this.client.interceptors.response.use(
            (response) => {
                console.log(`✅ API response from ${response.config.url}:`, {
                    status: response.status,
                    statusText: response.statusText,
                    dataSize: JSON.stringify(response.data).length + ' bytes'
                });

                return response;
            },
            async (error) => {
                const originalRequest = error.config;

                console.error(`❌ API error from ${originalRequest?.url}:`, {
                    status: error.response?.status,
                    statusText: error.response?.statusText,
                    message: error.response?.data?.message || error.message
                });

                if (error.response?.status === 401 && !originalRequest._retry) {
                    console.log('🔄 Token expired (401), attempting refresh...');

                    originalRequest._retry = true;

                    try {
                        const refreshToken = localStorage.getItem('refreshToken');
                        if (!refreshToken) {
                            console.log('❌ No refresh token found, redirecting to login');
                            this.clearAuthData();
                            window.location.href = '/welcome';
                            return Promise.reject(error);
                        }

                        console.log('🔄 Calling refresh token endpoint...');
                        const response = await this.refreshToken(refreshToken);
                        const newAccessToken = response.data.accessToken;

                        localStorage.setItem('accessToken', newAccessToken);
                        if (response.data.refreshToken) {
                            localStorage.setItem('refreshToken', response.data.refreshToken);
                            console.log('🔄 Both access and refresh tokens updated');
                        } else {
                            console.log('🔄 Only access token updated');
                        }

                        console.log('✅ Token refresh successful, retrying original request...');

                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        return this.client(originalRequest);

                    } catch (refreshError) {
                        console.error('❌ Token refresh failed:', refreshError);

                        this.clearAuthData();
                        console.log('🚪 Redirecting to welcome page...');
                        window.location.href = '/welcome';
                        return Promise.reject(refreshError);
                    }
                }

                return Promise.reject(error);
            }
        );

        console.log('✅ Interceptors configured successfully');
    }

    private async refreshToken(refreshToken: string) {
        console.log('🔄 Preparing refresh token request...');

        const savedUser = localStorage.getItem('user');
        const savedRole = localStorage.getItem('role');

        console.log('📋 Checking stored user data for refresh...', {
            hasUser: !!savedUser,
            hasRole: !!savedRole
        });

        if (!savedUser || !savedRole) {
            console.error('❌ Missing user data for token refresh');
            throw new Error('Missing user data for token refresh');
        }

        let email: string;
        try {
            const userData = JSON.parse(savedUser);
            email = userData.email;
            console.log(`👤 Using email for refresh: ${email}`);
        } catch (parseError) {
            console.error('❌ Failed to parse user data:', parseError);
            throw new Error('Invalid user data for token refresh');
        }

        const refreshData = {
            refreshToken,
            email,
            role: savedRole
        };

        console.log('📤 Sending refresh token request:', {
            endpoint: '/auth/refresh',
            email,
            role: savedRole,
            hasRefreshToken: !!refreshToken
        });

        const response = await axios.post<TokenResponseDTO>(
            `${this.client.defaults.baseURL}/auth/refresh`,
            refreshData
        );

        console.log('✅ Refresh token response received:', {
            hasAccessToken: !!response.data.accessToken,
            hasRefreshToken: !!response.data.refreshToken,
            expiresIn: response.data.expiresIn
        });

        return response;
    }

    private clearAuthData() {
        console.log('🧹 Clearing authentication data...');

        const keysToRemove = [
            'accessToken',
            'refreshToken',
            'user',
            'role',
            'basket'
        ];

        keysToRemove.forEach(key => {
            if (localStorage.getItem(key)) {
                localStorage.removeItem(key);
                console.log(`🗑️ Removed ${key} from localStorage`);
            }
        });

        console.log('✅ Authentication data cleared');
    }

    async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`📖 GET request initiated: ${url}`);

        if (config?.params) {
            console.log('🔍 GET params:', config.params);
        }

        const response = await this.client.get<T>(url, config);

        console.log(`📖 GET request completed: ${url} (${response.status})`);
        return response;
    }

    async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`📝 POST request initiated: ${url}`);

        if (data) {
            console.log('📦 POST data:', typeof data === 'object' ? JSON.stringify(data, null, 2) : data);
        }

        if (config?.headers) {
            console.log('📋 Custom headers:', config.headers);
        }

        const response = await this.client.post<T>(url, data, config);

        console.log(`📝 POST request completed: ${url} (${response.status})`);
        return response;
    }

    async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`✏️ PUT request initiated: ${url}`);

        if (data) {
            console.log('📦 PUT data:', typeof data === 'object' ? JSON.stringify(data, null, 2) : data);
        }

        if (config?.params) {
            console.log('🔍 PUT params:', config.params);
        }

        if (config?.headers) {
            console.log('📋 Custom headers:', config.headers);
        }

        const response = await this.client.put<T>(url, data, config);

        console.log(`✏️ PUT request completed: ${url} (${response.status})`);
        return response;
    }

    async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`🗑️ DELETE request initiated: ${url}`);

        if (config?.params) {
            console.log('🔍 DELETE params:', config.params);
        }

        const response = await this.client.delete<T>(url, config);

        console.log(`🗑️ DELETE request completed: ${url} (${response.status})`);
        return response;
    }

    getBaseURL(): string {
        return this.client.defaults.baseURL || '';
    }

    setBaseURL(baseURL: string): void {
        console.log(`🌐 Updating base URL from ${this.client.defaults.baseURL} to ${baseURL}`);
        this.client.defaults.baseURL = baseURL;
    }

    setDefaultHeader(key: string, value: string): void {
        console.log(`📋 Setting default header: ${key} = ${value}`);
        this.client.defaults.headers.common[key] = value;
    }

    debugAuthState(): void {
        console.log('🔍 Current authentication state:', {
            hasAccessToken: !!localStorage.getItem('accessToken'),
            hasRefreshToken: !!localStorage.getItem('refreshToken'),
            hasUser: !!localStorage.getItem('bookstore_user'),
            hasRole: !!localStorage.getItem('bookstore_role'),
            baseURL: this.client.defaults.baseURL
        });
    }
}

export const apiClient = new ApiClient();

(window as any).debugApi = () => apiClient.debugAuthState();