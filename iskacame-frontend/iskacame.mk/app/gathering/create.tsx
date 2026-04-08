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

export default function CreateGatheringScreen() {
    const router = useRouter();

    const defaultStart = (() => {
        const d = new Date();
        d.setHours(d.getHours() + 1, 0, 0, 0);
        return d;
    })();
    const defaultEnd = (() => {
        const d = new Date();
        d.setHours(d.getHours() + 3, 0, 0, 0);
        return d;
    })();

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [location, setLocation] = useState('');
    const [startDate, setStartDate] = useState<Date>(defaultStart);
    const [endDate, setEndDate] = useState<Date>(defaultEnd);
    const [selectedParticipants, setSelectedParticipants] = useState<UserSearchDto[]>([]);
    const [loading, setLoading] = useState(false);

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
        if (endDate <= startDate) {
            Alert.alert('Validation', 'End date must be after start date.');
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
            router.replace({
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
                {/* Header */}
                <View style={styles.header}>
                    <TouchableOpacity
                        onPress={() => {
                            if (router.canGoBack()) {
                                router.back();
                            } else {
                                router.replace('/(tabs)');
                            }
                        }}
                        style={styles.backBtn}
                    >
                        <Text style={styles.backText}>‹ Back</Text>
                    </TouchableOpacity>
                    <Text style={styles.pageTitle}>New Gathering</Text>
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
                        onChange={setStartDate}
                        minimumDate={new Date()}
                    />

                    <DateTimePicker
                        label="END DATE *"
                        value={endDate}
                        onChange={setEndDate}
                        minimumDate={startDate}
                    />

                    {/* Participant search */}
                    <ParticipantSearch
                        selectedUsers={selectedParticipants}
                        onAdd={handleAddParticipant}
                        onRemove={handleRemoveParticipant}
                    />

                    {/* Submit */}
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
    backBtn: { marginBottom: 12 },
    backText: { color: primaryColor, fontSize: 16, fontWeight: '600' },
    pageTitle: { fontSize: 26, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
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