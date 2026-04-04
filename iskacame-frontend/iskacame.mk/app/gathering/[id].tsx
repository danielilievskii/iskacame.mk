import React, { useCallback, useEffect, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    RefreshControl,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useAuth } from '@/context/auth-context';
import { gatheringService } from '@/service/gathering-service';
import type { GatheringDetailsDto, ParticipantDto, PlaceDto } from '@/service/dtos/gathering-types';
import {
    StatusBadge,
    UserAvatar,
    SectionHeader,
    EmptyState,
    formatDate,
} from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

function ParticipantRow({ p }: { p: ParticipantDto }) {
    const statusColor: Record<string, string> = {
        JOINED: '#34D399',
        INVITED: '#FBBF24',
        DECLINED: '#F87171',
        LEFT: '#9CA3AF',
        REMOVED: '#6B7280',
    };
    return (
        <View style={rowStyles.row}>
            <UserAvatar name={p.user.name} avatarUrl={p.user.avatarUrl} size={36} />
            <View style={rowStyles.info}>
                <Text style={rowStyles.name}>{p.user.name}</Text>
                <Text style={rowStyles.username}>@{p.user.username}</Text>
            </View>
            <View style={[rowStyles.statusDot, { backgroundColor: statusColor[p.participationStatus] ?? '#6B7280' }]} />
        </View>
    );
}

const rowStyles = StyleSheet.create({
    row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, gap: 12 },
    info: { flex: 1 },
    name: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    username: { fontSize: 12, color: '#6B7280', marginTop: 1 },
    statusDot: { width: 8, height: 8, borderRadius: 4 },
});

function PlaceCard({ place }: { place: PlaceDto }) {
    const levelEmoji: Record<string, string> = {
        FREE: '🆓',
        CHEAP: '💰',
        MODERATE: '💰💰',
        EXPENSIVE: '💰💰💰',
        LUXURY: '💎',
    };
    const typeEmoji: Record<string, string> = {
        CAFE: '☕',
        RESTAURANT: '🍽️',
        PARK: '🌳',
        OTHER: '📍',
    };
    return (
        <View style={placeStyles.card}>
            <Text style={placeStyles.emoji}>{typeEmoji[place.type] ?? '📍'}</Text>
            <View style={placeStyles.info}>
                <Text style={placeStyles.name}>{place.name}</Text>
                {place.address && <Text style={placeStyles.address} numberOfLines={1}>{place.address}</Text>}
            </View>
            <Text style={placeStyles.level}>{levelEmoji[place.priceLevel] ?? ''}</Text>
        </View>
    );
}

const placeStyles = StyleSheet.create({
    card: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        padding: 12,
        gap: 10,
        borderWidth: 1,
        borderColor: '#252530',
        marginBottom: 8,
    },
    emoji: { fontSize: 20 },
    info: { flex: 1 },
    name: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    address: { fontSize: 12, color: '#6B7280', marginTop: 2 },
    level: { fontSize: 14 },
});

function DetailRow({ label, value }: { label: string; value: string }) {
    return (
        <View style={detailStyles.row}>
            <Text style={detailStyles.label}>{label}</Text>
            <Text style={detailStyles.value}>{value}</Text>
        </View>
    );
}

const detailStyles = StyleSheet.create({
    row: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingVertical: 10,
        borderBottomWidth: 1,
        borderBottomColor: '#1F1F2E',
    },
    label: { fontSize: 14, color: '#6B7280' },
    value: { fontSize: 14, color: '#F0EBE1', fontWeight: '500', maxWidth: '55%', textAlign: 'right' },
});

