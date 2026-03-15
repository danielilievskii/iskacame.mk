import { UserDto } from "@/service/dtos/auth-types";

export type GatheringStatus = 'DRAFT' | 'OPEN' | 'FINALIZED' | 'CANCELLED';
export type ParticipationStatus = 'INVITED' | 'JOINED' | 'DECLINED' | 'LEFT' | 'REMOVED';
export type PlaceType = 'CAFE' | 'RESTAURANT' | 'PARK' | 'OTHER';
export type PlaceLevel = 'FREE' | 'CHEAP' | 'MODERATE' | 'EXPENSIVE' | 'LUXURY';

export interface PlaceDto {
    id: number;
    name: string;
    address: string | null;
    longitude: number | null;
    latitude: number | null;
    type: PlaceType;
    priceLevel: PlaceLevel;
    link: string | null;
}

export interface ParticipantDto {
    user: UserDto;
    participationStatus: ParticipationStatus;
}

export interface GatheringSummaryDto {
    id: number;
    creator: UserDto;
    title: string;
    status: GatheringStatus;
    createdAt: string;
}

export interface GatheringDetailsDto {
    id: number;
    creatorId: number;
    title: string;
    description: string | null;
    startDate: string;
    endDate: string;
    status: GatheringStatus;
    finalizedTime: string | null;
    finalizedPlace: PlaceDto | null;
    participants: ParticipantDto[] | null;
    suggestedPlaces: PlaceDto[] | null;
}

export interface GatheringInvitationDto {
    id: number;
    gatheringCreator: UserDto;
    gatheringTitle: string;
    createdAt: string;
}

export interface CreateGatheringRequest {
    title: string;
    description?: string;
    startDate: string;
    endDate: string;
    participantIds: number[];
}

export interface UpdateGatheringRequest {
    title?: string;
    description?: string;
    startDate?: string;
    endDate?: string;
}