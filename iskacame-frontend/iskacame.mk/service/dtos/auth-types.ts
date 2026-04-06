export interface UserDto {
    id: number;
    name: string;
    username: string;
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
    user: UserDto;
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

// Forgot / Reset password (unauthenticated)
export interface ForgotPasswordRequest {
    identifier: string;
}

export interface ResetPasswordRequest {
    identifier: string;
    token: string;
    newPassword: string;
}

// Change password (authenticated)
export interface ChangePasswordRequest {
    currentPassword: string;
}

export interface ConfirmPasswordRequest {
    token: string;
    newPassword: string;
}

// Change email (authenticated)
export interface ChangeEmailRequest {
    newEmail: string;
}

export interface ConfirmEmailRequest {
    newEmail: string;
    token: string;
}