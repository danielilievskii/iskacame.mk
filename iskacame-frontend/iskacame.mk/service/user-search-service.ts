import { apiRequest } from './api';

export interface UserSearchDto {
    id: number;
    name: string;
    username: string;
    avatarUrl: string | null;
}

export const userSearchService = {
    async searchUsers(identifier: string): Promise<UserSearchDto[]> {
        if (!identifier.trim()) return [];
        return apiRequest<UserSearchDto[]>(
            `/api/users/${encodeURIComponent(identifier.trim())}`
        );
    },
};