import { UserDto } from '@/service/dtos/auth-types';

export interface ChatMessageDto {
    id: number;
    sender: UserDto;
    content: string;
    sentAt: string;
    latestSeenBy: number[];
}

export interface SendMessageRequest {
    content: string;
}

export interface ChatMessageSentNotification {
    chatRoomId: number;
    type: 'MESSAGE_SENT';
    message: ChatMessageDto;
}

export interface ChatMessageDeletedNotification {
    chatRoomId: number;
    type: 'MESSAGE_DELETED';
    messageId: number;
}

export type ChatNotification = ChatMessageSentNotification | ChatMessageDeletedNotification;

export interface MessageReceiptNotification {
    chatRoomId: number;
    messageId: number;
    recipientId: number;
    status: string;
}

export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}
