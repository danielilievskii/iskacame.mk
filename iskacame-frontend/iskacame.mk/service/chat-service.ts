import { apiRequest, TOKEN_KEY } from '@/service/api';
import type { ChatMessageDto, PageResponse, SendMessageRequest } from '@/service/dtos/chat-types';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Client, IMessage } from '@stomp/stompjs';

const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8090';

function getWsUrl(): string {
    return BASE_URL.replace(/^http/, 'ws') + '/ws';
}

export const chatService = {
    getMessages(chatRoomId: number, page = 0, size = 30) {
        return apiRequest<PageResponse<ChatMessageDto>>(
            `/api/chat/room/${chatRoomId}/messages?page=${page}&size=${size}`
        );
    },

    deleteMessage(messageId: number) {
        return apiRequest<void>(`/api/chat/messages/${messageId}`, { method: 'DELETE' });
    },

    markDelivered(chatRoomId: number) {
        return apiRequest<void>(`/api/chat/room/${chatRoomId}/delivered`);
    },

    markSeen(chatRoomId: number) {
        return apiRequest<void>(`/api/chat/room/${chatRoomId}/seen`);
    },

    async createStompClient(
        onConnect: () => void,
        onError?: (error: string) => void
    ): Promise<Client> {
        const token = await AsyncStorage.getItem(TOKEN_KEY);
        const wsUrl = getWsUrl();

        const client = new Client({
            // Use webSocketFactory for React Native compatibility instead of brokerURL
            webSocketFactory: () => new WebSocket(wsUrl),
            connectHeaders: {
                Authorization: `Bearer ${token ?? ''}`,
            },
            // Required for React Native - binary frames and missing NULL byte handling
            forceBinaryWSFrames: true,
            appendMissingNULLonIncoming: true,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            onConnect,
            onStompError: (frame) => {
                console.warn('[STOMP] Error:', frame.headers?.message ?? 'Unknown error');
                onError?.(frame.headers?.message ?? 'WebSocket error');
            },
            onWebSocketError: (event) => {
                console.warn('[STOMP] WebSocket error:', event);
                onError?.('WebSocket connection error');
            },
            onDisconnect: () => {
                console.log('[STOMP] Disconnected');
            },
        });

        return client;
    },

    subscribeToChatRoom(
        client: Client,
        chatRoomId: number,
        onMessage: (notification: any) => void
    ) {
        return client.subscribe(`/topic/chat.${chatRoomId}`, (message: IMessage) => {
            const body = JSON.parse(message.body);
            onMessage(body);
        });
    },

    subscribeToReceipts(
        client: Client,
        chatRoomId: number,
        onReceipt: (notification: any) => void
    ) {
        return client.subscribe(`/topic/chat.${chatRoomId}.receipts`, (message: IMessage) => {
            const body = JSON.parse(message.body);
            onReceipt(body);
        });
    },

    sendMessage(client: Client, chatRoomId: number, request: SendMessageRequest) {
        client.publish({
            destination: `/app/chat.${chatRoomId}.send`,
            body: JSON.stringify(request),
        });
    },
};
