import AsyncStorage from '@react-native-async-storage/async-storage';
import { apiRequest, TOKEN_KEY } from './api';
import type { UserDto, UpdateUserRequest } from './dtos/auth-types';

const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8090';

export const userService = {
    async updateProfile(data: UpdateUserRequest): Promise<UserDto> {
        return apiRequest<UserDto>('/api/users/me/update', {
            method: 'PATCH',
            body: data,
        });
    },

    async uploadAvatar(imageUri: string, mimeType: string = 'image/jpeg'): Promise<string> {
        const token = await AsyncStorage.getItem(TOKEN_KEY);

        const formData = new FormData();
        formData.append('file', {
            uri: imageUri,
            type: mimeType,
            name: 'avatar.jpg',
        } as any);

        const response = await fetch(`${BASE_URL}/api/users/me/avatar`, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${token}`,
            },
            body: formData,
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Failed to upload avatar');
        }

        return response.text();
    },

    async deleteAvatar(): Promise<void> {
        const token = await AsyncStorage.getItem(TOKEN_KEY);

        const response = await fetch(`${BASE_URL}/api/users/me/avatar`, {
            method: 'DELETE',
            headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Failed to delete avatar');
        }
    },
};