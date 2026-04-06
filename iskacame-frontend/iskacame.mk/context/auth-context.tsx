import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { authService } from '@/service/auth-service';
import { deviceTokenService } from '@/service/device-token-service';
import { UserDto } from "@/service/dtos/auth-types";
import AsyncStorage from '@react-native-async-storage/async-storage';

interface AuthState {
    user: UserDto | null;
    isLoading: boolean;
    isAuthenticated: boolean;
}

interface AuthContextValue extends AuthState {
    signIn: (identifier: string, password: string) => Promise<void>;
    signOut: () => Promise<void>;
    refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [state, setState] = useState<AuthState>({
        user: null,
        isLoading: true,
        isAuthenticated: false,
    });

    const refreshUser = useCallback(async () => {
        try {
            const token = await authService.getStoredToken();
            if (!token) {
                setState({ user: null, isLoading: false, isAuthenticated: false });
                return;
            }
            const user = await authService.getMe();
            setState({ user, isLoading: false, isAuthenticated: true });
        } catch {
            setState({ user: null, isLoading: false, isAuthenticated: false });
        }
    }, []);

    useEffect(() => {
        refreshUser();
    }, [refreshUser]);

    const signIn = useCallback(async (identifier: string, password: string) => {
        const response = await authService.signIn({ identifier, password });
        setState({ user: response.user, isLoading: false, isAuthenticated: true });
    }, []);

    const signOut = useCallback(async () => {
        try {
            const pushToken = await AsyncStorage.getItem('push_token');
            if (pushToken) {
                await deviceTokenService.unregister(pushToken);
                await AsyncStorage.removeItem('push_token');
            }
        } catch { }
        await authService.signOut();
        setState({ user: null, isLoading: false, isAuthenticated: false });
    }, []);

    return (
        <AuthContext.Provider value={{ ...state, signIn, signOut, refreshUser }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
}