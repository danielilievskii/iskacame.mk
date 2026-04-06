import React, { useCallback, useEffect, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    ActivityIndicator,
    Alert,
    Modal,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { gatheringService } from '@/service/gathering-service';
import type { PlaceDto } from '@/service/dtos/gathering-types';
import { primaryColor } from '@/constants/theme';

const DURATION_OPTIONS = [
    { label: '15 minutes', value: 15 },
    { label: '30 minutes', value: 30 },
    { label: '1 hour', value: 60 },
    { label: '2 hours', value: 120 },
    { label: '24 hours', value: 1440 },
];

function PlaceSuggestionCard({ place }: { place: PlaceDto }) {
    const levelEmoji: Record<string, string> = {
        FREE: '', BUDGET: '', CHEAP: '', MODERATE: '', EXPENSIVE: '', LUXURY: '',
    };
    return (
        <View style={cardStyles.card}>
            <View style={cardStyles.info}>
                <Text style={cardStyles.name}>{place.name}</Text>
                {place.address && (
                    <Text style={cardStyles.address} numberOfLines={2}>{place.address}</Text>
                )}
                <View style={cardStyles.meta}>
                    <View style={cardStyles.tag}>
                        <Text style={cardStyles.tagText}>{place.type}</Text>
                    </View>
                    <View style={cardStyles.tag}>
                        <Text style={cardStyles.tagText}>{place.priceLevel}</Text>
                    </View>
                </View>
            </View>
        </View>
    );
}

const cardStyles = StyleSheet.create({
    card: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 12,
    },
    info: { gap: 6 },
    name: { fontSize: 16, fontWeight: '700', color: '#F0EBE1' },
    address: { fontSize: 13, color: '#9CA3AF' },
    meta: { flexDirection: 'row', gap: 8, marginTop: 4 },
    tag: {
        backgroundColor: '#2D2A45',
        borderRadius: 8,
        paddingHorizontal: 10,
        paddingVertical: 4,
    },
    tagText: { fontSize: 11, color: primaryColor, fontWeight: '600' },
});

