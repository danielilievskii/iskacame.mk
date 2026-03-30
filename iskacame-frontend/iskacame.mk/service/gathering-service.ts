import { apiRequest } from './api';
import type {
    GatheringSummaryDto,
    GatheringDetailsDto,
    CreateGatheringRequest,
    UpdateGatheringRequest,
    GatheringInvitationDto,
    GatheringResponseOptionsDto,
    SubmitGatheringResponseRequest,
    ActivityDto,
    DebtDto,
    CreatePaymentRequest,
    CreateExpenseRequest,
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

    async acceptInvitation(participationId: number): Promise<GatheringDetailsDto> {
        return apiRequest<GatheringDetailsDto>(`/api/gatherings/invitations/${participationId}/accept`, {
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

    async getResponseOptions(gatheringId: number): Promise<GatheringResponseOptionsDto> {
        return apiRequest<GatheringResponseOptionsDto>(`/api/gatherings/${gatheringId}/responses/options`);
    },

    async submitResponse(gatheringId: number, data: SubmitGatheringResponseRequest): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${gatheringId}/responses`, {
            method: 'POST',
            body: data,
        });
    },

    async updateResponse(gatheringId: number, data: SubmitGatheringResponseRequest): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${gatheringId}/responses`, {
            method: 'PUT',
            body: data,
        });
    },

    async getActivities(gatheringId: number): Promise<ActivityDto[]> {
        return apiRequest<ActivityDto[]>(`/api/gatherings/${gatheringId}/activities`);
    },

    async getDebts(gatheringId: number): Promise<DebtDto[]> {
        return apiRequest<DebtDto[]>(`/api/gatherings/${gatheringId}/debts`);
    },

    async createPayment(gatheringId: number, data: CreatePaymentRequest): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${gatheringId}/payments`, {
            method: 'POST',
            body: data,
        });
    },

    async createExpense(gatheringId: number, data: CreateExpenseRequest): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${gatheringId}/expenses`, {
            method: 'POST',
            body: data,
        });
    },

    async deletePayment(gatheringId: number, paymentId: number): Promise<void> {
        return apiRequest<void>(`/api/gatherings/${gatheringId}/payments/${paymentId}`, {
            method: 'DELETE',
        });
    },
};
