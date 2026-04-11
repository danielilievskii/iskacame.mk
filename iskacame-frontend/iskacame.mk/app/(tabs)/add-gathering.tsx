import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    KeyboardAvoidingView,
    Platform,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { useRouter } from 'expo-router';
import { gatheringService } from '@/service/gathering-service';
import { UserSearchDto } from '@/service/user-search-service';
import { DateTimePicker, toLocalDateTimeString } from '@/components/ui/date-time-picker';
import { ParticipantSearch } from '@/components/ui/participant-search';
import { primaryColor } from '@/constants/theme';

const TIME_SLOT_START_HOUR = 6;
const TIME_SLOT_END_HOUR = 23;
const MIN_DURATION_MS = 60 * 60 * 1000;

function clampToTimeSlotWindow(d: Date): Date {
    const clamped = new Date(d);
    const hour = clamped.getHours();
    if (hour < TIME_SLOT_START_HOUR) {
        clamped.setHours(TIME_SLOT_START_HOUR, 0, 0, 0);
    } else if (
        hour > TIME_SLOT_END_HOUR ||
        (hour === TIME_SLOT_END_HOUR && (clamped.getMinutes() > 0 || clamped.getSeconds() > 0))
    ) {
        clamped.setHours(TIME_SLOT_END_HOUR, 0, 0, 0);
    }
    return clamped;
}

function isWithinTimeSlotWindow(d: Date): boolean {
    const hour = d.getHours();
    if (hour < TIME_SLOT_START_HOUR) return false;
    if (hour > TIME_SLOT_END_HOUR) return false;
    if (hour === TIME_SLOT_END_HOUR && (d.getMinutes() > 0 || d.getSeconds() > 0)) return false;
    return true;
}

