import { UserDto } from "@/service/dtos/auth-types";

export type GatheringStatus = 'DRAFT' | 'OPEN' | 'FINALIZED' | 'CANCELLED';
export type ParticipationStatus = 'INVITED' | 'JOINED' | 'DECLINED' | 'LEFT' | 'REMOVED';
export type PlaceType = 'CAFE' | 'RESTAURANT' | 'PARK' | 'OTHER';
export type PlaceLevel = 'FREE' | 'CHEAP' | 'MODERATE' | 'EXPENSIVE' | 'LUXURY';
export type GatheringType = 'CASUAL' | 'SPORT' | 'ELEGANT' | 'PARTY' | 'CULTURAL' | 'OUTDOOR' | 'TRAVEL' | 'FOOD' | 'GAME' | 'MOVIE';
export type TimeSlot = 'MORNING' | 'NOON' | 'AFTERNOON' | 'EVENING';
export type ActivityType = 'EXPENSE' | 'PAYMENT';

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
    startDate: string;
    endDate: string;
    finalizedTime: string | null;
    chatRoomId: number;
    unseenMessagesCount: number | null;
}

export interface GatheringDetailsDto {
    id: number;
    creatorId: number;
    title: string;
    description: string | null;
    location: string | null;
    startDate: string;
    endDate: string;
    status: GatheringStatus;
    finalizedTime: string | null;
    finalizedPlace: PlaceDto | null;
    participants: ParticipantDto[] | null;
    suggestedPlaces: PlaceDto[] | null;
    chatRoomId: number;
    hasSubmittedResponse: boolean;
    activePoll: PlacePollDto | null;
    unseenMessagesCount: number;
}

export interface GatheringInvitationDto {
    id: number;
    gatheringId: number;
    gatheringCreator: UserDto;
    gatheringTitle: string;
    createdAt: string;
}

export interface CreateGatheringRequest {
    title: string;
    description?: string;
    location?: string;
    startDate: string;
    endDate: string;
    participantIds: number[];
}

export interface UpdateGatheringRequest {
    title?: string;
    description?: string;
    location?: string;
    startDate?: string;
    endDate?: string;
}

export interface GatheringTimeSlotOptionDto {
    id: number;
    date: string;
    slot: TimeSlot;
}

export interface GatheringResponseOptionsDto {
    types: GatheringType[];
    timeSlots: GatheringTimeSlotOptionDto[];
}

export interface SubmitGatheringResponseRequest {
    types: GatheringType[];
    timeSlotIds: number[];
}

export interface MyGatheringResponseDto {
    types: GatheringType[];
    timeSlotIds: number[];
}

export interface ExpenseSplitDto {
    userId: number;
    amountOwed: number;
    isOwnShare: boolean;
}

export interface ExpenseActivityDto {
    id: number;
    type: 'EXPENSE';
    description: string | null;
    totalAmount: number;
    paidByUserId: number;
    splits: ExpenseSplitDto[];
    createdAt: string;
}

export interface PaymentActivityDto {
    id: number;
    type: 'PAYMENT';
    fromUserId: number;
    toUserId: number;
    amount: number;
    createdAt: string;
}

export type ActivityDto = ExpenseActivityDto | PaymentActivityDto;

export interface DebtDto {
    fromUserId: number;
    toUserId: number;
    amount: number;
}

export interface CreatePaymentRequest {
    toUserId: number;
    amount: number;
}

export interface CreateSplitRequest {
    userId: number;
    amountOwed: number;
}

export interface CreateExpenseRequest {
    paidByUserId: number;
    totalAmount: number;
    description: string;
    splits: CreateSplitRequest[];
}

// Poll types
export type PollStatus = 'ACTIVE' | 'ENDED';

export interface PlacePollOptionDto {
    place: PlaceDto;
    voteCount: number;
}

export interface PlacePollDto {
    id: number;
    status: PollStatus;
    endsAt: string;
    createdAt: string;
    places: PlacePollOptionDto[];
    myVotedPlaceIds: number[];
}

// Notification types
export type NotificationType = 'GATHERING_INVITE' | 'VOTE_STARTED' | 'VOTE_ENDED' | 'GATHERING_CANCELLED';

export interface NotificationDto {
    id: number;
    type: NotificationType;
    title: string;
    body: string;
    gatheringId: number | null;
    read: boolean;
    createdAt: string;
}
