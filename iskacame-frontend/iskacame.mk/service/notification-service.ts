import { apiRequest } from './api';
import type { NotificationDto } from './dtos/gathering-types';

export const notificationService = {
    async getNotifications(): Promise<NotificationDto[]> {
        return apiRequest<NotificationDto[]>('/api/notifications');
    },

    async markAsRead(id: number): Promise<void> {
        return apiRequest<void>(`/api/notifications/${id}/read`, { method: 'POST' });
    },

    async markAllAsRead(): Promise<void> {
        return apiRequest<void>('/api/notifications/read-all', { method: 'POST' });
    },

    async getUnreadCount(): Promise<number> {
        return apiRequest<number>('/api/notifications/unread-count');
    },
};
