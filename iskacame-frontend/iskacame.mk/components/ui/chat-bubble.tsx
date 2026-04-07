import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TouchableOpacity,
    TextInput,
    FlatList,
    KeyboardAvoidingView,
    Platform,
    ActivityIndicator,
    Image,
    Modal,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAuth } from '@/context/auth-context';
import { chatService } from '@/service/chat-service';
import type {
    ChatMessageDto,
    ChatNotification,
    MessageReceiptNotification,
    MessageReceiptDto,
} from '@/service/dtos/chat-types';
import type { ParticipantDto } from '@/service/dtos/gathering-types';
import { primaryColor } from '@/constants/theme';
import { Client, StompSubscription } from '@stomp/stompjs';

interface ChatBubbleProps {
    chatRoomId: number;
    participants?: ParticipantDto[] | null;
}

export default function ChatBubble({ chatRoomId, participants }: ChatBubbleProps) {
    const { user } = useAuth();
    const insets = useSafeAreaInsets();
    const [open, setOpen] = useState(false);
    const [messages, setMessages] = useState<ChatMessageDto[]>([]);
    const [inputText, setInputText] = useState('');
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);
    const [connected, setConnected] = useState(false);
    const [expandedMsgId, setExpandedMsgId] = useState<number | null>(null);

    const stompClientRef = useRef<Client | null>(null);
    const subscriptionRef = useRef<StompSubscription | null>(null);
    const receiptsSubRef = useRef<StompSubscription | null>(null);
    const flatListRef = useRef<FlatList>(null);
    const openRef = useRef(open);

    // Build a lookup map: userId -> participant name
    const participantMap = useRef<Map<number, string>>(new Map());
    useEffect(() => {
        const map = new Map<number, string>();
        participants?.forEach((p) => map.set(p.user.id, p.user.name));
        participantMap.current = map;
    }, [participants]);

    useEffect(() => {
        openRef.current = open;
    }, [open]);

    // Load initial messages
    const loadMessages = useCallback(async () => {
        setLoading(true);
        try {
            const response = await chatService.getMessages(chatRoomId, 0, 30);
            setMessages(response.content.reverse());
            setPage(0);
            setHasMore(!response.last);
        } catch {
            // silently fail
        } finally {
            setLoading(false);
        }
    }, [chatRoomId]);

    // Load more (older) messages
    const loadMore = useCallback(async () => {
        if (loadingMore || !hasMore) return;
        setLoadingMore(true);
        try {
            const nextPage = page + 1;
            const response = await chatService.getMessages(chatRoomId, nextPage, 30);
            setMessages((prev) => [...response.content.reverse(), ...prev]);
            setPage(nextPage);
            setHasMore(!response.last);
        } catch {
            // silently fail
        } finally {
            setLoadingMore(false);
        }
    }, [chatRoomId, page, hasMore, loadingMore]);

    // Handle receipt WebSocket notification - update message receipts in state
    const handleReceiptNotification = useCallback((notification: MessageReceiptNotification) => {
        const { messageId, recipientId, status } = notification;

        setMessages((prev) =>
            prev.map((msg) => {
                // messageId === -1 means bulk update for all messages in the room
                if (messageId !== -1 && msg.id !== messageId) return msg;
                // Only update if this recipient has a receipt on this message
                const updatedReceipts = msg.receipts.map((r) => {
                    if (r.recipientId !== recipientId) return r;
                    return {
                        ...r,
                        status: status as MessageReceiptDto['status'],
                        ...(status === 'SEEN' ? { seenAt: new Date().toISOString() } : {}),
                        ...(status === 'DELIVERED' ? { deliveredAt: new Date().toISOString() } : {}),
                    };
                });
                return { ...msg, receipts: updatedReceipts };
            })
        );
    }, []);

    // Connect WebSocket
    useEffect(() => {
        let client: Client | null = null;

        const connect = async () => {
            client = await chatService.createStompClient(
                () => {
                    setConnected(true);
                    if (client) {
                        // Subscribe to messages
                        subscriptionRef.current = chatService.subscribeToChatRoom(
                            client,
                            chatRoomId,
                            (notification: ChatNotification) => {
                                if (notification.type === 'MESSAGE_SENT') {
                                    const msg = notification.message;
                                    setMessages((prev) => {
                                        if (prev.some((m) => m.id === msg.id)) return prev;
                                        return [...prev, msg];
                                    });
                                    if (!openRef.current && msg.sender.id !== user?.id) {
                                        setUnreadCount((c) => c + 1);
                                    }
                                } else if (notification.type === 'MESSAGE_DELETED') {
                                    setMessages((prev) =>
                                        prev.filter((m) => m.id !== notification.messageId)
                                    );
                                }
                            }
                        );

                        // Subscribe to receipts
                        receiptsSubRef.current = chatService.subscribeToReceipts(
                            client,
                            chatRoomId,
                            handleReceiptNotification
                        );
                    }
                },
                () => {
                    setConnected(false);
                }
            );
            stompClientRef.current = client;
            client.activate();
        };

        connect();

        return () => {
            subscriptionRef.current?.unsubscribe();
            receiptsSubRef.current?.unsubscribe();
            client?.deactivate();
            stompClientRef.current = null;
            setConnected(false);
        };
    }, [chatRoomId, user?.id, handleReceiptNotification]);

    // When chat opens, mark seen & reset unread
    useEffect(() => {
        if (open) {
            setUnreadCount(0);
            chatService.markSeen(chatRoomId).catch(() => {});
            loadMessages();
        }
    }, [open, chatRoomId, loadMessages]);

    const handleSend = useCallback(() => {
        const text = inputText.trim();
        if (!text) return;
        const client = stompClientRef.current;
        if (!client || !client.connected) {
            console.warn('[Chat] Cannot send: not connected');
            return;
        }
        try {
            chatService.sendMessage(client, chatRoomId, { content: text });
            setInputText('');
        } catch (e) {
            console.warn('[Chat] Send failed:', e);
        }
    }, [inputText, chatRoomId]);

    const formatTime = (dateStr: string) => {
        const d = new Date(dateStr);
        return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
    };

    const formatDateSeparator = (dateStr: string) => {
        const d = new Date(dateStr);
        const now = new Date();
        if (d.toDateString() === now.toDateString()) return 'Today';
        const yesterday = new Date(now);
        yesterday.setDate(yesterday.getDate() - 1);
        if (d.toDateString() === yesterday.toDateString()) return 'Yesterday';
        return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
    };

    const shouldShowDateSeparator = (index: number) => {
        if (index === 0) return true;
        return new Date(messages[index].sentAt).toDateString() !== new Date(messages[index - 1].sentAt).toDateString();
    };

    // Get receipt status summary for a message sent by the current user
    const getReceiptSummary = (receipts: MessageReceiptDto[]) => {
        if (receipts.length === 0) return null;
        const seenBy = receipts.filter((r) => r.status === 'SEEN');
        const deliveredTo = receipts.filter((r) => r.status === 'DELIVERED');

        if (seenBy.length > 0) {
            if (seenBy.length === receipts.length && receipts.length > 1) {
                return { label: 'Seen by all', type: 'seen' as const };
            }
            const names = seenBy
                .map((r) => participantMap.current.get(r.recipientId)?.split(' ')[0] ?? 'Someone')
                .join(', ');
            return { label: `Seen by ${names}`, type: 'seen' as const };
        }
        if (deliveredTo.length === receipts.length) {
            return { label: 'Delivered', type: 'delivered' as const };
        }
        if (deliveredTo.length > 0) {
            return { label: 'Delivered to some', type: 'delivered' as const };
        }
        return { label: 'Sent', type: 'sent' as const };
    };

    const renderMessage = ({ item, index }: { item: ChatMessageDto; index: number }) => {
        const isMe = item.sender.id === user?.id;
        const showDate = shouldShowDateSeparator(index);
        const initials = item.sender.name
            .split(' ')
            .map((w) => w[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);

        const receipt = isMe ? getReceiptSummary(item.receipts) : null;
        const isExpanded = expandedMsgId === item.id;

        return (
            <View>
                {showDate && (
                    <View style={cs.dateSeparator}>
                        <Text style={cs.dateSeparatorText}>{formatDateSeparator(item.sentAt)}</Text>
                    </View>
                )}
                <View style={[cs.msgRow, isMe && cs.msgRowMe]}>
                    {!isMe && (
                        <View style={cs.avatarSmall}>
                            {item.sender.avatarUrl ? (
                                <Image source={{ uri: item.sender.avatarUrl }} style={cs.avatarImg} />
                            ) : (
                                <Text style={cs.avatarText}>{initials}</Text>
                            )}
                        </View>
                    )}
                    <TouchableOpacity
                        style={[cs.bubbleWrapper, isMe && cs.bubbleWrapperMe]}
                        activeOpacity={0.8}
                        onPress={() => setExpandedMsgId(isExpanded ? null : item.id)}
                    >
                        <View style={[cs.bubble, isMe ? cs.bubbleMe : cs.bubbleOther]}>
                            {!isMe && <Text style={cs.senderName}>{item.sender.name}</Text>}
                            <Text style={[cs.msgText, isMe && cs.msgTextMe]}>{item.content}</Text>
                            <Text style={[cs.msgTime, isMe && cs.msgTimeMe]}>{formatTime(item.sentAt)}</Text>
                        </View>
                        {isExpanded && receipt && (
                            <Text
                                style={[
                                    cs.receiptText,
                                    receipt.type === 'seen' && cs.receiptSeen,
                                ]}
                            >
                                {receipt.label}
                            </Text>
                        )}
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

    return (
        <>
            {/* Full-screen modal chat */}
            <Modal
                visible={open}
                animationType="slide"
                transparent={false}
                onRequestClose={() => setOpen(false)}
            >
                <KeyboardAvoidingView
                    style={[cs.modalContainer, { paddingTop: insets.top }]}
                    behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    keyboardVerticalOffset={0}
                >
                    {/* Header */}
                    <View style={cs.header}>
                        <Text style={cs.headerTitle}>Chat</Text>
                        <TouchableOpacity onPress={() => setOpen(false)} style={cs.closeBtn}>
                            <Text style={cs.closeBtnText}>X</Text>
                        </TouchableOpacity>
                    </View>

                    {/* Messages */}
                    {loading ? (
                        <View style={cs.loadingContainer}>
                            <ActivityIndicator color={primaryColor} size="large" />
                        </View>
                    ) : messages.length === 0 ? (
                        <View style={cs.emptyContainer}>
                            <Text style={cs.emptyText}>No messages yet</Text>
                            <Text style={cs.emptySubtext}>Start the conversation!</Text>
                        </View>
                    ) : (
                        <FlatList
                            ref={flatListRef}
                            data={messages}
                            renderItem={renderMessage}
                            keyExtractor={(item) => String(item.id)}
                            style={cs.messagesList}
                            contentContainerStyle={cs.messagesContent}
                            onContentSizeChange={() =>
                                flatListRef.current?.scrollToEnd({ animated: false })
                            }
                            onScroll={({ nativeEvent }) => {
                                if (nativeEvent.contentOffset.y <= 0 && hasMore && !loadingMore) {
                                    loadMore();
                                }
                            }}
                            scrollEventThrottle={400}
                            ListHeaderComponent={
                                loadingMore ? (
                                    <ActivityIndicator color={primaryColor} size="small" style={{ paddingVertical: 10 }} />
                                ) : null
                            }
                            keyboardShouldPersistTaps="handled"
                            keyboardDismissMode="interactive"
                        />
                    )}

                    {/* Connection status */}
                    {!connected && (
                        <View style={cs.connectionBar}>
                            <ActivityIndicator color={primaryColor} size="small" />
                            <Text style={cs.connectionText}>Connecting...</Text>
                        </View>
                    )}

                    {/* Input */}
                    <View style={[cs.inputBar, { paddingBottom: Math.max(insets.bottom, 10) }]}>
                        <TextInput
                            style={cs.input}
                            value={inputText}
                            onChangeText={setInputText}
                            placeholder={connected ? 'Type a message...' : 'Connecting...'}
                            placeholderTextColor="#6B7280"
                            multiline
                            maxLength={1000}
                            editable={connected}
                        />
                        <TouchableOpacity
                            onPress={handleSend}
                            style={[cs.sendBtn, (!inputText.trim() || !connected) && cs.sendBtnDisabled]}
                            disabled={!inputText.trim() || !connected}
                            activeOpacity={0.7}
                        >
                            <Text style={cs.sendBtnText}>Send</Text>
                        </TouchableOpacity>
                    </View>
                </KeyboardAvoidingView>
            </Modal>

            {/* Floating button */}
            {!open && (
                <View style={cs.fabWrapper} pointerEvents="box-none">
                    <TouchableOpacity style={cs.fab} onPress={() => setOpen(true)} activeOpacity={0.8}>
                        <Text style={cs.fabIcon}>💬</Text>
                        {unreadCount > 0 && (
                            <View style={cs.badge}>
                                <Text style={cs.badgeText}>{unreadCount > 99 ? '99+' : unreadCount}</Text>
                            </View>
                        )}
                    </TouchableOpacity>
                </View>
            )}
        </>
    );
}

const cs = StyleSheet.create({
    // FAB
    fabWrapper: {
        position: 'absolute',
        bottom: 0,
        right: 0,
        left: 0,
        top: 0,
        justifyContent: 'flex-end',
        alignItems: 'flex-end',
    },
    fab: {
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: primaryColor,
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 20,
        marginBottom: 20,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 8,
    },
    fabIcon: {
        fontSize: 26,
    },
    badge: {
        position: 'absolute',
        top: -4,
        right: -4,
        backgroundColor: '#EF4444',
        borderRadius: 12,
        minWidth: 22,
        height: 22,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 6,
        borderWidth: 2,
        borderColor: '#0B0B0F',
    },
    badgeText: {
        color: '#FFFFFF',
        fontSize: 11,
        fontWeight: '800',
    },

    // Modal
    modalContainer: {
        flex: 1,
        backgroundColor: '#0D0D14',
    },

    // Header
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingVertical: 14,
        borderBottomWidth: 1,
        borderBottomColor: '#1F1F2E',
        backgroundColor: '#16161D',
    },
    headerTitle: {
        fontSize: 17,
        fontWeight: '800',
        color: '#F0EBE1',
    },
    closeBtn: {
        width: 32,
        height: 32,
        borderRadius: 16,
        backgroundColor: '#0B0B0F',
        justifyContent: 'center',
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#252530',
    },
    closeBtnText: {
        color: '#6B7280',
        fontWeight: '700',
        fontSize: 14,
    },

    // Messages
    loadingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    emptyContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 24,
    },
    emptyText: {
        fontSize: 16,
        fontWeight: '700',
        color: '#4B5563',
    },
    emptySubtext: {
        fontSize: 13,
        color: '#374151',
        marginTop: 4,
    },
    messagesList: {
        flex: 1,
        backgroundColor: '#0D0D14',
    },
    messagesContent: {
        paddingHorizontal: 12,
        paddingVertical: 8,
    },

    // Date separator
    dateSeparator: {
        alignItems: 'center',
        marginVertical: 12,
    },
    dateSeparatorText: {
        fontSize: 11,
        color: '#4B5563',
        fontWeight: '600',
        backgroundColor: '#16161D',
        paddingHorizontal: 12,
        paddingVertical: 4,
        borderRadius: 10,
        overflow: 'hidden',
    },

    // Message row
    msgRow: {
        flexDirection: 'row',
        alignItems: 'flex-end',
        marginBottom: 6,
        gap: 8,
    },
    msgRowMe: {
        justifyContent: 'flex-end',
    },
    avatarSmall: {
        width: 28,
        height: 28,
        borderRadius: 14,
        backgroundColor: '#2D2A45',
        justifyContent: 'center',
        alignItems: 'center',
        overflow: 'hidden',
    },
    avatarImg: {
        width: 28,
        height: 28,
        borderRadius: 14,
    },
    avatarText: {
        fontSize: 10,
        fontWeight: '700',
        color: primaryColor,
    },

    // Bubble wrapper + bubble
    bubbleWrapper: {
        maxWidth: '75%',
    },
    bubbleWrapperMe: {
        alignItems: 'flex-end',
    },
    bubble: {
        borderRadius: 16,
        paddingHorizontal: 14,
        paddingVertical: 10,
    },
    bubbleMe: {
        backgroundColor: primaryColor,
        borderBottomRightRadius: 4,
    },
    bubbleOther: {
        backgroundColor: '#252530',
        borderBottomLeftRadius: 4,
    },
    senderName: {
        fontSize: 11,
        fontWeight: '700',
        color: primaryColor,
        marginBottom: 2,
    },
    msgText: {
        fontSize: 14,
        color: '#F0EBE1',
        lineHeight: 20,
    },
    msgTextMe: {
        color: '#0B0B0F',
    },
    msgTime: {
        fontSize: 10,
        color: '#6B7280',
        marginTop: 4,
        alignSelf: 'flex-end',
    },
    msgTimeMe: {
        color: 'rgba(11,11,15,0.5)',
    },

    // Receipt text
    receiptText: {
        fontSize: 10,
        color: '#4B5563',
        marginTop: 2,
        alignSelf: 'flex-end',
        marginRight: 4,
    },
    receiptSeen: {
        color: '#60A5FA',
    },

    // Connection bar
    connectionBar: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 8,
        backgroundColor: '#1A1520',
        borderTopWidth: 1,
        borderTopColor: '#1F1F2E',
    },
    connectionText: {
        fontSize: 12,
        color: '#6B7280',
        fontWeight: '600',
    },

    // Input bar
    inputBar: {
        flexDirection: 'row',
        alignItems: 'flex-end',
        paddingHorizontal: 12,
        paddingTop: 10,
        borderTopWidth: 1,
        borderTopColor: '#1F1F2E',
        backgroundColor: '#16161D',
        gap: 8,
    },
    input: {
        flex: 1,
        backgroundColor: '#0B0B0F',
        borderWidth: 1,
        borderColor: '#252530',
        borderRadius: 20,
        paddingHorizontal: 16,
        paddingVertical: 10,
        color: '#F0EBE1',
        fontSize: 14,
        maxHeight: 100,
    },
    sendBtn: {
        backgroundColor: primaryColor,
        borderRadius: 20,
        paddingHorizontal: 18,
        paddingVertical: 10,
        justifyContent: 'center',
        alignItems: 'center',
    },
    sendBtnDisabled: {
        opacity: 0.4,
    },
    sendBtnText: {
        color: '#0B0B0F',
        fontWeight: '800',
        fontSize: 14,
    },
});
