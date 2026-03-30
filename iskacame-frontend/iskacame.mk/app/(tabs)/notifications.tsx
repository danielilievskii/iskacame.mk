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
import type { GatheringInvitationDto } from '@/service/dtos/gathering-types';
import { UserAvatar, EmptyState, formatShortDate } from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

export default function NotificationsScreen() {
    const router = useRouter();
    const [invitations, setInvitations] = useState<GatheringInvitationDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [selectedInvitation, setSelectedInvitation] = useState<GatheringInvitationDto | null>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const load = useCallback(async (isRefresh = false) => {
        if (!isRefresh) setLoading(true);
        try {
            const data = await gatheringService.getInvitations();
            setInvitations(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load invitations.');
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

    const handleAccept = async () => {
        if (!selectedInvitation) return;
        setActionLoading(true);
        try {
            await gatheringService.acceptInvitation(selectedInvitation.id);
            setSelectedInvitation(null);
            router.push({
                pathname: '/gathering/pick-preferences',
                params: { gatheringId: selectedInvitation.gatheringId },
            });
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to accept invitation.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleDecline = async () => {
        if (!selectedInvitation) return;
        setActionLoading(true);
        try {
            await gatheringService.declineInvitation(selectedInvitation.id);
            setInvitations((prev) => prev.filter((i) => i.id !== selectedInvitation.id));
            setSelectedInvitation(null);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to decline invitation.');
        } finally {
            setActionLoading(false);
        }
    };

    const renderItem = ({ item }: { item: GatheringInvitationDto }) => (
        <TouchableOpacity
            style={styles.card}
            onPress={() => setSelectedInvitation(item)}
            activeOpacity={0.75}
        >
            <View style={styles.cardRow}>
                <UserAvatar
                    name={item.gatheringCreator.name}
                    avatarUrl={item.gatheringCreator.avatarUrl}
                    size={44}
                />
                <View style={styles.cardInfo}>
                    <Text style={styles.cardTitle} numberOfLines={1}>
                        {item.gatheringTitle}
                    </Text>
                    <Text style={styles.cardSubtitle}>
                        Invited by {item.gatheringCreator.name}
                    </Text>
                </View>
                <Text style={styles.cardDate}>{formatShortDate(item.createdAt)}</Text>
            </View>
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.headerTitle}>Notifications</Text>
                <Text style={styles.headerSubtitle}>Your pending invitations</Text>
            </View>

            {loading ? (
                <View style={styles.center}>
                    <Text style={{ color: '#6B7280' }}>Loading...</Text>
                </View>
            ) : invitations.length === 0 ? (
                <EmptyState
                    icon="🔔"
                    title="No invitations"
                    subtitle="You're all caught up! New gathering invitations will appear here."
                />
            ) : (
                <FlatList
                    data={invitations}
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
                visible={!!selectedInvitation}
                transparent
                animationType="fade"
                onRequestClose={() => setSelectedInvitation(null)}
            >
                <View style={styles.overlay}>
                    <View style={styles.dialog}>
                        <Text style={styles.dialogTitle}>Gathering Invitation</Text>
                        {selectedInvitation && (
                            <>
                                <Text style={styles.dialogGathering}>
                                    {selectedInvitation.gatheringTitle}
                                </Text>
                                <Text style={styles.dialogCreator}>
                                    From {selectedInvitation.gatheringCreator.name}
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
                            onPress={() => setSelectedInvitation(null)}
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
    header: { paddingHorizontal: 24, marginBottom: 24 },
    headerTitle: { fontSize: 28, fontWeight: '800', color: '#F0EBE1', letterSpacing: -1 },
    headerSubtitle: { fontSize: 15, color: '#6B7280', marginTop: 2 },
    center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
    list: { paddingHorizontal: 24, paddingBottom: 24, gap: 12 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    cardRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
    cardInfo: { flex: 1 },
    cardTitle: { fontSize: 16, fontWeight: '700', color: '#F0EBE1' },
    cardSubtitle: { fontSize: 13, color: '#6B7280', marginTop: 2 },
    cardDate: { fontSize: 12, color: '#4B5563' },
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
