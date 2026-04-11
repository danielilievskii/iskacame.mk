import AsyncStorage from '@react-native-async-storage/async-storage';

const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8090';

export const TOKEN_KEY = 'auth_token';
export const REFRESH_TOKEN_KEY = 'refresh_token';

async function getToken(): Promise<string | null> {
    return AsyncStorage.getItem(TOKEN_KEY);
}

async function getRefreshToken(): Promise<string | null> {
    return AsyncStorage.getItem(REFRESH_TOKEN_KEY);
}

export async function setTokens(token: string, refreshToken: string): Promise<void> {
    await AsyncStorage.multiSet([
        [TOKEN_KEY, token],
        [REFRESH_TOKEN_KEY, refreshToken],
    ]);
}

export async function clearTokens(): Promise<void> {
    await AsyncStorage.multiRemove([TOKEN_KEY, REFRESH_TOKEN_KEY]);
}

type RequestOptions = {
    method?: string;
    body?: unknown;
    auth?: boolean;
};

type AuthRefreshResponse = {
    token: string;
    refreshToken: string;
};

let refreshPromise: Promise<string | null> | null = null;
let onAuthFailure: (() => void) | null = null;

export function setOnAuthFailure(handler: (() => void) | null) {
    onAuthFailure = handler;
}

async function performTokenRefresh(): Promise<string | null> {
    const currentRefresh = await getRefreshToken();
    if (!currentRefresh) return null;

    try {
        const response = await fetch(`${BASE_URL}/api/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: currentRefresh }),
        });

        if (!response.ok) return null;

        const data = (await response.json()) as AuthRefreshResponse;
        if (!data?.token || !data?.refreshToken) return null;

        await setTokens(data.token, data.refreshToken);
        return data.token;
    } catch {
        return null;
    }
}

async function refreshAccessToken(): Promise<string | null> {
    if (!refreshPromise) {
        refreshPromise = performTokenRefresh().finally(() => {
            refreshPromise = null;
        });
    }
    return refreshPromise;
}

async function doFetch(
    path: string,
    method: string,
    body: unknown,
    auth: boolean,
    token: string | null
): Promise<Response> {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };
    if (auth && token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return fetch(`${BASE_URL}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });
}

export async function apiRequest<T>(
    path: string,
    { method = 'GET', body, auth = true }: RequestOptions = {}
): Promise<T> {
    const token = auth ? await getToken() : null;
    let response = await doFetch(path, method, body, auth, token);

    if (response.status === 401 && auth) {
        const newToken = await refreshAccessToken();
        if (newToken) {
            response = await doFetch(path, method, body, auth, newToken);
        } else {
            await clearTokens();
            onAuthFailure?.();
        }
    }

    if (!response.ok) {
        const text = await response.text();
        const error = new Error(text || `Request failed: ${response.status}`);
        (error as any).status = response.status;
        throw error;
    }
    const text = await response.text();
    if (!text) return undefined as T;

    try {
        return JSON.parse(text) as T;
    } catch {
        return text as unknown as T;
    }
}

async function doUploadFetch(path: string, formData: FormData, token: string | null): Promise<Response> {
    const headers: Record<string, string> = {};
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return fetch(`${BASE_URL}${path}`, {
        method: 'POST',
        headers,
        body: formData,
    });
}

export async function apiUpload<T>(
    path: string,
    formData: FormData
): Promise<T> {
    const token = await getToken();
    let response = await doUploadFetch(path, formData, token);

    if (response.status === 401) {
        const newToken = await refreshAccessToken();
        if (newToken) {
            response = await doUploadFetch(path, formData, newToken);
        } else {
            await clearTokens();
            onAuthFailure?.();
        }
    }

    if (!response.ok) {
        const text = await response.text();
        const error = new Error(text || `Upload failed: ${response.status}`);
        (error as any).status = response.status;
        throw error;
    }
    const text = await response.text();
    if (!text) return undefined as T;
    return JSON.parse(text) as T;
}