export default function PlaceSuggestionsScreen() {
    const { gatheringId } = useLocalSearchParams<{ gatheringId: string }>();
    const router = useRouter();
    const [places, setPlaces] = useState<PlaceDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [regenerating, setRegenerating] = useState(false);
    const [showDurationModal, setShowDurationModal] = useState(false);
    const [creatingPoll, setCreatingPoll] = useState(false);

    const loadSuggestions = useCallback(async () => {
        if (!gatheringId) return;
        setLoading(true);
        try {
            const data = await gatheringService.generatePlaceSuggestions(Number(gatheringId));
            setPlaces(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to generate suggestions.');
        } finally {
            setLoading(false);
        }
    }, [gatheringId]);

    useEffect(() => {
        loadSuggestions();
    }, [loadSuggestions]);

    const handleRegenerate = async () => {
        if (!gatheringId) return;
        setRegenerating(true);
        try {
            const data = await gatheringService.generatePlaceSuggestions(Number(gatheringId));
            setPlaces(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to regenerate suggestions.');
        } finally {
            setRegenerating(false);
        }
    };

    const handleStartVote = async (durationMinutes: number) => {
        if (!gatheringId) return;
        setCreatingPoll(true);
        setShowDurationModal(false);
        try {
            await gatheringService.createPoll(Number(gatheringId), durationMinutes);
            Alert.alert('Vote Started', 'All participants have been notified!', [
                { text: 'OK', onPress: () => router.back() },
            ]);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to create poll.');
        } finally {
            setCreatingPoll(false);
        }
    };

    if (loading) {
        return (
            <View style={styles.center}>
                <ActivityIndicator color={primaryColor} size="large" />
                <Text style={styles.loadingText}>Generating AI suggestions...</Text>
            </View>
        );
    }

    return (
        <View style={styles.container}>
            <ScrollView
                contentContainerStyle={styles.content}
                showsVerticalScrollIndicator={false}
            >
                {/* Header */}
                <View style={styles.topBar}>
                    <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
                        <Text style={styles.backText}>{'< Back'}</Text>
                    </TouchableOpacity>
                </View>

                <Text style={styles.title}>AI Place Suggestions</Text>
                <Text style={styles.subtitle}>
                    These places were generated based on participant preferences. Regenerate for new options or start a vote when you're happy.
                </Text>

                {/* Place list */}
                {places.length === 0 ? (
                    <View style={styles.emptyState}>
                        <Text style={styles.emptyIcon}>{'  '}</Text>
                        <Text style={styles.emptyTitle}>No suggestions yet</Text>
                        <Text style={styles.emptySubtitle}>
                            Tap regenerate to get AI-powered place suggestions.
                        </Text>
                    </View>
                ) : (
                    places.map((place, index) => (
                        <PlaceSuggestionCard key={place.id ?? index} place={place} />
                    ))
                )}

                {/* Action buttons */}
                <View style={styles.actions}>
                    <TouchableOpacity
                        style={[styles.regenerateBtn, regenerating && { opacity: 0.6 }]}
                        onPress={handleRegenerate}
                        disabled={regenerating || creatingPoll}
                        activeOpacity={0.8}
                    >
                        {regenerating ? (
                            <ActivityIndicator color={primaryColor} size="small" />
                        ) : (
                            <Text style={styles.regenerateBtnText}>Regenerate</Text>
                        )}
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={[styles.startVoteBtn, (places.length === 0 || creatingPoll) && { opacity: 0.5 }]}
                        onPress={() => setShowDurationModal(true)}
                        disabled={places.length === 0 || creatingPoll}
                        activeOpacity={0.8}
                    >
                        {creatingPoll ? (
                            <ActivityIndicator color="#0B0B0F" size="small" />
                        ) : (
                            <Text style={styles.startVoteBtnText}>Start Vote</Text>
                        )}
                    </TouchableOpacity>
                </View>
            </ScrollView>

            {/* Duration picker modal */}
            <Modal
                visible={showDurationModal}
                transparent
                animationType="fade"
                onRequestClose={() => setShowDurationModal(false)}
            >
                <View style={styles.overlay}>
                    <View style={styles.dialog}>
                        <Text style={styles.dialogTitle}>Vote Duration</Text>
                        <Text style={styles.dialogSubtitle}>
                            How long should the vote be open?
                        </Text>

                        {DURATION_OPTIONS.map((option) => (
                            <TouchableOpacity
                                key={option.value}
                                style={styles.durationOption}
                                onPress={() => handleStartVote(option.value)}
                                activeOpacity={0.7}
                            >
                                <Text style={styles.durationOptionText}>{option.label}</Text>
                            </TouchableOpacity>
                        ))}

                        <TouchableOpacity
                            style={styles.cancelBtn}
                            onPress={() => setShowDurationModal(false)}
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
    container: { flex: 1, backgroundColor: '#0B0B0F' },
    center: { flex: 1, backgroundColor: '#0B0B0F', justifyContent: 'center', alignItems: 'center', gap: 16 },
    loadingText: { color: '#6B7280', fontSize: 15 },
    content: { paddingTop: 56, paddingHorizontal: 24, paddingBottom: 40 },
    topBar: { marginBottom: 24 },
    backBtn: {},
    backText: { color: primaryColor, fontSize: 16, fontWeight: '600' },
    title: { fontSize: 26, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5, marginBottom: 8 },
    subtitle: { fontSize: 14, color: '#6B7280', lineHeight: 21, marginBottom: 24 },
    emptyState: { alignItems: 'center', paddingVertical: 40 },
    emptyIcon: { fontSize: 40, marginBottom: 12 },
    emptyTitle: { fontSize: 18, fontWeight: '700', color: '#F0EBE1', marginBottom: 4 },
    emptySubtitle: { fontSize: 14, color: '#6B7280', textAlign: 'center' },
    actions: { flexDirection: 'row', gap: 12, marginTop: 24 },
    regenerateBtn: {
        flex: 1,
        backgroundColor: '#16161D',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    regenerateBtnText: { color: primaryColor, fontWeight: '700', fontSize: 15 },
    startVoteBtn: {
        flex: 1,
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
    },
    startVoteBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
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
        textAlign: 'center',
        marginBottom: 4,
    },
    dialogSubtitle: {
        fontSize: 14,
        color: '#6B7280',
        textAlign: 'center',
        marginBottom: 20,
    },
    durationOption: {
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
        marginBottom: 8,
        borderWidth: 1,
        borderColor: '#252530',
    },
    durationOptionText: { color: '#F0EBE1', fontWeight: '600', fontSize: 15 },
    cancelBtn: { paddingVertical: 12, alignItems: 'center', marginTop: 4 },
    cancelBtnText: { color: '#6B7280', fontSize: 14 },
});
