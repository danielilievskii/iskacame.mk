import AsyncStorage from '@react-native-async-storage/async-storage';

const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8090';

export const TOKEN_KEY = 'auth_token';

async function getToken(): Promise<string | null> {
    return AsyncStorage.getItem(TOKEN_KEY);
}

type RequestOptions = {
    method?: string;
    body?: unknown;
    auth?: boolean;
};

export async function apiRequest<T>(
    path: string,
    { method = 'GET', body, auth = true }: RequestOptions = {}
): Promise<T> {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };

    if (auth) {
        const token = await getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }

    const response = await fetch(`${BASE_URL}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
        let errorMessage = `Request failed: ${response.status}`;
        try {
            const errorBody = await response.text();
            errorMessage = errorBody || errorMessage;
        } catch {
            // keep default message
        }
        throw new Error(errorMessage);
    }

    const text = await response.text();
    if (!text) return undefined as T;

    try {
        return JSON.parse(text) as T;
    } catch {
        return text as unknown as T;
    }
}