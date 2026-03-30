import React, { useCallback, useEffect, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { gatheringService } from '@/service/gathering-service';
import type {
    GatheringResponseOptionsDto,
    GatheringType,
    GatheringTimeSlotOptionDto,
} from '@/service/dtos/gathering-types';
import { SectionHeader } from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

const TYPE_LABELS: Record<GatheringType, { label: string; emoji: string }> = {
    CASUAL: { label: 'Casual', emoji: '😎' },
    SPORT: { label: 'Sport', emoji: '⚽' },
    ELEGANT: { label: 'Elegant', emoji: '🥂' },
    PARTY: { label: 'Party', emoji: '🎉' },
    CULTURAL: { label: 'Cultural', emoji: '🎭' },
    OUTDOOR: { label: 'Outdoor', emoji: '🏕️' },
    TRAVEL: { label: 'Travel', emoji: '✈️' },
    FOOD: { label: 'Food', emoji: '🍕' },
    GAME: { label: 'Game', emoji: '🎮' },
    MOVIE: { label: 'Movie', emoji: '🎬' },
};

const SLOT_LABELS: Record<string, { label: string; time: string }> = {
    MORNING: { label: 'Morning', time: '6:00 - 12:00' },
    NOON: { label: 'Noon', time: '12:00 - 14:00' },
    AFTERNOON: { label: 'Afternoon', time: '14:00 - 18:00' },
    EVENING: { label: 'Evening', time: '18:00 - 23:00' },
};

function formatSlotDate(iso: string): string {
    try {
        const d = new Date(iso);
        return d.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' });
    } catch {
        return iso;
    }
}

export default function PickPreferencesScreen() {
    const { gatheringId, edit } = useLocalSearchParams<{ gatheringId: string; edit?: string }>();
    const isEditMode = edit === '1';
    const router = useRouter();

    const [options, setOptions] = useState<GatheringResponseOptionsDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [selectedTypes, setSelectedTypes] = useState<Set<GatheringType>>(new Set());
    const [selectedSlotIds, setSelectedSlotIds] = useState<Set<number>>(new Set());

    const load = useCallback(async () => {
        if (!gatheringId) return;
        try {
            const data = await gatheringService.getResponseOptions(Number(gatheringId));
            setOptions(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load options.');
        } finally {
            setLoading(false);
        }
    }, [gatheringId]);

    useEffect(() => {
        load();
    }, [load]);

    const toggleType = (type: GatheringType) => {
        setSelectedTypes((prev) => {
            const next = new Set(prev);
            if (next.has(type)) next.delete(type);
            else next.add(type);
            return next;
        });
    };

    const toggleSlot = (id: number) => {
        setSelectedSlotIds((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const handleSubmit = async () => {
        if (selectedTypes.size === 0) {
            Alert.alert('Validation', 'Please select at least one theme.');
            return;
        }
        if (selectedSlotIds.size === 0) {
            Alert.alert('Validation', 'Please select at least one time slot.');
            return;
        }

        setSubmitting(true);
        try {
            const payload = {
                types: Array.from(selectedTypes),
                timeSlotIds: Array.from(selectedSlotIds),
            };

            if (isEditMode) {
                await gatheringService.updateResponse(Number(gatheringId), payload);
            } else {
                await gatheringService.submitResponse(Number(gatheringId), payload);
            }
            router.replace({ pathname: '/gathering/[id]', params: { id: gatheringId! } });
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to submit preferences.');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <View style={styles.center}>
                <ActivityIndicator color={primaryColor} size="large" />
            </View>
        );
    }

    if (!options) {
        return (
            <View style={styles.center}>
                <Text style={{ color: '#6B7280' }}>Could not load options.</Text>
            </View>
        );
    }

    const slotsByDate = options.timeSlots.reduce<Record<string, GatheringTimeSlotOptionDto[]>>(
        (acc, slot) => {
            const key = slot.date;
            if (!acc[key]) acc[key] = [];
            acc[key].push(slot);
            return acc;
        },
        {}
    );

    return (
        <ScrollView
            style={styles.container}
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator={false}
        >
            <View style={styles.header}>
                {isEditMode && (
                    <TouchableOpacity onPress={() => router.back()} style={{ marginBottom: 8 }}>
                        <Text style={{ color: primaryColor, fontSize: 16, fontWeight: '600' }}>‹ Back</Text>
                    </TouchableOpacity>
                )}
                <Text style={styles.pageTitle}>
                    {isEditMode ? 'Edit Preferences' : 'Pick Preferences'}
                </Text>
                <Text style={styles.subtitle}>
                    Choose your preferred themes and time slots
                </Text>
            </View>

            {/* Theme selection */}
            <View style={styles.card}>
                <SectionHeader title="PREFERRED THEMES" />
                <View style={styles.chipGrid}>
                    {options.types.map((type) => {
                        const isSelected = selectedTypes.has(type);
                        const cfg = TYPE_LABELS[type];
                        return (
                            <TouchableOpacity
                                key={type}
                                style={[styles.chip, isSelected && styles.chipSelected]}
                                onPress={() => toggleType(type)}
                                activeOpacity={0.7}
                            >
                                <Text style={styles.chipEmoji}>{cfg?.emoji ?? '?'}</Text>
                                <Text style={[styles.chipLabel, isSelected && styles.chipLabelSelected]}>
                                    {cfg?.label ?? type}
                                </Text>
                            </TouchableOpacity>
                        );
                    })}
                </View>
            </View>

            {/* Time slot selection */}
            <View style={styles.card}>
                <SectionHeader title="PREFERRED TIME SLOTS" />
                {Object.entries(slotsByDate).map(([date, slots]) => (
                    <View key={date} style={styles.dateGroup}>
                        <Text style={styles.dateLabel}>{formatSlotDate(date)}</Text>
                        <View style={styles.slotRow}>
                            {slots.map((slot) => {
                                const isSelected = selectedSlotIds.has(slot.id);
                                const cfg = SLOT_LABELS[slot.slot];
                                return (
                                    <TouchableOpacity
                                        key={slot.id}
                                        style={[styles.slotChip, isSelected && styles.slotChipSelected]}
                                        onPress={() => toggleSlot(slot.id)}
                                        activeOpacity={0.7}
                                    >
                                        <Text
                                            style={[
                                                styles.slotChipLabel,
                                                isSelected && styles.slotChipLabelSelected,
                                            ]}
                                        >
                                            {cfg?.label ?? slot.slot}
                                        </Text>
                                        <Text style={styles.slotChipTime}>{cfg?.time ?? ''}</Text>
                                    </TouchableOpacity>
                                );
                            })}
                        </View>
                    </View>
                ))}
            </View>

            {/* Submit */}
            <TouchableOpacity
                style={[styles.submitBtn, submitting && styles.submitBtnDisabled]}
                onPress={handleSubmit}
                disabled={submitting}
                activeOpacity={0.85}
            >
                {submitting ? (
                    <ActivityIndicator color="#0B0B0F" />
                ) : (
                    <Text style={styles.submitBtnText}>
                        {isEditMode ? 'Update Preferences' : 'Submit Preferences'}
                    </Text>
                )}
            </TouchableOpacity>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    center: { flex: 1, backgroundColor: '#0B0B0F', justifyContent: 'center', alignItems: 'center' },
    container: { flex: 1, backgroundColor: '#0B0B0F' },
    content: { paddingTop: 60, paddingHorizontal: 24, paddingBottom: 40 },
    header: { marginBottom: 24 },
    pageTitle: { fontSize: 26, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
    subtitle: { fontSize: 14, color: '#6B7280', marginTop: 4 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        padding: 20,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 16,
    },
    chipGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
    chip: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        paddingHorizontal: 14,
        paddingVertical: 10,
        borderWidth: 1,
        borderColor: '#252530',
        gap: 6,
    },
    chipSelected: {
        backgroundColor: '#2D2A45',
        borderColor: primaryColor,
    },
    chipEmoji: { fontSize: 16 },
    chipLabel: { fontSize: 14, color: '#9CA3AF', fontWeight: '600' },
    chipLabelSelected: { color: primaryColor },
    dateGroup: { marginBottom: 16 },
    dateLabel: {
        fontSize: 13,
        fontWeight: '700',
        color: '#9CA3AF',
        marginBottom: 8,
    },
    slotRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
    slotChip: {
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        paddingHorizontal: 14,
        paddingVertical: 10,
        borderWidth: 1,
        borderColor: '#252530',
    },
    slotChipSelected: {
        backgroundColor: '#2D2A45',
        borderColor: primaryColor,
    },
    slotChipLabel: { fontSize: 13, fontWeight: '600', color: '#9CA3AF' },
    slotChipLabelSelected: { color: primaryColor },
    slotChipTime: { fontSize: 11, color: '#4B5563', marginTop: 2 },
    submitBtn: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 8,
    },
    submitBtnDisabled: { opacity: 0.6 },
    submitBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 16, letterSpacing: 0.3 },
});
