export interface UserDto {
    id: number;
    name: string;
    username: string;
    email: string | null;
    avatarUrl: string | null;
    phone: string | null;
}

export interface UpdateUserRequest {
    name: string;
    username: string;
    phone?: string | null;
}

export interface UserSearchDto {
    id: number;
    name: string;
    username: string;
    avatarUrl: string | null;
}

export interface AuthResponse {
    token: string;
    refreshToken: string;
    user: UserDto;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}

export interface SignUpRequest {
    name: string;
    username: string;
    email: string;
    password: string;
}

export interface SignInRequest {
    identifier: string;
    password: string;
}

export interface VerifyTokenRequest {
    identifier: string;
    token: string;
}

export interface ResendTokenRequest {
    identifier: string;
}

export interface ForgotPasswordRequest {
    identifier: string;
}

export interface ResetPasswordRequest {
    identifier: string;
    token: string;
    newPassword: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
}

export interface ConfirmPasswordRequest {
    token: string;
    newPassword: string;
}

export interface ChangeEmailRequest {
    newEmail: string;
}

export interface ConfirmEmailRequest {
    newEmail: string;
    token: string;
}