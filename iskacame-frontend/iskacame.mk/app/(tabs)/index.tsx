import React, { useCallback, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';
import { useAuth } from '@/context/auth-context';
import { gatheringService } from '@/service/gathering-service';
import type { GatheringSummaryDto } from '@/service/dtos/gathering-types';
import { StatusBadge, EmptyState, formatShortDate } from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

export default function HomeScreen() {
    const { user } = useAuth();
    const router = useRouter();
    const [gatherings, setGatherings] = useState<GatheringSummaryDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const load = useCallback(async (isRefresh = false) => {
        if (!isRefresh) setLoading(true);
        try {
            const data = await gatheringService.getMyGatherings();
            setGatherings(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load gatherings.');
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

    const renderItem = ({ item }: { item: GatheringSummaryDto }) => (
        <TouchableOpacity
            style={styles.card}
            onPress={() => router.push({ pathname: '/gathering/[id]', params: { id: item.id } })}
            activeOpacity={0.75}
        >
            <View style={styles.cardTop}>
                <View style={styles.cardLeft}>
                    <View style={styles.dateChip}>
                        <Text style={styles.dateChipText}>{formatShortDate(item.createdAt)}</Text>
                    </View>
                    <StatusBadge status={item.status}/>
                </View>
                <Text style={styles.chevron}>›</Text>
            </View>
            <Text style={styles.cardTitle} numberOfLines={2}>
                {item.title}
            </Text>
            <View style={styles.cardFooter}>
                <Text style={styles.creatorLabel}>by {item.creator.name}</Text>
            </View>
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <View>
                    <Text style={styles.greeting}>Hey, {user?.name?.split(' ')[0]} 👋</Text>
                    <Text style={styles.subtitle}>Your gatherings</Text>
                </View>
                <TouchableOpacity
                    style={styles.newBtn}
                    onPress={() => router.push('/gathering/create')}
                    activeOpacity={0.8}
                >
                    <Text style={styles.newBtnText}>+ New</Text>
                </TouchableOpacity>
            </View>

            {loading ? (
                <View style={styles.center}>
                    <Text style={{ color: '#6B7280' }}>Loading…</Text>
                </View>
            ) : gatherings.length === 0 ? (
                <EmptyState
                    icon="🗓️"
                    title="No gatherings yet"
                    subtitle="Create your first gathering and invite your friends."
                />
            ) : (
                <FlatList
                    data={gatherings}
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
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#0B0B0F', paddingTop: 60 },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 24,
        marginBottom: 24,
    },
    greeting: { fontSize: 28, fontWeight: '800', color: '#F0EBE1', letterSpacing: -1 },
    subtitle: { fontSize: 15, color: '#6B7280', marginTop: 2 },
    newBtn: {
        backgroundColor: primaryColor,
        borderRadius: 20,
        paddingHorizontal: 16,
        paddingVertical: 8,
    },
    newBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 14 },
    center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
    list: { paddingHorizontal: 24, paddingBottom: 24, gap: 12 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 18,
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    cardTop: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 12,
    },
    cardLeft: { flexDirection: 'row', alignItems: 'center', gap: 8 },
    dateChip: {
        backgroundColor: '#0B0B0F',
        borderRadius: 8,
        paddingHorizontal: 8,
        paddingVertical: 3,
        borderWidth: 1,
        borderColor: '#252530',
    },
    dateChipText: { color: '#9CA3AF', fontSize: 11, fontWeight: '600' },
    chevron: { color: '#4B5563', fontSize: 22, fontWeight: '300' },
    cardTitle: { fontSize: 17, fontWeight: '700', color: '#F0EBE1', lineHeight: 24, marginBottom: 10 },
    cardFooter: { flexDirection: 'row', alignItems: 'center' },
    creatorLabel: { fontSize: 12, color: '#6B7280' },
});