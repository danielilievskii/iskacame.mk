import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    RefreshControl,
    ActivityIndicator,
    Alert,
    Modal,
    TextInput,
    Image,
    KeyboardAvoidingView,
    Platform,
    Linking,
} from 'react-native';
import { WebView } from 'react-native-webview';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useAuth } from '@/context/auth-context';
import { gatheringService } from '@/service/gathering-service';
import { userSearchService, type UserSearchDto } from '@/service/user-search-service';
import type {
    GatheringDetailsDto,
    ParticipantDto,
    PlaceDto,
    PlacePollDto,
    ActivityDto,
    DebtDto,
} from '@/service/dtos/gathering-types';
import {
    StatusBadge,
    UserAvatar,
    SectionHeader,
    EmptyState,
    formatDate,
} from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

function ProfilePreviewModal({
    visible,
    participant,
    onClose,
}: {
    visible: boolean;
    participant: ParticipantDto | null;
    onClose: () => void;
}) {
    if (!participant) return null;
    const { user } = participant;
    const initials = user.name
        .split(' ')
        .map((w) => w[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);

    return (
        <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
            <View style={modalStyles.overlay}>
                <View style={modalStyles.dialog}>
                    <View style={modalStyles.avatarSection}>
                        {user.avatarUrl ? (
                            <Image source={{ uri: user.avatarUrl }} style={modalStyles.avatar} />
                        ) : (
                            <View style={modalStyles.avatarPlaceholder}>
                                <Text style={modalStyles.avatarInitials}>{initials}</Text>
                            </View>
                        )}
                    </View>
                    <Text style={modalStyles.name}>{user.name}</Text>
                    <Text style={modalStyles.username}>@{user.username}</Text>

                    <View style={modalStyles.infoCard}>
                        <View style={modalStyles.infoRow}>
                            <Text style={modalStyles.infoLabel}>Status</Text>
                            <Text style={modalStyles.infoValue}>{participant.participationStatus}</Text>
                        </View>
                        <View style={modalStyles.divider} />
                        <View style={modalStyles.infoRow}>
                            <Text style={modalStyles.infoLabel}>User ID</Text>
                            <Text style={modalStyles.infoValue}>#{user.id}</Text>
                        </View>
                    </View>

                    <TouchableOpacity style={modalStyles.closeBtn} onPress={onClose}>
                        <Text style={modalStyles.closeBtnText}>Close</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </Modal>
    );
}

const modalStyles = StyleSheet.create({
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
        alignItems: 'center',
    },
    avatarSection: { marginBottom: 16 },
    avatar: { width: 80, height: 80, borderRadius: 40 },
    avatarPlaceholder: {
        width: 80,
        height: 80,
        borderRadius: 40,
        backgroundColor: '#2D2A45',
        justifyContent: 'center',
        alignItems: 'center',
    },
    avatarInitials: { fontSize: 28, fontWeight: '700', color: '#B8AEDE' },
    name: { fontSize: 20, fontWeight: '800', color: '#F0EBE1', marginBottom: 2 },
    username: { fontSize: 14, color: '#6B7280', marginBottom: 16 },
    infoCard: {
        width: '100%',
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        padding: 16,
        borderWidth: 1,
        borderColor: '#252530',
        marginBottom: 16,
    },
    infoRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 6 },
    infoLabel: { fontSize: 13, color: '#6B7280' },
    infoValue: { fontSize: 13, color: '#F0EBE1', fontWeight: '600' },
    divider: { height: 1, backgroundColor: '#1F1F2E', marginVertical: 6 },
    closeBtn: {
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        paddingVertical: 12,
        paddingHorizontal: 32,
        borderWidth: 1,
        borderColor: '#252530',
    },
    closeBtnText: { color: '#6B7280', fontWeight: '600', fontSize: 14 },
});

function ParticipantRow({ p, onPress }: { p: ParticipantDto; onPress: () => void }) {
    const statusColor: Record<string, string> = {
        JOINED: '#34D399',
        INVITED: '#FBBF24',
        DECLINED: '#F87171',
        LEFT: '#9CA3AF',
        REMOVED: '#6B7280',
    };
    return (
        <TouchableOpacity style={rowStyles.row} onPress={onPress} activeOpacity={0.7}>
            <UserAvatar name={p.user.name} avatarUrl={p.user.avatarUrl} size={36} />
            <View style={rowStyles.info}>
                <Text style={rowStyles.name}>{p.user.name}</Text>
                <Text style={rowStyles.username}>@{p.user.username}</Text>
            </View>
            <View style={[rowStyles.statusDot, { backgroundColor: statusColor[p.participationStatus] ?? '#6B7280' }]} />
        </TouchableOpacity>
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
        FREE: '🆓', CHEAP: '💰', MODERATE: '💰💰', EXPENSIVE: '💰💰💰', LUXURY: '💎',
    };
    const typeEmoji: Record<string, string> = {
        CAFE: '☕', RESTAURANT: '🍽️', PARK: '🌳', OTHER: '📍',
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
        flexDirection: 'row', alignItems: 'center', backgroundColor: '#0B0B0F',
        borderRadius: 12, padding: 12, gap: 10, borderWidth: 1, borderColor: '#252530', marginBottom: 8,
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
    row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#1F1F2E' },
    label: { fontSize: 14, color: '#6B7280' },
    value: { fontSize: 14, color: '#F0EBE1', fontWeight: '500', maxWidth: '55%', textAlign: 'right' },
});

function getUserName(userId: number, participants: ParticipantDto[] | null): string {
    const p = participants?.find((pp) => pp.user.id === userId);
    return p?.user.name ?? `User #${userId}`;
}

interface SplitEntry {
    userId: number;
    userName: string;
    amount: string;
    isOwn: boolean;
}

