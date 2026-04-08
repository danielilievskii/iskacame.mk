import React, { useCallback, useEffect, useState } from 'react';
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
import { useLocalSearchParams, useRouter } from 'expo-router';
import { gatheringService } from '@/service/gathering-service';
import type { GatheringDetailsDto } from '@/service/dtos/gathering-types';
import { StatusBadge, formatDate } from '@/components/ui/gathering-ui';
import { primaryColor } from '@/constants/theme';

export default function ManageGatheringScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const router = useRouter();
    const [gathering, setGathering] = useState<GatheringDetailsDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [cancelling, setCancelling] = useState(false);

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');

    const load = useCallback(async () => {
        if (!id) return;
        try {
            const data = await gatheringService.getGatheringDetails(Number(id));
            setGathering(data);
            setTitle(data.title);
            setDescription(data.description ?? '');
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load gathering.');
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        load();
    }, [load]);

    const handleSave = async () => {
        if (!title.trim()) {
            Alert.alert('Validation', 'Title cannot be empty.');
            return;
        }
        setSaving(true);
        try {
            await gatheringService.updateGathering(Number(id), {
                title: title.trim(),
                description: description.trim() || undefined,
            });
            Alert.alert('Saved', 'Gathering updated successfully.', [
                { text: 'OK', onPress: () => router.back() },
            ]);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to update gathering.');
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = () => {
        Alert.alert(
            'Cancel gathering',
            'This action cannot be undone. Are you sure you want to cancel this gathering?',
            [
                { text: 'Back', style: 'cancel' },
                {
                    text: 'Cancel gathering',
                    style: 'destructive',
                    onPress: async () => {
                        setCancelling(true);
                        try {
                            await gatheringService.cancelGathering(Number(id));
                            Alert.alert('Cancelled', 'The gathering has been cancelled.', [
                                { text: 'OK', onPress: () => router.replace('/(tabs)') },
                            ]);
                        } catch (e: any) {
                            Alert.alert('Error', e.message ?? 'Failed to cancel gathering.');
                        } finally {
                            setCancelling(false);
                        }
                    },
                },
            ]
        );
    };

    if (loading) {
        return (
            <View style={styles.center}>
                <ActivityIndicator color={primaryColor} size="large"/>
            </View>
        );
    }

    if (!gathering) {
        return (
            <View style={styles.center}>
                <Text style={{ color: '#6B7280' }}>Gathering not found.</Text>
            </View>
        );
    }

    return (
        <KeyboardAvoidingView
            style={styles.flex}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
            <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
                {/* Header */}
                <View style={styles.header}>
                    <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
                        <Text style={styles.backText}>‹ Back</Text>
                    </TouchableOpacity>
                    <Text style={styles.pageTitle}>Manage</Text>
                    <StatusBadge status={gathering.status}/>
                </View>

                {/* Current info */}
                <View style={styles.infoCard}>
                    <Text style={styles.infoLabel}>GATHERING ID</Text>
                    <Text style={styles.infoValue}>#{gathering.id}</Text>
                    <Text style={[styles.infoLabel, { marginTop: 8 }]}>ORIGINAL START DATE</Text>
                    <Text style={styles.infoValue}>{formatDate(gathering.startDate)}</Text>
                </View>

                {/* Edit form */}
                <View style={styles.card}>
                    <Text style={styles.sectionTitle}>EDIT DETAILS</Text>

                    <View style={styles.field}>
                        <Text style={styles.label}>TITLE</Text>
                        <TextInput
                            style={styles.input}
                            value={title}
                            onChangeText={setTitle}
                            placeholder="Title…"
                            placeholderTextColor="#6B7280"
                            maxLength={100}
                        />
                        <Text style={styles.charCount}>{title.length}/100</Text>
                    </View>

                    <View style={styles.field}>
                        <Text style={styles.label}>DESCRIPTION</Text>
                        <TextInput
                            style={[styles.input, styles.multiline]}
                            value={description}
                            onChangeText={setDescription}
                            placeholder="Description…"
                            placeholderTextColor="#6B7280"
                            multiline
                            numberOfLines={4}
                            maxLength={500}
                        />
                        <Text style={styles.charCount}>{description.length}/500</Text>
                    </View>

                    <TouchableOpacity
                        style={[styles.saveBtn, saving && styles.btnDisabled]}
                        onPress={handleSave}
                        disabled={saving}
                        activeOpacity={0.85}
                    >
                        {saving ? (
                            <ActivityIndicator color="#0B0B0F"/>
                        ) : (
                            <Text style={styles.saveBtnText}>Save changes</Text>
                        )}
                    </TouchableOpacity>
                </View>

                {/* Danger zone */}
                {gathering.status !== 'CANCELLED' && (
                    <View style={styles.dangerCard}>
                        <Text style={styles.dangerTitle}>DANGER ZONE</Text>
                        <Text style={styles.dangerDesc}>
                            Cancelling a gathering cannot be undone. All invitations will be invalidated.
                        </Text>
                        <TouchableOpacity
                            style={[styles.cancelBtn, cancelling && styles.btnDisabled]}
                            onPress={handleCancel}
                            disabled={cancelling}
                            activeOpacity={0.8}
                        >
                            {cancelling ? (
                                <ActivityIndicator color="#EF4444"/>
                            ) : (
                                <Text style={styles.cancelBtnText}>Cancel gathering</Text>
                            )}
                        </TouchableOpacity>
                    </View>
                )}
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    flex: { flex: 1, backgroundColor: '#0B0B0F' },
    center: { flex: 1, backgroundColor: '#0B0B0F', justifyContent: 'center', alignItems: 'center' },
    container: { paddingHorizontal: 24, paddingTop: 60, paddingBottom: 40 },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        marginBottom: 24,
        flexWrap: 'wrap',
    },
    backBtn: { marginRight: 'auto' as any },
    backText: { color: primaryColor, fontSize: 16, fontWeight: '600' },
    pageTitle: { fontSize: 22, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
    infoCard: {
        backgroundColor: '#16161D',
        borderRadius: 14,
        padding: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 16,
    },
    infoLabel: { fontSize: 10, fontWeight: '700', color: '#4B5563', letterSpacing: 1.5 },
    infoValue: { fontSize: 14, color: '#F0EBE1', fontWeight: '500', marginTop: 2 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 24,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 16,
    },
    sectionTitle: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
        marginBottom: 20,
    },
    field: { marginBottom: 18 },
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
    saveBtn: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 4,
    },
    btnDisabled: { opacity: 0.5 },
    saveBtnText: { color: '#0B0B0F', fontWeight: '800', fontSize: 16 },
    dangerCard: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 24,
        borderWidth: 1,
        borderColor: '#3B1D1D',
    },
    dangerTitle: {
        fontSize: 10,
        fontWeight: '700',
        color: '#EF4444',
        letterSpacing: 1.5,
        marginBottom: 10,
    },
    dangerDesc: { fontSize: 14, color: '#9CA3AF', lineHeight: 22, marginBottom: 16 },
    cancelBtn: {
        backgroundColor: '#16161D',
        borderRadius: 12,
        borderWidth: 1,
        borderColor: '#3B1D1D',
        paddingVertical: 14,
        alignItems: 'center',
    },
    cancelBtnText: { color: '#EF4444', fontWeight: '700', fontSize: 15 },
});