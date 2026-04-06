import React, { useCallback, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
    Modal,
    ActivityIndicator,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';
import { gatheringService } from '@/service/gathering-service';
import { notificationService } from '@/service/notification-service';
import type { NotificationDto, GatheringInvitationDto } from '@/service/dtos/gathering-types';
import { UserAvatar, EmptyState } from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

const ICON_MAP: Record<string, string> = {
    GATHERING_INVITE: '✉️',
    VOTE_STARTED: '🗳️',
    VOTE_ENDED: '📊',
    GATHERING_CANCELLED: '❌',
};

function timeAgo(dateStr: string): string {
    const now = Date.now();
    const date = new Date(dateStr).getTime();
    const diff = now - date;
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'Just now';
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
}

export default function NotificationsScreen() {
    const router = useRouter();
    const [notifications, setNotifications] = useState<NotificationDto[]>([]);
    const [invitations, setInvitations] = useState<GatheringInvitationDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [selectedInvite, setSelectedInvite] = useState<GatheringInvitationDto | null>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const load = useCallback(async (isRefresh = false) => {
        if (!isRefresh) setLoading(true);
        try {
            const [notifData, inviteData] = await Promise.all([
                notificationService.getNotifications(),
                gatheringService.getInvitations(),
            ]);
            setNotifications(notifData);
            setInvitations(inviteData);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load notifications.');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, []);

    useFocusEffect(
        useCallback(() => {
            load();
        }, [load])
    );

    const onRefresh = () => {
        setRefreshing(true);
        load(true);
    };

    const handleMarkAllRead = async () => {
        try {
            await notificationService.markAllAsRead();
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to mark all as read.');
        }
    };

    const handleNotificationTap = async (notification: NotificationDto) => {
        if (!notification.read) {
            try {
                await notificationService.markAsRead(notification.id);
                setNotifications((prev) =>
                    prev.map((n) => (n.id === notification.id ? { ...n, read: true } : n))
                );
            } catch { }
        }

        if (notification.type === 'GATHERING_INVITE') {
            const invite = invitations.find((i) => i.gatheringId === notification.gatheringId);
            if (invite) {
                setSelectedInvite(invite);
            } else if (notification.gatheringId) {
                router.push(`/gathering/${notification.gatheringId}`);
            }
        } else if (notification.gatheringId) {
            router.push(`/gathering/${notification.gatheringId}`);
        }
    };

    const handleAccept = async () => {
        if (!selectedInvite) return;
        setActionLoading(true);
        try {
            await gatheringService.acceptInvitation(selectedInvite.id);
            setSelectedInvite(null);
            router.push({
                pathname: '/gathering/pick-preferences',
                params: { gatheringId: selectedInvite.gatheringId },
            });
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to accept invitation.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleDecline = async () => {
        if (!selectedInvite) return;
        setActionLoading(true);
        try {
            await gatheringService.declineInvitation(selectedInvite.id);
            setInvitations((prev) => prev.filter((i) => i.id !== selectedInvite.id));
            setSelectedInvite(null);
            load(true);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to decline invitation.');
        } finally {
            setActionLoading(false);
        }
    };

    const unreadCount = notifications.filter((n) => !n.read).length;

    const renderItem = ({ item }: { item: NotificationDto }) => (
        <TouchableOpacity
            style={[styles.card, !item.read && styles.cardUnread]}
            onPress={() => handleNotificationTap(item)}
            activeOpacity={0.75}
        >
            <View style={styles.cardRow}>
                <View style={styles.iconContainer}>
                    <Text style={styles.icon}>{ICON_MAP[item.type] ?? '🔔'}</Text>
                </View>
                <View style={styles.cardInfo}>
                    <Text style={styles.cardTitle} numberOfLines={1}>
                        {item.title}
                    </Text>
                    <Text style={styles.cardBody} numberOfLines={2}>
                        {item.body}
                    </Text>
                </View>
                <View style={styles.cardRight}>
                    <Text style={styles.cardDate}>{timeAgo(item.createdAt)}</Text>
                    {!item.read && <View style={styles.unreadDot} />}
                </View>
            </View>
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <View>
                    <Text style={styles.headerTitle}>Notifications</Text>
                    <Text style={styles.headerSubtitle}>
                        {unreadCount > 0 ? `${unreadCount} unread` : 'All caught up'}
                    </Text>
                </View>
                {unreadCount > 0 && (
                    <TouchableOpacity onPress={handleMarkAllRead} style={styles.markAllBtn}>
                        <Text style={styles.markAllBtnText}>Mark all read</Text>
                    </TouchableOpacity>
                )}
            </View>

            {loading ? (
                <View style={styles.center}>
                    <Text style={{ color: '#6B7280' }}>Loading...</Text>
                </View>
            ) : notifications.length === 0 ? (
                <EmptyState
                    icon="🔔"
                    title="No notifications"
                    subtitle="You're all caught up! New notifications will appear here."
                />
            ) : (
                <FlatList
                    data={notifications}
                    keyExtractor={(item) => String(item.id)}
                    renderItem={renderItem}
                    contentContainerStyle={styles.list}
                    showsVerticalScrollIndicator={false}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor={primaryColor}
                        />
                    }
                />
            )}

            {/* Invitation dialog */}
            <Modal
                visible={!!selectedInvite}
                transparent
                animationType="fade"
                onRequestClose={() => setSelectedInvite(null)}
            >
                <View style={styles.overlay}>
                    <View style={styles.dialog}>
                        <Text style={styles.dialogTitle}>Gathering Invitation</Text>
                        {selectedInvite && (
                            <>
                                <Text style={styles.dialogGathering}>
                                    {selectedInvite.gatheringTitle}
                                </Text>
                                <Text style={styles.dialogCreator}>
                                    From {selectedInvite.gatheringCreator.name}
                                </Text>
                            </>
                        )}

                        <View style={styles.dialogActions}>
                            <TouchableOpacity
                                style={styles.declineBtn}
                                onPress={handleDecline}
                                disabled={actionLoading}
                                activeOpacity={0.8}
                            >
                                {actionLoading ? (
                                    <ActivityIndicator color="#F87171" size="small" />
                                ) : (
                                    <Text style={styles.declineBtnText}>Decline</Text>
                                )}
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={styles.acceptBtn}
                                onPress={handleAccept}
                                disabled={actionLoading}
                                activeOpacity={0.8}
                            >
                                {actionLoading ? (
                                    <ActivityIndicator color="#0B0B0F" size="small" />
                                ) : (
                                    <Text style={styles.acceptBtnText}>Accept</Text>
                                )}
                            </TouchableOpacity>
                        </View>

                        <TouchableOpacity
                            style={styles.cancelBtn}
                            onPress={() => setSelectedInvite(null)}
                            disabled={actionLoading}
                        >
                            <Text style={styles.cancelBtnText}>Cancel</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </Modal>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#0B0B0F', paddingTop: 60 },
    header: {
        paddingHorizontal: 24,
        marginBottom: 24,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
    },
    headerTitle: { fontSize: 28, fontWeight: '800', color: '#F0EBE1', letterSpacing: -1 },
    headerSubtitle: { fontSize: 15, color: '#6B7280', marginTop: 2 },
    markAllBtn: {
        backgroundColor: '#1F1F2E',
        borderRadius: 10,
        paddingHorizontal: 14,
        paddingVertical: 8,
        borderWidth: 1,
        borderColor: '#252530',
    },
    markAllBtnText: { color: primaryColor, fontSize: 12, fontWeight: '700' },
    center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
    list: { paddingHorizontal: 24, paddingBottom: 24, gap: 10 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    cardUnread: {
        borderColor: '#2D2A45',
        backgroundColor: '#1A1828',
    },
    cardRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
    iconContainer: {
        width: 40,
        height: 40,
        borderRadius: 12,
        backgroundColor: '#0B0B0F',
        justifyContent: 'center',
        alignItems: 'center',
    },
    icon: { fontSize: 18 },
    cardInfo: { flex: 1 },
    cardTitle: { fontSize: 15, fontWeight: '700', color: '#F0EBE1' },
    cardBody: { fontSize: 13, color: '#9CA3AF', marginTop: 2 },
    cardRight: { alignItems: 'flex-end', gap: 6 },
    cardDate: { fontSize: 11, color: '#4B5563' },
    unreadDot: {
        width: 8,
        height: 8,
        borderRadius: 4,
        backgroundColor: primaryColor,
    },
    overlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.7)',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 32,
    },
    dialog: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 28,
        width: '100%',
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    dialogTitle: {
        fontSize: 20,
        fontWeight: '800',
        color: '#F0EBE1',
        marginBottom: 16,
        textAlign: 'center',
    },
    dialogGathering: {
        fontSize: 17,
        fontWeight: '700',
        color: primaryColor,
        textAlign: 'center',
        marginBottom: 4,
    },
    dialogCreator: {
        fontSize: 14,
        color: '#6B7280',
        textAlign: 'center',
        marginBottom: 28,
    },
    dialogActions: {
        flexDirection: 'row',
        gap: 12,
    },
    declineBtn: {
        flex: 1,
        backgroundColor: '#2A1515',
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#3B1D1D',
    },
    declineBtnText: { color: '#F87171', fontWeight: '700', fontSize: 15 },
    acceptBtn: {
        flex: 1,
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
    },
    acceptBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
    cancelBtn: {
        marginTop: 12,
        paddingVertical: 10,
        alignItems: 'center',
    },
    cancelBtnText: { color: '#6B7280', fontSize: 14 },
});
