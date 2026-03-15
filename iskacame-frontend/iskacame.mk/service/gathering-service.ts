import { apiRequest } from './api';
import type {
    GatheringSummaryDto,
    GatheringDetailsDto,
    CreateGatheringRequest,
    UpdateGatheringRequest,
    GatheringInvitationDto,
} from './dtos/gathering-types';

export const gatheringService = {
    async getMyGatherings(): Promise<GatheringSummaryDto[]> {
        return apiRequest<GatheringSummaryDto[]>('/api/gatherings/my-gatherings');
    },

    async getGatheringDetails(id: number): Promise<GatheringDetailsDto> {
        return apiRequest<GatheringDetailsDto>(`/api/gatherings/${id}`);
    },

    async createGathering(data: CreateGatheringRequest): Promise<GatheringDetailsDto> {
        return apiRequest<GatheringDetailsDto>('/api/gatherings', {
            method: 'POST',
            body: data,
        });
    },

    async updateGathering(id: number, data: UpdateGatheringRequest): Promise<GatheringDetailsDto> {
        return apiRequest<GatheringDetailsDto>(`/api/gatherings/${id}`, {
            method: 'PATCH',
            body: data,
        });
    },

    async cancelGathering(id: number): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${id}`, { method: 'DELETE' });
    },

    async getInvitations(): Promise<GatheringInvitationDto[]> {
        return apiRequest<GatheringInvitationDto[]>('/api/gatherings/invitations');
    },

    async acceptInvitation(participationId: number): Promise<void> {
        return apiRequest<void>(`/api/gatherings/invitations/${participationId}/accept`, {
            method: 'POST',
        });
    },

    async declineInvitation(participationId: number): Promise<void> {
        return apiRequest<void>(`/api/gatherings/invitations/${participationId}/decline`, {
            method: 'POST',
        });
    },

    async leaveGathering(gatheringId: number): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${gatheringId}/leave`, { method: 'DELETE' });
    },
};