export default function GatheringDetailsScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const router = useRouter();
    const { user } = useAuth();
    const [gathering, setGathering] = useState<GatheringDetailsDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const load = useCallback(async () => {
        if (!id) return;
        try {
            const data = await gatheringService.getGatheringDetails(Number(id));
            setGathering(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load gathering.');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [id]);

    useEffect(() => {
        load();
    }, [load]);

    const onRefresh = () => {
        setRefreshing(true);
        load();
    };

    const handleLeave = () => {
        Alert.alert('Leave gathering', 'Are you sure you want to leave this gathering?', [
            { text: 'Cancel', style: 'cancel' },
            {
                text: 'Leave',
                style: 'destructive',
                onPress: async () => {
                    try {
                        await gatheringService.leaveGathering(Number(id));
                        router.back();
                    } catch (e: any) {
                        Alert.alert('Error', e.message);
                    }
                },
            },
        ]);
    };

    if (loading) {
        return (
            <View style={styles.center}>
                <ActivityIndicator color={primaryColor} size="large" />
            </View>
        );
    }

    if (!gathering) {
        return <EmptyState icon="❓" title="Not found" subtitle="This gathering could not be loaded." />;
    }

    const isCreator = gathering.creatorId === user?.id;
    const canManage = isCreator && gathering.status !== 'CANCELLED' && gathering.status !== 'FINALIZED';
    const canLeave = !isCreator && gathering.status !== 'CANCELLED';

    return (
        <ScrollView
            style={styles.container}
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator={false}
            refreshControl={
                <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={primaryColor} />
            }
        >
            {/* Top bar */}
            <View style={styles.topBar}>
                <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
                    <Text style={styles.backText}>‹ Back</Text>
                </TouchableOpacity>
                {canManage && (
                    <TouchableOpacity
                        onPress={() => router.push({ pathname: '/gathering/manage/[id]', params: { id } })}
                        style={styles.manageBtn}
                    >
                        <Text style={styles.manageBtnText}>Manage</Text>
                    </TouchableOpacity>
                )}
            </View>

            {/* Hero */}
            <View style={styles.heroSection}>
                <StatusBadge status={gathering.status} />
                <Text style={styles.title}>{gathering.title}</Text>
                {gathering.description ? (
                    <Text style={styles.description}>{gathering.description}</Text>
                ) : null}
            </View>

            {/* Details card */}
            <View style={styles.card}>
                <SectionHeader title="DETAILS" />
                <DetailRow label="Start" value={formatDate(gathering.startDate)} />
                <DetailRow label="End" value={formatDate(gathering.endDate)} />
                {gathering.finalizedTime && (
                    <DetailRow label="Finalized" value={formatDate(gathering.finalizedTime)} />
                )}
                {gathering.finalizedPlace && (
                    <View style={{ marginTop: 12 }}>
                        <Text style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>FINALIZED PLACE</Text>
                        <PlaceCard place={gathering.finalizedPlace} />
                    </View>
                )}
            </View>

            {/* Participants */}
            {gathering.participants && gathering.participants.length > 0 && (
                <View style={styles.card}>
                    <SectionHeader title={`PARTICIPANTS (${gathering.participants.length})`} />
                    {gathering.participants.map((p) => (
                        <ParticipantRow key={p.user.id} p={p} />
                    ))}
                </View>
            )}

            {/* Suggested places */}
            {gathering.suggestedPlaces && gathering.suggestedPlaces.length > 0 && (
                <View style={styles.card}>
                    <SectionHeader title="SUGGESTED PLACES" />
                    {gathering.suggestedPlaces.map((place) => (
                        <PlaceCard key={place.id} place={place} />
                    ))}
                </View>
            )}

            {/* Actions */}
            {canLeave && (
                <TouchableOpacity style={styles.leaveBtn} onPress={handleLeave} activeOpacity={0.8}>
                    <Text style={styles.leaveBtnText}>Leave gathering</Text>
                </TouchableOpacity>
            )}
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    center: { flex: 1, backgroundColor: '#0B0B0F', justifyContent: 'center', alignItems: 'center' },
    container: { flex: 1, backgroundColor: '#0B0B0F' },
    content: { paddingTop: 56, paddingHorizontal: 24, paddingBottom: 40 },
    topBar: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 24,
    },
    backBtn: {},
    backText: { color: primaryColor, fontSize: 16, fontWeight: '600' },
    manageBtn: {
        backgroundColor: '#1F1F2E',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderWidth: 1,
        borderColor: '#252530',
    },
    manageBtnText: { color: primaryColor, fontWeight: '700', fontSize: 14 },
    heroSection: { marginBottom: 24, gap: 12 },
    title: { fontSize: 26, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5, lineHeight: 34 },
    description: { fontSize: 15, color: '#9CA3AF', lineHeight: 23 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 20,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 16,
    },
    leaveBtn: {
        backgroundColor: '#16161D',
        borderRadius: 12,
        borderWidth: 1,
        borderColor: '#3B1D1D',
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 4,
    },
    leaveBtnText: { color: '#EF4444', fontWeight: '700', fontSize: 15 },
});