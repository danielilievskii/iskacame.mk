import React, { useCallback, useMemo, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
    TextInput,
    Modal,
    Pressable,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';
import { useAuth } from '@/context/auth-context';
import { gatheringService } from '@/service/gathering-service';
import type { GatheringStatus, GatheringSummaryDto } from '@/service/dtos/gathering-types';
import { StatusBadge, EmptyState, formatShortDate } from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

type StatusFilter = 'ALL' | GatheringStatus;

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
    { value: 'ALL', label: 'All' },
    { value: 'DRAFT', label: 'Draft' },
    { value: 'OPEN', label: 'Open' },
    { value: 'FINALIZED', label: 'Finalized' },
    { value: 'CANCELLED', label: 'Cancelled' },
];

export default function HomeScreen() {
    const { user } = useAuth();
    const router = useRouter();
    const [gatherings, setGatherings] = useState<GatheringSummaryDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
    const [statusMenuOpen, setStatusMenuOpen] = useState(false);

    const currentStatusLabel = useMemo(
        () => STATUS_FILTERS.find((f) => f.value === statusFilter)?.label ?? 'All',
        [statusFilter]
    );

    const filteredGatherings = useMemo(() => {
        const q = search.trim().toLowerCase();
        return gatherings.filter((g) => {
            if (statusFilter !== 'ALL' && g.status !== statusFilter) return false;
            if (q === '') return true;
            return (
                g.title.toLowerCase().includes(q) ||
                g.creator.name?.toLowerCase().includes(q)
            );
        });
    }, [gatherings, search, statusFilter]);

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

    const renderItem = ({ item }: { item: GatheringSummaryDto }) => {
        const unread = item.unseenMessagesCount ?? 0;
        return (
            <TouchableOpacity
                style={styles.card}
                onPress={() => router.push({ pathname: '/gathering/[id]', params: { id: item.id } })}
                activeOpacity={0.75}
            >
                <View style={styles.cardTop}>
                    <View style={styles.cardLeft}>
                        {item.finalizedTime ? (
                            <View style={styles.dateChip}>
                                <Text style={styles.dateChipText}>{formatShortDate(item.finalizedTime)}</Text>
                            </View>
                        ) : (
                            <View style={styles.dateChip}>
                                <Text style={styles.dateChipText}>
                                    {formatShortDate(item.startDate)} – {formatShortDate(item.endDate)}
                                </Text>
                            </View>
                        )}
                        <StatusBadge status={item.status}/>
                    </View>
                    <View style={styles.cardRight}>
                        {unread > 0 && (
                            <View style={styles.unreadBadge}>
                                <Text style={styles.unreadBadgeText}>
                                    {unread > 99 ? '99+' : unread}
                                </Text>
                            </View>
                        )}
                        <Text style={styles.chevron}>›</Text>
                    </View>
                </View>
                <Text style={styles.cardTitle} numberOfLines={2}>
                    {item.title}
                </Text>
                <View style={styles.cardFooter}>
                    <Text style={styles.creatorLabel}>by {item.creator.name}</Text>
                </View>
            </TouchableOpacity>
        );
    };

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

            <View style={styles.toolbar}>
                <View style={styles.searchWrap}>
                    <TextInput
                        value={search}
                        onChangeText={setSearch}
                        placeholder="Search by title or creator…"
                        placeholderTextColor="#4B5563"
                        style={styles.searchInput}
                        returnKeyType="search"
                        clearButtonMode="while-editing"
                    />
                    {search.length > 0 && (
                        <TouchableOpacity
                            style={styles.searchClear}
                            onPress={() => setSearch('')}
                            hitSlop={10}
                        >
                            <Text style={styles.searchClearText}>✕</Text>
                        </TouchableOpacity>
                    )}
                </View>

                <TouchableOpacity
                    style={styles.dropdownBtn}
                    onPress={() => setStatusMenuOpen(true)}
                    activeOpacity={0.75}
                >
                    <Text style={styles.dropdownBtnText} numberOfLines={1}>
                        {currentStatusLabel}
                    </Text>
                    <Text style={styles.dropdownCaret}>▾</Text>
                </TouchableOpacity>
            </View>

            <Modal
                visible={statusMenuOpen}
                transparent
                animationType="fade"
                onRequestClose={() => setStatusMenuOpen(false)}
            >
                <Pressable
                    style={styles.modalBackdrop}
                    onPress={() => setStatusMenuOpen(false)}
                >
                    <Pressable style={styles.dropdownMenu}>
                        {STATUS_FILTERS.map((f) => {
                            const active = statusFilter === f.value;
                            return (
                                <TouchableOpacity
                                    key={f.value}
                                    style={[
                                        styles.dropdownItem,
                                        active && styles.dropdownItemActive,
                                    ]}
                                    onPress={() => {
                                        setStatusFilter(f.value);
                                        setStatusMenuOpen(false);
                                    }}
                                    activeOpacity={0.75}
                                >
                                    <Text
                                        style={[
                                            styles.dropdownItemText,
                                            active && styles.dropdownItemTextActive,
                                        ]}
                                    >
                                        {f.label}
                                    </Text>
                                    {active && <Text style={styles.dropdownCheck}>✓</Text>}
                                </TouchableOpacity>
                            );
                        })}
                    </Pressable>
                </Pressable>
            </Modal>

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
            ) : filteredGatherings.length === 0 ? (
                <EmptyState
                    icon="🔍"
                    title="No matches"
                    subtitle="Try a different search or status filter."
                />
            ) : (
                <FlatList
                    data={filteredGatherings}
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
    toolbar: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        paddingHorizontal: 24,
        marginBottom: 16,
    },
    searchWrap: {
        flex: 1,
        position: 'relative',
        justifyContent: 'center',
    },
    searchInput: {
        backgroundColor: '#16161D',
        borderRadius: 14,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        paddingHorizontal: 16,
        paddingVertical: 12,
        paddingRight: 40,
        color: '#F0EBE1',
        fontSize: 14,
    },
    searchClear: {
        position: 'absolute',
        right: 12,
        width: 24,
        height: 24,
        borderRadius: 12,
        backgroundColor: '#252530',
        justifyContent: 'center',
        alignItems: 'center',
    },
    searchClearText: { color: '#9CA3AF', fontSize: 11, fontWeight: '700' },
    dropdownBtn: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        paddingHorizontal: 14,
        paddingVertical: 12,
        backgroundColor: '#16161D',
        borderRadius: 14,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        minWidth: 110,
    },
    dropdownBtnText: {
        color: '#F0EBE1',
        fontSize: 14,
        fontWeight: '600',
        flex: 1,
    },
    dropdownCaret: { color: '#9CA3AF', fontSize: 12 },
    modalBackdrop: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 32,
    },
    dropdownMenu: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        width: '100%',
        maxWidth: 280,
        paddingVertical: 6,
    },
    dropdownItem: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 18,
        paddingVertical: 14,
    },
    dropdownItemActive: { backgroundColor: '#0B0B0F' },
    dropdownItemText: { color: '#F0EBE1', fontSize: 15, fontWeight: '500' },
    dropdownItemTextActive: { color: primaryColor, fontWeight: '700' },
    dropdownCheck: { color: primaryColor, fontSize: 14, fontWeight: '800' },
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
    cardRight: { flexDirection: 'row', alignItems: 'center', gap: 8 },
    unreadBadge: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        minWidth: 24,
        height: 24,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 7,
    },
    unreadBadgeText: {
        color: '#0B0B0F',
        fontSize: 12,
        fontWeight: '800',
    },
    chevron: { color: '#4B5563', fontSize: 22, fontWeight: '300' },
    cardTitle: { fontSize: 17, fontWeight: '700', color: '#F0EBE1', lineHeight: 24, marginBottom: 10 },
    cardFooter: { flexDirection: 'row', alignItems: 'center' },
    creatorLabel: { fontSize: 12, color: '#6B7280' },
});