export default function AddGatheringScreen() {
    const router = useRouter();

    const defaultStart = (() => {
        const d = new Date();
        d.setHours(d.getHours() + 1, 0, 0, 0);
        return clampToTimeSlotWindow(d);
    })();
    const defaultEnd = (() => {
        const d = new Date();
        d.setHours(d.getHours() + 3, 0, 0, 0);
        return clampToTimeSlotWindow(d);
    })();

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [location, setLocation] = useState('');
    const [startDate, setStartDate] = useState<Date>(defaultStart);
    const [endDate, setEndDate] = useState<Date>(defaultEnd);
    const [selectedParticipants, setSelectedParticipants] = useState<UserSearchDto[]>([]);
    const [loading, setLoading] = useState(false);

    const reset = () => {
        setTitle('');
        setDescription('');
        setLocation('');
        setStartDate(defaultStart);
        setEndDate(defaultEnd);
        setSelectedParticipants([]);
    };

    const handleAddParticipant = (user: UserSearchDto) => {
        setSelectedParticipants((prev) =>
            prev.find((u) => u.id === user.id) ? prev : [...prev, user]
        );
    };

    const handleRemoveParticipant = (id: number) => {
        setSelectedParticipants((prev) => prev.filter((u) => u.id !== id));
    };

    const handleCreate = async () => {
        if (!title.trim()) {
            Alert.alert('Validation', 'Title is required.');
            return;
        }
        if (endDate.getTime() - startDate.getTime() < MIN_DURATION_MS) {
            Alert.alert('Validation', 'End date must be at least 1 hour after start date.');
            return;
        }
        if (!isWithinTimeSlotWindow(startDate) || !isWithinTimeSlotWindow(endDate)) {
            Alert.alert(
                'Validation',
                `Times must be between ${String(TIME_SLOT_START_HOUR).padStart(2, '0')}:00 and ${String(TIME_SLOT_END_HOUR).padStart(2, '0')}:00.`
            );
            return;
        }
        if (selectedParticipants.length === 0) {
            Alert.alert('Validation', 'Please add at least one participant.');
            return;
        }

        setLoading(true);
        try {
            const gathering = await gatheringService.createGathering({
                title: title.trim(),
                description: description.trim() || undefined,
                location: location.trim() || undefined,
                startDate: toLocalDateTimeString(startDate),
                endDate: toLocalDateTimeString(endDate),
                participantIds: selectedParticipants.map((u) => u.id),
            });
            reset();
            router.push({
                pathname: '/gathering/pick-preferences',
                params: { gatheringId: gathering.id },
            });
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to create gathering.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <KeyboardAvoidingView
            style={styles.flex}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
            <ScrollView
                contentContainerStyle={styles.container}
                keyboardShouldPersistTaps="handled"
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <Text style={styles.pageTitle}>New Gathering</Text>
                    <Text style={styles.subtitle}>Fill in the details below</Text>
                </View>

                <View style={styles.card}>
                    {/* Title */}
                    <View style={styles.field}>
                        <Text style={styles.label}>TITLE *</Text>
                        <TextInput
                            style={styles.input}
                            value={title}
                            onChangeText={setTitle}
                            placeholder="Weekend BBQ, Movie Night…"
                            placeholderTextColor="#6B7280"
                            maxLength={100}
                        />
                        <Text style={styles.charCount}>{title.length}/100</Text>
                    </View>

                    {/* Description */}
                    <View style={styles.field}>
                        <Text style={styles.label}>DESCRIPTION</Text>
                        <TextInput
                            style={[styles.input, styles.multiline]}
                            value={description}
                            onChangeText={setDescription}
                            placeholder="What's the plan? Any details…"
                            placeholderTextColor="#6B7280"
                            multiline
                            numberOfLines={4}
                            maxLength={500}
                        />
                        <Text style={styles.charCount}>{description.length}/500</Text>
                    </View>

                    {/* Location */}
                    <View style={styles.field}>
                        <Text style={styles.label}>LOCATION</Text>
                        <TextInput
                            style={styles.input}
                            value={location}
                            onChangeText={setLocation}
                            placeholder="City or area for AI suggestions…"
                            placeholderTextColor="#6B7280"
                            maxLength={200}
                        />
                        <Text style={styles.charCount}>{location.length}/200</Text>
                    </View>

                    {/* Date pickers */}
                    <DateTimePicker
                        label="START DATE *"
                        value={startDate}
                        onChange={(d) => {
                            const clamped = clampToTimeSlotWindow(d);
                            setStartDate(clamped);
                            if (endDate.getTime() - clamped.getTime() < MIN_DURATION_MS) {
                                const bumped = new Date(clamped.getTime() + MIN_DURATION_MS);
                                setEndDate(clampToTimeSlotWindow(bumped));
                            }
                        }}
                        minimumDate={new Date()}
                    />

                    <DateTimePicker
                        label="END DATE *"
                        value={endDate}
                        onChange={(d) => {
                            const minEnd = startDate.getTime() + MIN_DURATION_MS;
                            const floored =
                                d.getTime() < minEnd ? new Date(minEnd) : d;
                            setEndDate(clampToTimeSlotWindow(floored));
                        }}
                        minimumDate={new Date(startDate.getTime() + MIN_DURATION_MS)}
                    />

                    <Text style={styles.hint}>
                        Times are restricted to{' '}
                        {String(TIME_SLOT_START_HOUR).padStart(2, '0')}:00 –{' '}
                        {String(TIME_SLOT_END_HOUR).padStart(2, '0')}:00 so they line up with the
                        available time slots.
                    </Text>

                    {/* Participant search */}
                    <ParticipantSearch
                        selectedUsers={selectedParticipants}
                        onAdd={handleAddParticipant}
                        onRemove={handleRemoveParticipant}
                    />

                    <TouchableOpacity
                        style={[styles.button, loading && styles.buttonDisabled]}
                        onPress={handleCreate}
                        disabled={loading}
                        activeOpacity={0.85}
                    >
                        {loading ? (
                            <ActivityIndicator color="#0B0B0F" />
                        ) : (
                            <Text style={styles.buttonText}>Create Gathering</Text>
                        )}
                    </TouchableOpacity>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    flex: { flex: 1, backgroundColor: '#0B0B0F' },
    container: { paddingHorizontal: 24, paddingTop: 60, paddingBottom: 40 },
    header: { marginBottom: 28 },
    pageTitle: { fontSize: 26, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
    subtitle: { fontSize: 14, color: '#6B7280', marginTop: 4 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 24,
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    field: { marginBottom: 20 },
    label: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
        marginBottom: 8,
    },
    input: {
        backgroundColor: '#0B0B0F',
        borderWidth: 1,
        borderColor: '#252530',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 14,
        color: '#F0EBE1',
        fontSize: 16,
    },
    multiline: { height: 100, textAlignVertical: 'top', paddingTop: 14 },
    charCount: { fontSize: 11, color: '#4B5563', marginTop: 4, textAlign: 'right' },
    hint: { fontSize: 11, color: '#6B7280', marginTop: -8, marginBottom: 16, lineHeight: 16 },
    button: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 8,
    },
    buttonDisabled: { opacity: 0.6 },
    buttonText: { color: '#0B0B0F', fontWeight: '800', fontSize: 16, letterSpacing: 0.3 },
});