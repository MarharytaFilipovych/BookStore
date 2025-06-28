import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import {API_ENDPOINTS, TokenResponseDTO, RefreshTokenDTO, Role} from '../types';

class ApiClient {
    private readonly client: AxiosInstance;
    private isRefreshing = false; // ✅ ADDED: Prevent multiple refresh attempts
    private failedQueue: Array<{
        resolve: (value: any) => void;
        reject: (error: any) => void;
        config: any;
    }> = []; // ✅ ADDED: Queue for failed requests during refresh

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

    // ✅ ADDED: Process queued requests after successful refresh
    private processQueue(error: any, token: string | null = null) {
        this.failedQueue.forEach(({ resolve, reject, config }) => {
            if (error) {
                reject(error);
            } else {
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                resolve(this.client(config));
            }
        });

        this.failedQueue = [];
    }

    private setupInterceptors() {
        console.log('⚙️ Setting up request/response interceptors...');

        this.client.interceptors.request.use(
            (config) => {
                console.log(`🚀 Making ${config.method?.toUpperCase()} request to: ${config.baseURL}${config.url}`);

                // Only set token if Authorization header isn't already present
                if (!config.headers.Authorization) {
                    const token = localStorage.getItem('accessToken');
                    if (token) {
                        config.headers.Authorization = `Bearer ${token}`;
                        console.log('🔑 JWT token attached from localStorage');
                    } else {
                        console.log('⚠️ No JWT token found in localStorage');
                    }
                } else {
                    console.log('🔑 Using existing Authorization header');
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

                // Handle 401 errors (token expired)
                if (error.response?.status === 401 && !originalRequest._retry) {
                    console.log('🔄 Token expired (401), attempting refresh...');

                    // ✅ FIXED: Handle concurrent requests during refresh
                    if (this.isRefreshing) {
                        console.log('⏳ Refresh already in progress, queuing request...');
                        return new Promise((resolve, reject) => {
                            this.failedQueue.push({ resolve, reject, config: originalRequest });
                        });
                    }

                    originalRequest._retry = true;
                    this.isRefreshing = true;

                    try {
                        const refreshToken = localStorage.getItem('refreshToken');
                        const savedUser = localStorage.getItem('user');
                        const savedRole = localStorage.getItem('role');

                        if (!refreshToken || !savedUser || !savedRole) {
                            console.log('❌ Missing refresh data, redirecting to login');
                            throw new Error('Missing authentication data');
                        }

                        console.log('🔄 Calling refresh token endpoint...');

                        // ✅ FIXED: Parse user data properly
                        let email: string;
                        try {
                            const userData = JSON.parse(savedUser);
                            email = userData.email;
                        } catch (parseError) {
                            console.error('❌ Failed to parse user data:', parseError);
                            throw new Error('Invalid user data');
                        }

                        // ✅ FIXED: Use correct refresh token payload structure
                        const refreshData: RefreshTokenDTO = {
                            refresh_token: refreshToken, // ✅ FIXED: Correct field name
                            email: email,
                            role: savedRole as Role
                        };

                        console.log('📤 Sending refresh token request:', {
                            endpoint: API_ENDPOINTS.auth.refresh,
                            email,
                            role: savedRole,
                            hasRefreshToken: !!refreshToken
                        });

                        // ✅ FIXED: Use a fresh axios instance to avoid interceptor loop
                        const response = await axios.post<TokenResponseDTO>(
                            `${this.client.defaults.baseURL}${API_ENDPOINTS.auth.refresh}`,
                            refreshData,
                            {
                                headers: {
                                    'Content-Type': 'application/json'
                                }
                            }
                        );

                        const newAccessToken = response.data.access_token;

                        // Store new tokens
                        localStorage.setItem('accessToken', newAccessToken);
                        if (response.data.refresh_token) {
                            localStorage.setItem('refreshToken', response.data.refresh_token);
                            console.log('🔄 Both access and refresh tokens updated');
                        } else {
                            console.log('🔄 Only access token updated');
                        }

                        console.log('✅ Token refresh successful, processing queued requests...');

                        // ✅ FIXED: Update original request and process queue
                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        this.processQueue(null, newAccessToken);

                        return this.client(originalRequest);

                    } catch (refreshError) {
                        console.error('❌ Token refresh failed:', refreshError);

                        this.clearAuthData();
                        this.processQueue(refreshError, null);

                        console.log('🚪 Redirecting to welcome page...');
                        window.location.href = '/';

                        return Promise.reject(refreshError);
                    } finally {
                        this.isRefreshing = false; // ✅ FIXED: Reset refresh flag
                    }
                }

                return Promise.reject(error);
            }
        );

        console.log('✅ Interceptors configured successfully');
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
            hasUser: !!localStorage.getItem('user'),
            hasRole: !!localStorage.getItem('role'),
            baseURL: this.client.defaults.baseURL,
            isRefreshing: this.isRefreshing,
            queuedRequests: this.failedQueue.length
        });
    }
}

export const apiClient = new ApiClient();

// ✅ ADDED: Debug helper
(window as any).debugApi = () => apiClient.debugAuthState();