function AddExpenseModal({
    visible,
    onClose,
    onSubmit,
    participants,
    currentUserId,
    currentUserName,
}: {
    visible: boolean;
    onClose: () => void;
    onSubmit: (data: { description: string; totalAmount: number; paidByUserId: number; splits: { userId: number; amountOwed: number }[] }) => void;
    participants: ParticipantDto[];
    currentUserId: number;
    currentUserName: string;
}) {
    const [description, setDescription] = useState('');
    const [totalAmount, setTotalAmount] = useState('');
    const [ownShare, setOwnShare] = useState('');
    const [otherSplits, setOtherSplits] = useState<SplitEntry[]>([]);
    const [showUserPicker, setShowUserPicker] = useState(false);

    const joinedParticipants = participants.filter(
        (p) => p.participationStatus === 'JOINED' && p.user.id !== currentUserId
    );

    const availableUsers = joinedParticipants.filter(
        (p) => !otherSplits.some((s) => s.userId === p.user.id)
    );

    const addSplit = (p: ParticipantDto) => {
        setOtherSplits((prev) => [
            ...prev,
            { userId: p.user.id, userName: p.user.name, amount: '', isOwn: false },
        ]);
        setShowUserPicker(false);
    };

    const updateSplitAmount = (idx: number, val: string) => {
        setOtherSplits((prev) => prev.map((s, i) => (i === idx ? { ...s, amount: val } : s)));
    };

    const removeSplit = (idx: number) => {
        setOtherSplits((prev) => prev.filter((_, i) => i !== idx));
    };

    const resetForm = () => {
        setDescription('');
        setTotalAmount('');
        setOwnShare('');
        setOtherSplits([]);
    };

    const handleSubmit = () => {
        const total = parseFloat(totalAmount);
        if (!description.trim()) {
            Alert.alert('Validation', 'Description is required.');
            return;
        }
        if (isNaN(total) || total <= 0) {
            Alert.alert('Validation', 'Enter a valid total amount.');
            return;
        }
        const own = parseFloat(ownShare);
        if (isNaN(own) || own < 0) {
            Alert.alert('Validation', 'Enter a valid amount for your share.');
            return;
        }

        const splits: { userId: number; amountOwed: number }[] = [
            { userId: currentUserId, amountOwed: own },
        ];

        for (const s of otherSplits) {
            const amt = parseFloat(s.amount);
            if (isNaN(amt) || amt <= 0) {
                Alert.alert('Validation', `Enter a valid amount for ${s.userName}.`);
                return;
            }
            splits.push({ userId: s.userId, amountOwed: amt });
        }

        const splitsTotal = splits.reduce((sum, s) => sum + s.amountOwed, 0);
        if (Math.abs(splitsTotal - total) > 0.01) {
            Alert.alert(
                'Validation',
                `Splits total ($${splitsTotal.toFixed(2)}) doesn't match the expense total ($${total.toFixed(2)}).`
            );
            return;
        }

        onSubmit({
            description: description.trim(),
            totalAmount: total,
            paidByUserId: currentUserId,
            splits,
        });
        resetForm();
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    return (
        <Modal visible={visible} transparent animationType="fade" onRequestClose={handleClose}>
            <KeyboardAvoidingView
                style={emStyles.overlay}
                behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            >
                <ScrollView
                    contentContainerStyle={emStyles.scrollContent}
                    keyboardShouldPersistTaps="handled"
                    showsVerticalScrollIndicator={false}
                >
                    <View style={emStyles.dialog}>
                        <Text style={emStyles.title}>Add Expense</Text>

                        {/* Description */}
                        <View style={emStyles.field}>
                            <Text style={emStyles.label}>DESCRIPTION</Text>
                            <TextInput
                                style={emStyles.input}
                                value={description}
                                onChangeText={setDescription}
                                placeholder="What was it for?"
                                placeholderTextColor="#6B7280"
                                maxLength={100}
                            />
                        </View>

                        {/* Total amount */}
                        <View style={emStyles.field}>
                            <Text style={emStyles.label}>TOTAL AMOUNT YOU PAID</Text>
                            <TextInput
                                style={emStyles.input}
                                value={totalAmount}
                                onChangeText={setTotalAmount}
                                placeholder="0.00"
                                placeholderTextColor="#6B7280"
                                keyboardType="decimal-pad"
                            />
                        </View>

                        {/* Splits header */}
                        <Text style={emStyles.splitsHeader}>SPLITS</Text>

                        {/* Own share */}
                        <View style={emStyles.splitRow}>
                            <View style={emStyles.splitUser}>
                                <View style={emStyles.youBadge}>
                                    <Text style={emStyles.youBadgeText}>You</Text>
                                </View>
                                <Text style={emStyles.splitName} numberOfLines={1}>
                                    {currentUserName}
                                </Text>
                            </View>
                            <TextInput
                                style={emStyles.splitInput}
                                value={ownShare}
                                onChangeText={setOwnShare}
                                placeholder="0.00"
                                placeholderTextColor="#6B7280"
                                keyboardType="decimal-pad"
                            />
                        </View>

                        {/* Other splits */}
                        {otherSplits.map((split, idx) => (
                            <View key={split.userId} style={emStyles.splitRow}>
                                <View style={emStyles.splitUser}>
                                    <Text style={emStyles.splitName} numberOfLines={1}>
                                        {split.userName}
                                    </Text>
                                </View>
                                <TextInput
                                    style={emStyles.splitInput}
                                    value={split.amount}
                                    onChangeText={(v) => updateSplitAmount(idx, v)}
                                    placeholder="0.00"
                                    placeholderTextColor="#6B7280"
                                    keyboardType="decimal-pad"
                                />
                                <TouchableOpacity onPress={() => removeSplit(idx)} style={emStyles.removeBtn}>
                                    <Text style={emStyles.removeBtnText}>✕</Text>
                                </TouchableOpacity>
                            </View>
                        ))}

                        {/* Add split button */}
                        {availableUsers.length > 0 && (
                            <TouchableOpacity
                                style={emStyles.addSplitBtn}
                                onPress={() => setShowUserPicker(true)}
                                activeOpacity={0.7}
                            >
                                <Text style={emStyles.addSplitBtnText}>+ Add person</Text>
                            </TouchableOpacity>
                        )}

                        {/* User picker inline */}
                        {showUserPicker && (
                            <View style={emStyles.userPicker}>
                                {availableUsers.map((p) => (
                                    <TouchableOpacity
                                        key={p.user.id}
                                        style={emStyles.userPickerRow}
                                        onPress={() => addSplit(p)}
                                        activeOpacity={0.7}
                                    >
                                        <UserAvatar name={p.user.name} avatarUrl={p.user.avatarUrl} size={28} />
                                        <Text style={emStyles.userPickerName}>{p.user.name}</Text>
                                    </TouchableOpacity>
                                ))}
                                <TouchableOpacity
                                    style={emStyles.userPickerCancel}
                                    onPress={() => setShowUserPicker(false)}
                                >
                                    <Text style={{ color: '#6B7280', fontSize: 13 }}>Cancel</Text>
                                </TouchableOpacity>
                            </View>
                        )}

                        {/* Actions */}
                        <View style={emStyles.actions}>
                            <TouchableOpacity style={emStyles.cancelBtn} onPress={handleClose}>
                                <Text style={emStyles.cancelBtnText}>Cancel</Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={emStyles.confirmBtn} onPress={handleSubmit}>
                                <Text style={emStyles.confirmBtnText}>Add Expense</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </ScrollView>
            </KeyboardAvoidingView>
        </Modal>
    );
}

const emStyles = StyleSheet.create({
    overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.7)' },
    scrollContent: { flexGrow: 1, justifyContent: 'center', padding: 24 },
    dialog: {
        backgroundColor: '#16161D', borderRadius: 20, padding: 24,
        borderWidth: 1, borderColor: '#1F1F2E',
    },
    title: { fontSize: 20, fontWeight: '800', color: '#F0EBE1', marginBottom: 20, textAlign: 'center' },
    field: { marginBottom: 16 },
    label: { fontSize: 10, fontWeight: '700', color: '#4B5563', letterSpacing: 1.5, marginBottom: 6 },
    input: {
        backgroundColor: '#0B0B0F', borderWidth: 1, borderColor: '#252530', borderRadius: 12,
        paddingHorizontal: 16, paddingVertical: 12, color: '#F0EBE1', fontSize: 16,
    },
    splitsHeader: {
        fontSize: 10, fontWeight: '700', color: '#4B5563', letterSpacing: 1.5,
        marginBottom: 12, marginTop: 4,
    },
    splitRow: {
        flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10,
    },
    splitUser: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 8 },
    youBadge: {
        backgroundColor: '#2D2A45', borderRadius: 6, paddingHorizontal: 8, paddingVertical: 2,
    },
    youBadgeText: { color: primaryColor, fontSize: 11, fontWeight: '700' },
    splitName: { fontSize: 14, color: '#F0EBE1', fontWeight: '500', flexShrink: 1 },
    splitInput: {
        backgroundColor: '#0B0B0F', borderWidth: 1, borderColor: '#252530', borderRadius: 10,
        paddingHorizontal: 12, paddingVertical: 10, color: '#F0EBE1', fontSize: 15,
        width: 90, textAlign: 'right', fontWeight: '600',
    },
    removeBtn: {
        width: 28, height: 28, borderRadius: 14, backgroundColor: '#2A1515',
        justifyContent: 'center', alignItems: 'center',
    },
    removeBtnText: { color: '#F87171', fontSize: 12, fontWeight: '700' },
    addSplitBtn: {
        borderWidth: 1, borderColor: '#252530', borderStyle: 'dashed', borderRadius: 12,
        paddingVertical: 12, alignItems: 'center', marginBottom: 12, marginTop: 4,
    },
    addSplitBtnText: { color: primaryColor, fontWeight: '700', fontSize: 14 },
    userPicker: {
        backgroundColor: '#0B0B0F', borderRadius: 12, borderWidth: 1, borderColor: '#252530',
        padding: 8, marginBottom: 12,
    },
    userPickerRow: {
        flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 8, paddingHorizontal: 8,
    },
    userPickerName: { fontSize: 14, color: '#F0EBE1', fontWeight: '500' },
    userPickerCancel: { paddingVertical: 8, alignItems: 'center' },
    actions: { flexDirection: 'row', gap: 12, marginTop: 8 },
    cancelBtn: {
        flex: 1, backgroundColor: '#0B0B0F', borderRadius: 12, paddingVertical: 14,
        alignItems: 'center', borderWidth: 1, borderColor: '#252530',
    },
    cancelBtnText: { color: '#6B7280', fontWeight: '600', fontSize: 15 },
    confirmBtn: { flex: 1, backgroundColor: primaryColor, borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
    confirmBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
});

