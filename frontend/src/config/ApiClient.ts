import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

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
                console.log(`🚀 Making ${config.method?.toUpperCase()} request to: ${config.baseURL}${config.url}`);
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
                if (config.data) console.log('📦 Request data:', config.data);
                if (config.params) console.log('🔍 Request params:', config.params);
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
            (error) => {
                console.error(`❌ API error from ${error.config?.url}:`, {
                    status: error.response?.status,
                    statusText: error.response?.statusText,
                    message: error.response?.data?.message || error.message
                });
                return Promise.reject(error);
            }
        );

        console.log('✅ Interceptors configured successfully');
    }

    async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`📖 GET request initiated: ${url}`);
        if (config?.params) console.log('🔍 GET params:', config.params);
        const response = await this.client.get<T>(url, config);
        console.log(`📖 GET request completed: ${url} (${response.status})`);
        return response;
    }

    async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`📝 POST request initiated: ${url}`);
        if (data) console.log('📦 POST data:', typeof data === 'object' ? JSON.stringify(data, null, 2) : data);
        if (config?.headers) console.log('📋 Custom headers:', config.headers);
        const response = await this.client.post<T>(url, data, config);
        console.log(`📝 POST request completed: ${url} (${response.status})`);
        return response;
    }

    async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`✏️ PUT request initiated: ${url}`);
        if (data) console.log('📦 PUT data:', typeof data === 'object' ? JSON.stringify(data, null, 2) : data);
        if (config?.params) console.log('🔍 PUT params:', config.params);
        if (config?.headers) console.log('📋 Custom headers:', config.headers);
        const response = await this.client.put<T>(url, data, config);
        console.log(`✏️ PUT request completed: ${url} (${response.status})`);
        return response;
    }

    async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
        console.log(`🗑️ DELETE request initiated: ${url}`);
        if (config?.params) console.log('🔍 DELETE params:', config.params);
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
}
export const apiClient = new ApiClient();
