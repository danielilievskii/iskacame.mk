import { apiRequest } from './api';

export const deviceTokenService = {
    async register(token: string, platform?: string): Promise<void> {
        return apiRequest<void>('/api/device-tokens', {
            method: 'POST',
            body: { token, platform },
        });
    },

    async unregister(token: string): Promise<void> {
        return apiRequest<void>(`/api/device-tokens?token=${encodeURIComponent(token)}`, {
            method: 'DELETE',
        });
    },
};
