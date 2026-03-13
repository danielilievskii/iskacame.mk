import AsyncStorage from '@react-native-async-storage/async-storage';
import { apiRequest, TOKEN_KEY } from '@/lib/api';

export interface UserDto {
    id: number;
    name: string;
    username: string;
    avatarUrl: string | null;
}

export interface AuthResponse {
    token: string;
    user: UserDto;
}

export interface SignUpRequest {
    name: string;
    username: string;
    email: string;
    password: string;
}

export interface SignInRequest {
    email: string;
    password: string;
}

export interface VerifyTokenRequest {
    email: string;
    token: string;
}

export interface ResendTokenRequest {
    email: string;
}

export const authService = {
    async signUp(data: SignUpRequest): Promise<void> {
        await apiRequest('/api/auth/signUp', { method: 'POST', body: data, auth: false });
    },

    async signIn(data: SignInRequest): Promise<AuthResponse> {
        const response = await apiRequest<AuthResponse>('/api/auth/signIn', {
            method: 'POST',
            body: data,
            auth: false,
        });
        await AsyncStorage.setItem(TOKEN_KEY, response.token);
        return response;
    },

    async verifyEmail(data: VerifyTokenRequest): Promise<void> {
        await apiRequest('/api/auth/verify-email', { method: 'POST', body: data, auth: false });
    },

    async resendCode(data: ResendTokenRequest): Promise<void> {
        await apiRequest('/api/auth/resend-verification-code', { method: 'POST', body: data, auth: false });
    },

    async signOut(): Promise<void> {
        await AsyncStorage.removeItem(TOKEN_KEY);
    },

    async getMe(): Promise<UserDto> {
        return apiRequest<UserDto>('/api/users/me');
    },

    async getStoredToken(): Promise<string | null> {
        return AsyncStorage.getItem(TOKEN_KEY);
    },
};