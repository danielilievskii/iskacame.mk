export interface UserDto {
    id: number;
    name: string;
    username: string;
    avatarUrl: string | null;
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
    email: string;
    token: string;
}

export interface ResendTokenRequest {
    email: string;
}