function buildMapsQuery(place: PlaceDto): string {
    const parts = [place.name, place.address].filter(Boolean).join(' ');
    return encodeURIComponent(parts);
}

function MapModal({
    visible,
    place,
    onClose,
}: {
    visible: boolean;
    place: PlaceDto | null;
    onClose: () => void;
}) {
    if (!place) return null;

    const query = buildMapsQuery(place);
    const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${query}`;

    const handleOpenExternal = () => {
        Linking.openURL(mapsUrl);
    };

    return (
        <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
            <View style={mapStyles.overlay}>
                <View style={mapStyles.container}>
                    <View style={mapStyles.header}>
                        <View style={{ flex: 1 }}>
                            <Text style={mapStyles.title} numberOfLines={1}>{place.name}</Text>
                            {place.address && (
                                <Text style={mapStyles.address} numberOfLines={1}>{place.address}</Text>
                            )}
                        </View>
                        <TouchableOpacity onPress={onClose} style={mapStyles.closeBtn}>
                            <Text style={mapStyles.closeBtnText}>X</Text>
                        </TouchableOpacity>
                    </View>

                    <View style={mapStyles.webviewContainer}>
                        <WebView
                            source={{ uri: mapsUrl }}
                            style={mapStyles.webview}
                            javaScriptEnabled
                            domStorageEnabled
                            scalesPageToFit
                            startInLoadingState
                            renderLoading={() => (
                                <View style={mapStyles.loading}>
                                    <ActivityIndicator color={primaryColor} size="large" />
                                </View>
                            )}
                        />
                    </View>

                    <TouchableOpacity
                        style={mapStyles.openBtn}
                        onPress={handleOpenExternal}
                        activeOpacity={0.85}
                    >
                        <Text style={mapStyles.openBtnText}>Open in Google Maps</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </Modal>
    );
}

const mapStyles = StyleSheet.create({
    overlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.8)',
        justifyContent: 'flex-end',
    },
    container: {
        backgroundColor: '#16161D',
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        borderBottomWidth: 0,
        overflow: 'hidden',
        height: '75%',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
        gap: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#1F1F2E',
    },
    title: { fontSize: 16, fontWeight: '700', color: '#F0EBE1' },
    address: { fontSize: 13, color: '#6B7280', marginTop: 2 },
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
    closeBtnText: { color: '#6B7280', fontWeight: '700', fontSize: 14 },
    webviewContainer: { flex: 1 },
    webview: { flex: 1 },
    loading: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#0B0B0F',
    },
    openBtn: {
        backgroundColor: primaryColor,
        paddingVertical: 16,
        alignItems: 'center',
        margin: 16,
        borderRadius: 12,
    },
    openBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
});

function VotingCard({
    poll,
    gatheringId,
    onVoteSubmitted,
}: {
    poll: PlacePollDto;
    gatheringId: number;
    onVoteSubmitted: () => void;
}) {
    const [selectedPlaceIds, setSelectedPlaceIds] = useState<number[]>(poll.myVotedPlaceIds);
    const [submitting, setSubmitting] = useState(false);
    const [timeLeft, setTimeLeft] = useState('');
    const [mapPlace, setMapPlace] = useState<PlaceDto | null>(null);

    useEffect(() => {
        const updateTimer = () => {
            const now = Date.now();
            const end = new Date(poll.endsAt).getTime();
            const diff = end - now;
            if (diff <= 0) {
                setTimeLeft('Ended');
                return;
            }
            const hours = Math.floor(diff / 3600000);
            const mins = Math.floor((diff % 3600000) / 60000);
            const secs = Math.floor((diff % 60000) / 1000);
            if (hours > 0) {
                setTimeLeft(`${hours}h ${mins}m ${secs}s`);
            } else {
                setTimeLeft(`${mins}m ${secs}s`);
            }
        };
        updateTimer();
        const interval = setInterval(updateTimer, 1000);
        return () => clearInterval(interval);
    }, [poll.endsAt]);

    const togglePlace = (placeId: number) => {
        setSelectedPlaceIds((prev) =>
            prev.includes(placeId) ? prev.filter((id) => id !== placeId) : [...prev, placeId]
        );
    };

    const handleSubmitVote = async () => {
        if (selectedPlaceIds.length === 0) {
            Alert.alert('Vote', 'Select at least one place.');
            return;
        }
        setSubmitting(true);
        try {
            await gatheringService.castVote(gatheringId, selectedPlaceIds);
            onVoteSubmitted();
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to submit vote.');
        } finally {
            setSubmitting(false);
        }
    };

    const isActive = poll.status === 'ACTIVE' && timeLeft !== 'Ended';
    const sortedPlaces = [...poll.places].sort((a, b) => b.voteCount - a.voteCount);

    return (
        <View style={voteStyles.container}>
            <View style={voteStyles.header}>
                <SectionHeader title={isActive ? 'VOTE FOR A PLACE' : 'VOTE RESULTS'} />
                <View style={[voteStyles.timerBadge, !isActive && { backgroundColor: '#2A1515' }]}>
                    <Text style={[voteStyles.timerText, !isActive && { color: '#F87171' }]}>
                        {isActive ? timeLeft : 'Ended'}
                    </Text>
                </View>
            </View>

            {sortedPlaces.map(({ place, voteCount }) => {
                const isSelected = selectedPlaceIds.includes(place.id);
                return (
                    <TouchableOpacity
                        key={place.id}
                        style={[
                            voteStyles.option,
                            isSelected && voteStyles.optionSelected,
                        ]}
                        onPress={() => isActive && togglePlace(place.id)}
                        onLongPress={() => setMapPlace(place)}
                        activeOpacity={isActive ? 0.7 : 1}
                    >
                        <View style={voteStyles.optionContent}>
                            <View style={{ flex: 1 }}>
                                <Text style={voteStyles.optionName}>{place.name}</Text>
                                {place.address && (
                                    <Text style={voteStyles.optionAddress} numberOfLines={1}>
                                        {place.address}
                                    </Text>
                                )}
                            </View>
                            <View style={voteStyles.voteBadge}>
                                <Text style={voteStyles.voteCount}>{voteCount}</Text>
                            </View>
                            {isActive && (
                                <View style={[voteStyles.checkbox, isSelected && voteStyles.checkboxChecked]}>
                                    {isSelected && <Text style={voteStyles.checkmark}>{'  '}</Text>}
                                </View>
                            )}
                        </View>
                    </TouchableOpacity>
                );
            })}

            {isActive && (
                <TouchableOpacity
                    style={[voteStyles.submitBtn, submitting && { opacity: 0.6 }]}
                    onPress={handleSubmitVote}
                    disabled={submitting}
                    activeOpacity={0.85}
                >
                    {submitting ? (
                        <ActivityIndicator color="#0B0B0F" size="small" />
                    ) : (
                        <Text style={voteStyles.submitBtnText}>
                            {poll.myVotedPlaceIds.length > 0 ? 'Update Vote' : 'Submit Vote'}
                        </Text>
                    )}
                </TouchableOpacity>
            )}

            <Text style={voteStyles.hint}>Long press a place to view on map</Text>

            <MapModal
                visible={!!mapPlace}
                place={mapPlace}
                onClose={() => setMapPlace(null)}
            />
        </View>
    );
}

const voteStyles = StyleSheet.create({
    container: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 20,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 16,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 4,
    },
    timerBadge: {
        backgroundColor: '#2D2A45',
        borderRadius: 8,
        paddingHorizontal: 10,
        paddingVertical: 4,
    },
    timerText: { color: primaryColor, fontSize: 12, fontWeight: '700' },
    option: {
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        padding: 14,
        marginTop: 8,
        borderWidth: 1,
        borderColor: '#252530',
    },
    optionSelected: {
        borderColor: primaryColor,
        backgroundColor: '#1A1828',
    },
    optionContent: { flexDirection: 'row', alignItems: 'center', gap: 12 },
    optionName: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    optionAddress: { fontSize: 12, color: '#6B7280', marginTop: 2 },
    voteBadge: {
        backgroundColor: '#2D2A45',
        borderRadius: 8,
        paddingHorizontal: 10,
        paddingVertical: 4,
        minWidth: 32,
        alignItems: 'center',
    },
    voteCount: { color: primaryColor, fontSize: 13, fontWeight: '700' },
    checkbox: {
        width: 22,
        height: 22,
        borderRadius: 6,
        borderWidth: 2,
        borderColor: '#4B5563',
        justifyContent: 'center',
        alignItems: 'center',
    },
    checkboxChecked: {
        borderColor: primaryColor,
        backgroundColor: primaryColor,
    },
    checkmark: { color: '#0B0B0F', fontSize: 13, fontWeight: '800' },
    submitBtn: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
        marginTop: 16,
    },
    submitBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
    hint: { color: '#4B5563', fontSize: 11, textAlign: 'center', marginTop: 12 },
});

function InviteParticipantModal({
    visible,
    onClose,
    gatheringId,
    existingParticipantIds,
    onInvited,
}: {
    visible: boolean;
    onClose: () => void;
    gatheringId: number;
    existingParticipantIds: number[];
    onInvited: () => void;
}) {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<UserSearchDto[]>([]);
    const [searching, setSearching] = useState(false);
    const [inviting, setInviting] = useState<number | null>(null);
    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const handleSearch = (text: string) => {
        setQuery(text);
        if (debounceRef.current) clearTimeout(debounceRef.current);
        if (!text.trim()) {
            setResults([]);
            return;
        }
        debounceRef.current = setTimeout(async () => {
            setSearching(true);
            try {
                const users = await userSearchService.searchUsers(text);
                setResults(users.filter((u) => !existingParticipantIds.includes(u.id)));
            } catch {
                setResults([]);
            } finally {
                setSearching(false);
            }
        }, 400);
    };

    const handleInvite = async (userId: number) => {
        setInviting(userId);
        try {
            await gatheringService.inviteUser(gatheringId, userId);
            setResults((prev) => prev.filter((u) => u.id !== userId));
            onInvited();
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to invite user.');
        } finally {
            setInviting(null);
        }
    };

    const handleClose = () => {
        setQuery('');
        setResults([]);
        onClose();
    };

    return (
        <Modal visible={visible} transparent animationType="fade" onRequestClose={handleClose}>
            <KeyboardAvoidingView
                style={inviteStyles.overlay}
                behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            >
                <View style={inviteStyles.dialog}>
                    <Text style={inviteStyles.title}>Invite Participant</Text>
                    <TextInput
                        style={inviteStyles.input}
                        value={query}
                        onChangeText={handleSearch}
                        placeholder="Search by name or username..."
                        placeholderTextColor="#6B7280"
                        autoFocus
                    />
                    {searching && (
                        <ActivityIndicator color={primaryColor} size="small" style={{ marginVertical: 12 }} />
                    )}
                    {results.length > 0 && (
                        <View style={inviteStyles.resultsList}>
                            {results.map((u) => {
                                const initials = u.name
                                    .split(' ')
                                    .map((w) => w[0])
                                    .join('')
                                    .toUpperCase()
                                    .slice(0, 2);
                                return (
                                    <View key={u.id} style={inviteStyles.resultRow}>
                                        {u.avatarUrl ? (
                                            <Image source={{ uri: u.avatarUrl }} style={inviteStyles.resultAvatar} />
                                        ) : (
                                            <View style={inviteStyles.resultAvatarPlaceholder}>
                                                <Text style={inviteStyles.resultAvatarText}>{initials}</Text>
                                            </View>
                                        )}
                                        <View style={{ flex: 1 }}>
                                            <Text style={inviteStyles.resultName}>{u.name}</Text>
                                            <Text style={inviteStyles.resultUsername}>@{u.username}</Text>
                                        </View>
                                        <TouchableOpacity
                                            style={inviteStyles.inviteBtn}
                                            onPress={() => handleInvite(u.id)}
                                            disabled={inviting === u.id}
                                            activeOpacity={0.8}
                                        >
                                            {inviting === u.id ? (
                                                <ActivityIndicator color="#0B0B0F" size="small" />
                                            ) : (
                                                <Text style={inviteStyles.inviteBtnText}>Invite</Text>
                                            )}
                                        </TouchableOpacity>
                                    </View>
                                );
                            })}
                        </View>
                    )}
                    {!searching && query.trim().length > 0 && results.length === 0 && (
                        <Text style={inviteStyles.noResults}>No users found</Text>
                    )}
                    <TouchableOpacity style={inviteStyles.closeBtn} onPress={handleClose}>
                        <Text style={inviteStyles.closeBtnText}>Close</Text>
                    </TouchableOpacity>
                </View>
            </KeyboardAvoidingView>
        </Modal>
    );
}

const inviteStyles = StyleSheet.create({
    overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', justifyContent: 'center', padding: 24 },
    dialog: {
        backgroundColor: '#16161D', borderRadius: 20, padding: 24,
        borderWidth: 1, borderColor: '#1F1F2E', maxHeight: '80%',
    },
    title: { fontSize: 20, fontWeight: '800', color: '#F0EBE1', marginBottom: 16, textAlign: 'center' },
    input: {
        backgroundColor: '#0B0B0F', borderWidth: 1, borderColor: '#252530', borderRadius: 12,
        paddingHorizontal: 16, paddingVertical: 12, color: '#F0EBE1', fontSize: 16,
    },
    resultsList: { marginTop: 12, maxHeight: 300 },
    resultRow: {
        flexDirection: 'row', alignItems: 'center', gap: 12,
        paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#1F1F2E',
    },
    resultAvatar: { width: 36, height: 36, borderRadius: 18 },
    resultAvatarPlaceholder: {
        width: 36, height: 36, borderRadius: 18, backgroundColor: '#2D2A45',
        justifyContent: 'center', alignItems: 'center',
    },
    resultAvatarText: { fontSize: 13, fontWeight: '700', color: '#B8AEDE' },
    resultName: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    resultUsername: { fontSize: 12, color: '#6B7280', marginTop: 1 },
    inviteBtn: {
        backgroundColor: primaryColor, borderRadius: 10,
        paddingHorizontal: 16, paddingVertical: 8,
    },
    inviteBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 13 },
    noResults: { color: '#6B7280', fontSize: 14, textAlign: 'center', marginTop: 16 },
    closeBtn: {
        marginTop: 16, paddingVertical: 12, alignItems: 'center',
        backgroundColor: '#0B0B0F', borderRadius: 12, borderWidth: 1, borderColor: '#252530',
    },
    closeBtnText: { color: '#6B7280', fontWeight: '600', fontSize: 14 },
});

export default function GatheringDetailsScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const router = useRouter();
    const { user } = useAuth();
    const [gathering, setGathering] = useState<GatheringDetailsDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [activities, setActivities] = useState<ActivityDto[]>([]);
    const [debts, setDebts] = useState<DebtDto[]>([]);
    const [payModalDebt, setPayModalDebt] = useState<DebtDto | null>(null);
    const [payAmount, setPayAmount] = useState('');
    const [paying, setPaying] = useState(false);
    const [previewParticipant, setPreviewParticipant] = useState<ParticipantDto | null>(null);
    const [showAddExpense, setShowAddExpense] = useState(false);
    const [showInviteModal, setShowInviteModal] = useState(false);

    const load = useCallback(async () => {
        if (!id) return;
        try {
            const data = await gatheringService.getGatheringDetails(Number(id));

            if (!data.hasSubmittedResponse && data.status !== 'CANCELLED') {
                router.replace({
                    pathname: '/gathering/pick-preferences',
                    params: { gatheringId: id },
                });
                return;
            }

            setGathering(data);

            const [activitiesData, debtsData] = await Promise.all([
                gatheringService.getActivities(Number(id)),
                gatheringService.getDebts(Number(id)),
            ]);
            setActivities(activitiesData);
            setDebts(debtsData);
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

    const handlePay = async () => {
        if (!payModalDebt || !gathering) return;
        const amount = parseFloat(payAmount);
        if (isNaN(amount) || amount <= 0) {
            Alert.alert('Validation', 'Please enter a valid amount.');
            return;
        }
        setPaying(true);
        try {
            await gatheringService.createPayment(gathering.id, {
                toUserId: payModalDebt.toUserId,
                amount,
            });
            setPayModalDebt(null);
            setPayAmount('');
            onRefresh();
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Payment failed.');
        } finally {
            setPaying(false);
        }
    };

    const handleAddExpense = async (data: {
        description: string;
        totalAmount: number;
        paidByUserId: number;
        splits: { userId: number; amountOwed: number }[];
    }) => {
        if (!gathering) return;
        try {
            await gatheringService.createExpense(gathering.id, {
                paidByUserId: data.paidByUserId,
                totalAmount: data.totalAmount,
                description: data.description,
                splits: data.splits,
            });
            setShowAddExpense(false);
            onRefresh();
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to add expense.');
        }
    };

    const handleDeletePayment = (paymentId: number) => {
        if (!gathering) return;
        Alert.alert('Cancel payment', 'Are you sure? This will restore the debt.', [
            { text: 'Keep', style: 'cancel' },
            {
                text: 'Cancel payment',
                style: 'destructive',
                onPress: async () => {
                    try {
                        await gatheringService.deletePayment(gathering.id, paymentId);
                        onRefresh();
                    } catch (e: any) {
                        Alert.alert('Error', e.message ?? 'Failed to cancel payment.');
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
    const hasPoll = gathering.activePoll !== null;
    const canShowAiSuggestions = isCreator && (gathering.status === 'OPEN' || gathering.status === 'DRAFT') && !hasPoll;

    const finalizedTimePassed = gathering.finalizedTime
        ? new Date(gathering.finalizedTime).getTime() < Date.now()
        : false;
    const showExpenses = finalizedTimePassed;

    const myDebts = debts.filter((d) => d.fromUserId === user?.id && d.amount > 0);

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
                <View style={styles.topBarRight}>
                    {gathering.hasSubmittedResponse && gathering.status !== 'CANCELLED' && !hasPoll && (
                        <TouchableOpacity
                            onPress={() =>
                                router.push({
                                    pathname: '/gathering/pick-preferences',
                                    params: { gatheringId: id, edit: '1' },
                                })
                            }
                            style={styles.editPrefsBtn}
                        >
                            <Text style={styles.editPrefsBtnText}>Edit Prefs</Text>
                        </TouchableOpacity>
                    )}
                    {canManage && (
                        <TouchableOpacity
                            onPress={() => router.push({ pathname: '/gathering/manage/[id]', params: { id } })}
                            style={styles.manageBtn}
                        >
                            <Text style={styles.manageBtnText}>Manage</Text>
                        </TouchableOpacity>
                    )}
                </View>
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
                    <View style={styles.sectionRow}>
                        <SectionHeader title={`PARTICIPANTS (${gathering.participants.length})`} />
                        {isCreator && (gathering.status === 'OPEN' || gathering.status === 'DRAFT') && (
                            <TouchableOpacity
                                onPress={() => setShowInviteModal(true)}
                                style={styles.addExpenseBtn}
                                activeOpacity={0.8}
                            >
                                <Text style={styles.addExpenseBtnText}>+ Invite</Text>
                            </TouchableOpacity>
                        )}
                    </View>
                    {gathering.participants.map((p) => (
                        <ParticipantRow
                            key={p.user.id}
                            p={p}
                            onPress={() => setPreviewParticipant(p)}
                        />
                    ))}
                </View>
            )}

            {/* AI Suggestions button (creator only, no poll yet) */}
            {canShowAiSuggestions && (
                <TouchableOpacity
                    style={styles.aiSuggestionsBtn}
                    onPress={() =>
                        router.push({
                            pathname: '/gathering/place-suggestions',
                            params: { gatheringId: id },
                        })
                    }
                    activeOpacity={0.8}
                >
                    <Text style={styles.aiSuggestionsBtnText}>
                        {gathering.suggestedPlaces && gathering.suggestedPlaces.length > 0
                            ? 'Review AI Suggestions'
                            : 'Generate AI Suggestions'}
                    </Text>
                </TouchableOpacity>
            )}

            {/* Suggested places (when no poll) */}
            {!hasPoll && gathering.suggestedPlaces && gathering.suggestedPlaces.length > 0 && (
                <View style={styles.card}>
                    <SectionHeader title="SUGGESTED PLACES" />
                    {gathering.suggestedPlaces.map((place) => (
                        <PlaceCard key={place.id} place={place} />
                    ))}
                </View>
            )}

            {/* Place vote */}
            {gathering.activePoll && (
                <VotingCard
                    poll={gathering.activePoll}
                    gatheringId={gathering.id}
                    onVoteSubmitted={onRefresh}
                />
            )}

            {/* Expenses Section — only when finalized time has passed */}
            {showExpenses && (
                <>
                    {/* Activity feed */}
                    <View style={styles.card}>
                        <View style={styles.sectionRow}>
                            <SectionHeader title="EXPENSES & PAYMENTS" />
                            <TouchableOpacity
                                onPress={() => setShowAddExpense(true)}
                                style={styles.addExpenseBtn}
                                activeOpacity={0.8}
                            >
                                <Text style={styles.addExpenseBtnText}>+ Add</Text>
                            </TouchableOpacity>
                        </View>
                        {activities.length === 0 ? (
                            <Text style={styles.emptyText}>No expenses or payments yet.</Text>
                        ) : (
                            activities.map((activity) => {
                                if (activity.type === 'EXPENSE') {
                                    return (
                                        <View key={`expense-${activity.id}`} style={styles.activityRow}>
                                            <View style={styles.activityIcon}>
                                                <Text style={{ fontSize: 18 }}>🧾</Text>
                                            </View>
                                            <View style={styles.activityInfo}>
                                                <Text style={styles.activityTitle}>
                                                    {activity.description ?? 'Expense'}
                                                </Text>
                                                <Text style={styles.activitySub}>
                                                    Paid by {getUserName(activity.paidByUserId, gathering.participants)}
                                                </Text>
                                            </View>
                                            <Text style={styles.activityAmount}>
                                                ${Number(activity.totalAmount).toFixed(2)}
                                            </Text>
                                        </View>
                                    );
                                } else {
                                    return (
                                        <View key={`payment-${activity.id}`} style={styles.activityRow}>
                                            <View style={styles.activityIcon}>
                                                <Text style={{ fontSize: 18 }}>💸</Text>
                                            </View>
                                            <View style={styles.activityInfo}>
                                                <Text style={styles.activityTitle}>Payment</Text>
                                                <Text style={styles.activitySub}>
                                                    {getUserName(activity.fromUserId, gathering.participants)} → {getUserName(activity.toUserId, gathering.participants)}
                                                </Text>
                                            </View>
                                            <Text style={[styles.activityAmount, { color: '#34D399' }]}>
                                                ${Number(activity.amount).toFixed(2)}
                                            </Text>
                                            {activity.fromUserId === user?.id && (
                                                <TouchableOpacity
                                                    onPress={() => handleDeletePayment(activity.id)}
                                                    style={styles.undoBtn}
                                                    activeOpacity={0.7}
                                                >
                                                    <Text style={styles.undoBtnText}>Undo</Text>
                                                </TouchableOpacity>
                                            )}
                                        </View>
                                    );
                                }
                            })
                        )}
                    </View>

                    {/* Debts - what you owe */}
                    {myDebts.length > 0 && (
                        <View style={styles.card}>
                            <SectionHeader title="YOU OWE" />
                            {myDebts.map((debt, i) => (
                                <View key={i} style={styles.debtRow}>
                                    <View style={styles.debtInfo}>
                                        <Text style={styles.debtName}>
                                            {getUserName(debt.toUserId, gathering.participants)}
                                        </Text>
                                        <Text style={styles.debtAmount}>
                                            ${Number(debt.amount).toFixed(2)}
                                        </Text>
                                    </View>
                                    <TouchableOpacity
                                        style={styles.payBtn}
                                        onPress={() => {
                                            setPayModalDebt(debt);
                                            setPayAmount(String(Number(debt.amount).toFixed(2)));
                                        }}
                                        activeOpacity={0.8}
                                    >
                                        <Text style={styles.payBtnText}>Pay</Text>
                                    </TouchableOpacity>
                                </View>
                            ))}
                        </View>
                    )}

                    {/* All debts overview */}
                    {debts.length > 0 && (
                        <View style={styles.card}>
                            <SectionHeader title="ALL DEBTS" />
                            {debts.map((debt, i) => (
                                <View key={i} style={styles.allDebtRow}>
                                    <Text style={styles.allDebtText}>
                                        {getUserName(debt.fromUserId, gathering.participants)} → {getUserName(debt.toUserId, gathering.participants)}
                                    </Text>
                                    <Text style={styles.allDebtAmount}>
                                        ${Number(debt.amount).toFixed(2)}
                                    </Text>
                                </View>
                            ))}
                        </View>
                    )}
                </>
            )}

            {/* Actions */}
            {canLeave && (
                <TouchableOpacity style={styles.leaveBtn} onPress={handleLeave} activeOpacity={0.8}>
                    <Text style={styles.leaveBtnText}>Leave gathering</Text>
                </TouchableOpacity>
            )}

            {/* Profile preview modal */}
            <ProfilePreviewModal
                visible={!!previewParticipant}
                participant={previewParticipant}
                onClose={() => setPreviewParticipant(null)}
            />

            {/* Pay modal */}
            <Modal
                visible={!!payModalDebt}
                transparent
                animationType="fade"
                onRequestClose={() => setPayModalDebt(null)}
            >
                <View style={styles.payOverlay}>
                    <View style={styles.payDialog}>
                        <Text style={styles.payDialogTitle}>Make Payment</Text>
                        {payModalDebt && (
                            <Text style={styles.payDialogSub}>
                                To {getUserName(payModalDebt.toUserId, gathering.participants)}
                            </Text>
                        )}
                        <TextInput
                            style={styles.payInput}
                            value={payAmount}
                            onChangeText={setPayAmount}
                            keyboardType="decimal-pad"
                            placeholder="Amount"
                            placeholderTextColor="#6B7280"
                        />
                        <View style={styles.payActions}>
                            <TouchableOpacity
                                style={styles.payCancelBtn}
                                onPress={() => setPayModalDebt(null)}
                                disabled={paying}
                            >
                                <Text style={styles.payCancelBtnText}>Cancel</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[styles.payConfirmBtn, paying && { opacity: 0.6 }]}
                                onPress={handlePay}
                                disabled={paying}
                                activeOpacity={0.85}
                            >
                                {paying ? (
                                    <ActivityIndicator color="#0B0B0F" size="small" />
                                ) : (
                                    <Text style={styles.payConfirmBtnText}>Confirm</Text>
                                )}
                            </TouchableOpacity>
                        </View>
                    </View>
                </View>
            </Modal>

            {/* Add Expense modal */}
            {gathering.participants && (
                <AddExpenseModal
                    visible={showAddExpense}
                    onClose={() => setShowAddExpense(false)}
                    onSubmit={handleAddExpense}
                    participants={gathering.participants}
                    currentUserId={user?.id ?? 0}
                    currentUserName={user?.name ?? 'You'}
                />
            )}

            {/* Invite participant modal */}
            <InviteParticipantModal
                visible={showInviteModal}
                onClose={() => setShowInviteModal(false)}
                gatheringId={gathering.id}
                existingParticipantIds={gathering.participants?.map((p) => p.user.id) ?? []}
                onInvited={onRefresh}
            />
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
    topBarRight: { flexDirection: 'row', gap: 8 },
    backBtn: {},
    backText: { color: primaryColor, fontSize: 16, fontWeight: '600' },
    editPrefsBtn: {
        backgroundColor: '#1F1F2E',
        borderRadius: 12,
        paddingHorizontal: 14,
        paddingVertical: 8,
        borderWidth: 1,
        borderColor: '#252530',
    },
    editPrefsBtnText: { color: '#9CA3AF', fontWeight: '600', fontSize: 13 },
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
    sectionRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    addExpenseBtn: {
        backgroundColor: primaryColor,
        borderRadius: 10,
        paddingHorizontal: 14,
        paddingVertical: 6,
    },
    addExpenseBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 13 },
    emptyText: { color: '#6B7280', fontSize: 14, marginTop: 8 },
    activityRow: {
        flexDirection: 'row', alignItems: 'center', paddingVertical: 10,
        borderBottomWidth: 1, borderBottomColor: '#1F1F2E', gap: 12,
    },
    activityIcon: {
        width: 36, height: 36, borderRadius: 18, backgroundColor: '#0B0B0F',
        justifyContent: 'center', alignItems: 'center',
    },
    activityInfo: { flex: 1 },
    activityTitle: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    activitySub: { fontSize: 12, color: '#6B7280', marginTop: 1 },
    activityAmount: { fontSize: 15, fontWeight: '700', color: '#F0EBE1' },
    undoBtn: {
        backgroundColor: '#2A1515', borderRadius: 8, paddingHorizontal: 10, paddingVertical: 5,
        marginLeft: 8, borderWidth: 1, borderColor: '#3B1D1D',
    },
    undoBtnText: { color: '#F87171', fontSize: 12, fontWeight: '700' },
    debtRow: {
        flexDirection: 'row', alignItems: 'center', paddingVertical: 12,
        borderBottomWidth: 1, borderBottomColor: '#1F1F2E',
    },
    debtInfo: { flex: 1 },
    debtName: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    debtAmount: { fontSize: 13, color: '#F87171', fontWeight: '600', marginTop: 2 },
    payBtn: { backgroundColor: primaryColor, borderRadius: 10, paddingHorizontal: 20, paddingVertical: 8 },
    payBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 14 },
    allDebtRow: {
        flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8,
        borderBottomWidth: 1, borderBottomColor: '#1F1F2E',
    },
    allDebtText: { fontSize: 13, color: '#9CA3AF' },
    allDebtAmount: { fontSize: 13, color: '#F0EBE1', fontWeight: '600' },
    aiSuggestionsBtn: {
        backgroundColor: '#2D2A45',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginBottom: 16,
        borderWidth: 1,
        borderColor: primaryColor,
    },
    aiSuggestionsBtnText: { color: primaryColor, fontWeight: '800', fontSize: 15 },
    leaveBtn: {
        backgroundColor: '#16161D', borderRadius: 12, borderWidth: 1, borderColor: '#3B1D1D',
        paddingVertical: 16, alignItems: 'center', marginTop: 4,
    },
    leaveBtnText: { color: '#EF4444', fontWeight: '700', fontSize: 15 },
    payOverlay: {
        flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', justifyContent: 'center', alignItems: 'center', padding: 32,
    },
    payDialog: {
        backgroundColor: '#16161D', borderRadius: 20, padding: 28, width: '100%', borderWidth: 1, borderColor: '#1F1F2E',
    },
    payDialogTitle: { fontSize: 20, fontWeight: '800', color: '#F0EBE1', marginBottom: 4, textAlign: 'center' },
    payDialogSub: { fontSize: 14, color: '#6B7280', textAlign: 'center', marginBottom: 20 },
    payInput: {
        backgroundColor: '#0B0B0F', borderWidth: 1, borderColor: '#252530', borderRadius: 12,
        paddingHorizontal: 16, paddingVertical: 14, color: '#F0EBE1', fontSize: 18,
        textAlign: 'center', fontWeight: '700', marginBottom: 20,
    },
    payActions: { flexDirection: 'row', gap: 12 },
    payCancelBtn: {
        flex: 1, backgroundColor: '#0B0B0F', borderRadius: 12, paddingVertical: 14,
        alignItems: 'center', borderWidth: 1, borderColor: '#252530',
    },
    payCancelBtnText: { color: '#6B7280', fontWeight: '600', fontSize: 15 },
    payConfirmBtn: { flex: 1, backgroundColor: primaryColor, borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
    payConfirmBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
});
