import AsyncStorage from '@react-native-async-storage/async-storage';
import { apiRequest, TOKEN_KEY } from '@/service/api';
import type {
    AuthResponse,
    ResendTokenRequest,
    SignInRequest,
    SignUpRequest,
    UserDto,
    VerifyTokenRequest,
    ForgotPasswordRequest,
    ResetPasswordRequest,
    ChangePasswordRequest,
    ConfirmPasswordRequest,
    ChangeEmailRequest,
    ConfirmEmailRequest,
} from '@/service/dtos/auth-types';

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

    // Forgot password (unauthenticated)
    async forgotPassword(data: ForgotPasswordRequest): Promise<void> {
        await apiRequest('/api/auth/forgot-password', { method: 'POST', body: data, auth: false });
    },

    async resetPassword(data: ResetPasswordRequest): Promise<void> {
        await apiRequest('/api/auth/reset-password', { method: 'PATCH', body: data, auth: false });
    },

    // Change password (authenticated)
    async changePassword(data: ChangePasswordRequest): Promise<void> {
        await apiRequest('/api/users/change-password', { method: 'POST', body: data });
    },

    async confirmPasswordChange(data: ConfirmPasswordRequest): Promise<void> {
        await apiRequest('/api/users/confirm-password', { method: 'PATCH', body: data });
    },

    // Change email (authenticated)
    async changeEmail(data: ChangeEmailRequest): Promise<void> {
        await apiRequest('/api/users/change-email', { method: 'POST', body: data });
    },

    async confirmEmailChange(data: ConfirmEmailRequest): Promise<void> {
        await apiRequest('/api/users/confirm-email', { method: 'PATCH', body: data });
    },
};