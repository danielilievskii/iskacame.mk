import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    Alert,
    ActivityIndicator,
    ScrollView,
    Image,
    Platform,
    Modal,
    TextInput,
    KeyboardAvoidingView,
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { useAuth } from '@/context/auth-context';
import { userService } from '@/service/user-service';
import { StatusBar } from 'expo-status-bar';
import { primaryColor } from '@/constants/theme';

export default function ProfileScreen() {
    const { user, signOut, refreshUser } = useAuth();
    const [signingOut, setSigningOut] = useState(false);
    const [uploadingAvatar, setUploadingAvatar] = useState(false);
    const [deletingAvatar, setDeletingAvatar] = useState(false);
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [editName, setEditName] = useState('');
    const [editUsername, setEditUsername] = useState('');
    const [editPhone, setEditPhone] = useState('');
    const [saving, setSaving] = useState(false);

    const openEditModal = () => {
        setEditName(user?.name ?? '');
        setEditUsername(user?.username ?? '');
        setEditPhone(user?.phone ?? '');
        setEditModalVisible(true);
    };

    const handleSaveProfile = async () => {
        if (!editName.trim()) {
            Alert.alert('Validation', 'Name is required.');
            return;
        }
        if (!editUsername.trim() || editUsername.trim().length < 3) {
            Alert.alert('Validation', 'Username must be at least 3 characters.');
            return;
        }
        setSaving(true);
        try {
            await userService.updateProfile({
                name: editName.trim(),
                username: editUsername.trim(),
                phone: editPhone.trim() || null,
            });
            await refreshUser();
            setEditModalVisible(false);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to update profile.');
        } finally {
            setSaving(false);
        }
    };

    const handleSignOut = () => {
        Alert.alert('Sign out', 'Are you sure you want to sign out?', [
            { text: 'Cancel', style: 'cancel' },
            {
                text: 'Sign out',
                style: 'destructive',
                onPress: async () => {
                    setSigningOut(true);
                    await signOut();
                    setSigningOut(false);
                },
            },
        ]);
    };

    const handlePickAvatar = async () => {
        if (Platform.OS !== 'web') {
            const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
            if (status !== 'granted') {
                Alert.alert('Permission required', 'Please grant access to your photo library.');
                return;
            }
        }

        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ImagePicker.MediaTypeOptions.Images,
            allowsEditing: true,
            aspect: [1, 1],
            quality: 0.8,
        });

        if (result.canceled || !result.assets?.[0]) return;

        const asset = result.assets[0];
        setUploadingAvatar(true);
        try {
            await userService.uploadAvatar(asset.uri, asset.mimeType ?? 'image/jpeg');
            await refreshUser();
            Alert.alert('Success', 'Profile picture updated.');
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to upload avatar.');
        } finally {
            setUploadingAvatar(false);
        }
    };

    const handleDeleteAvatar = () => {
        Alert.alert('Remove picture', 'Remove your profile picture?', [
            { text: 'Cancel', style: 'cancel' },
            {
                text: 'Remove',
                style: 'destructive',
                onPress: async () => {
                    setDeletingAvatar(true);
                    try {
                        await userService.deleteAvatar();
                        await refreshUser();
                    } catch (e: any) {
                        Alert.alert('Error', e.message ?? 'Failed to delete avatar.');
                    } finally {
                        setDeletingAvatar(false);
                    }
                },
            },
        ]);
    };

    const initials = user?.name
        ?.split(' ')
        .map((w: string) => w[0])
        .join('')
        .toUpperCase()
        .slice(0, 2) ?? '??';

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.content}>
            <StatusBar style="light" />

            {/* Avatar section */}
            <View style={styles.avatarSection}>
                <View style={styles.avatarWrapper}>
                    {user?.avatarUrl ? (
                        <Image source={{ uri: user.avatarUrl }} style={styles.avatar} />
                    ) : (
                        <View style={styles.avatarPlaceholder}>
                            <Text style={styles.avatarInitials}>{initials}</Text>
                        </View>
                    )}

                    <TouchableOpacity
                        style={styles.avatarOverlay}
                        onPress={handlePickAvatar}
                        disabled={uploadingAvatar || deletingAvatar}
                        activeOpacity={0.8}
                    >
                        {uploadingAvatar ? (
                            <ActivityIndicator color="#F0EBE1" size="small" />
                        ) : (
                            <Text style={styles.avatarOverlayText}>📷</Text>
                        )}
                    </TouchableOpacity>
                </View>

                <Text style={styles.name}>{user?.name}</Text>
                <Text style={styles.handle}>@{user?.username}</Text>

                <View style={styles.avatarActions}>
                    <TouchableOpacity
                        style={styles.avatarBtn}
                        onPress={handlePickAvatar}
                        disabled={uploadingAvatar}
                        activeOpacity={0.8}
                    >
                        {uploadingAvatar ? (
                            <ActivityIndicator color={primaryColor} size="small" />
                        ) : (
                            <Text style={styles.avatarBtnText}>
                                {user?.avatarUrl ? 'Change photo' : 'Upload photo'}
                            </Text>
                        )}
                    </TouchableOpacity>

                    {user?.avatarUrl && (
                        <TouchableOpacity
                            style={[styles.avatarBtn, styles.avatarBtnDanger]}
                            onPress={handleDeleteAvatar}
                            disabled={deletingAvatar}
                            activeOpacity={0.8}
                        >
                            {deletingAvatar ? (
                                <ActivityIndicator color="#EF4444" size="small" />
                            ) : (
                                <Text style={[styles.avatarBtnText, styles.avatarBtnTextDanger]}>
                                    Remove photo
                                </Text>
                            )}
                        </TouchableOpacity>
                    )}
                </View>
            </View>

            {/* Account info card */}
            <View style={styles.infoCard}>
                <View style={styles.sectionRow}>
                    <Text style={styles.sectionLabel}>ACCOUNT INFO</Text>
                    <TouchableOpacity onPress={openEditModal} activeOpacity={0.7}>
                        <Text style={styles.editLink}>Edit</Text>
                    </TouchableOpacity>
                </View>

                <InfoRow label="Name" value={user?.name ?? '—'} />
                <View style={styles.divider} />
                <InfoRow label="Username" value={`@${user?.username}`} />
                <View style={styles.divider} />
                <InfoRow label="User ID" value={`#${user?.id}`} />
                {user?.phone ? (
                    <>
                        <View style={styles.divider} />
                        <InfoRow label="Phone" value={user.phone} />
                    </>
                ) : null}
            </View>

            {/* Sign out */}
            <TouchableOpacity
                style={[styles.signOutBtn, signingOut && styles.btnDisabled]}
                onPress={handleSignOut}
                disabled={signingOut}
                activeOpacity={0.8}
            >
                {signingOut ? (
                    <ActivityIndicator color="#EF4444" />
                ) : (
                    <Text style={styles.signOutText}>Sign out</Text>
                )}
            </TouchableOpacity>

            {/* Edit Profile Modal */}
            <Modal
                visible={editModalVisible}
                transparent
                animationType="fade"
                onRequestClose={() => setEditModalVisible(false)}
            >
                <KeyboardAvoidingView
                    style={styles.overlay}
                    behavior={Platform.OS === 'ios' ? 'padding' : undefined}
                >
                    <View style={styles.dialog}>
                        <Text style={styles.dialogTitle}>Edit Profile</Text>

                        <View style={styles.field}>
                            <Text style={styles.fieldLabel}>NAME</Text>
                            <TextInput
                                style={styles.fieldInput}
                                value={editName}
                                onChangeText={setEditName}
                                placeholder="Your name"
                                placeholderTextColor="#6B7280"
                                maxLength={50}
                            />
                        </View>

                        <View style={styles.field}>
                            <Text style={styles.fieldLabel}>USERNAME</Text>
                            <TextInput
                                style={styles.fieldInput}
                                value={editUsername}
                                onChangeText={setEditUsername}
                                placeholder="Username"
                                placeholderTextColor="#6B7280"
                                autoCapitalize="none"
                                maxLength={30}
                            />
                        </View>

                        <View style={styles.field}>
                            <Text style={styles.fieldLabel}>PHONE (optional)</Text>
                            <TextInput
                                style={styles.fieldInput}
                                value={editPhone}
                                onChangeText={setEditPhone}
                                placeholder="+38970123456"
                                placeholderTextColor="#6B7280"
                                keyboardType="phone-pad"
                                maxLength={16}
                            />
                        </View>

                        <View style={styles.dialogActions}>
                            <TouchableOpacity
                                style={styles.dialogCancel}
                                onPress={() => setEditModalVisible(false)}
                                disabled={saving}
                            >
                                <Text style={styles.dialogCancelText}>Cancel</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[styles.dialogConfirm, saving && { opacity: 0.6 }]}
                                onPress={handleSaveProfile}
                                disabled={saving}
                                activeOpacity={0.85}
                            >
                                {saving ? (
                                    <ActivityIndicator color="#0B0B0F" size="small" />
                                ) : (
                                    <Text style={styles.dialogConfirmText}>Save</Text>
                                )}
                            </TouchableOpacity>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
        </ScrollView>
    );
}

function InfoRow({ label, value }: { label: string; value: string }) {
    return (
        <View style={styles.infoRow}>
            <Text style={styles.infoKey}>{label}</Text>
            <Text style={styles.infoValue}>{value}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#0B0B0F' },
    content: { paddingTop: 70, paddingHorizontal: 24, paddingBottom: 40 },

    avatarSection: { alignItems: 'center', marginBottom: 28 },
    avatarWrapper: { position: 'relative', marginBottom: 14 },
    avatar: { width: 96, height: 96, borderRadius: 48 },
    avatarPlaceholder: {
        width: 96,
        height: 96,
        borderRadius: 48,
        backgroundColor: primaryColor,
        justifyContent: 'center',
        alignItems: 'center',
    },
    avatarInitials: { fontSize: 32, fontWeight: '800', color: '#0B0B0F' },
    avatarOverlay: {
        position: 'absolute',
        bottom: 0,
        right: 0,
        width: 30,
        height: 30,
        borderRadius: 15,
        backgroundColor: '#252530',
        borderWidth: 2,
        borderColor: '#0B0B0F',
        justifyContent: 'center',
        alignItems: 'center',
    },
    avatarOverlayText: { fontSize: 14 },
    name: { fontSize: 24, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
    handle: { fontSize: 14, color: '#6B7280', marginTop: 4, marginBottom: 16 },

    avatarActions: { flexDirection: 'row', gap: 8 },
    avatarBtn: {
        backgroundColor: '#1F1F2E',
        borderRadius: 10,
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderWidth: 1,
        borderColor: '#252530',
        minWidth: 90,
        alignItems: 'center',
    },
    avatarBtnDanger: { borderColor: '#3B1D1D' },
    avatarBtnText: { color: primaryColor, fontWeight: '600', fontSize: 13 },
    avatarBtnTextDanger: { color: '#EF4444' },

    infoCard: {
        backgroundColor: '#16161D',
        borderRadius: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        padding: 20,
        marginBottom: 20,
    },
    sectionRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 16,
    },
    sectionLabel: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
    },
    editLink: {
        fontSize: 13,
        fontWeight: '700',
        color: primaryColor,
    },
    infoRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 4,
    },
    infoKey: { fontSize: 14, color: '#6B7280' },
    infoValue: { fontSize: 14, color: '#F0EBE1', fontWeight: '500' },
    divider: { height: 1, backgroundColor: '#1F1F2E', marginVertical: 10 },

    signOutBtn: {
        backgroundColor: '#16161D',
        borderRadius: 12,
        borderWidth: 1,
        borderColor: '#3B1D1D',
        paddingVertical: 16,
        alignItems: 'center',
    },
    btnDisabled: { opacity: 0.5 },
    signOutText: { color: '#EF4444', fontWeight: '700', fontSize: 15 },

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
        marginBottom: 20,
        textAlign: 'center',
    },
    field: { marginBottom: 16 },
    fieldLabel: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
        marginBottom: 6,
    },
    fieldInput: {
        backgroundColor: '#0B0B0F',
        borderWidth: 1,
        borderColor: '#252530',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 12,
        color: '#F0EBE1',
        fontSize: 16,
    },
    dialogActions: { flexDirection: 'row', gap: 12, marginTop: 8 },
    dialogCancel: {
        flex: 1,
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#252530',
    },
    dialogCancelText: { color: '#6B7280', fontWeight: '600', fontSize: 15 },
    dialogConfirm: {
        flex: 1,
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
    },
    dialogConfirmText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
});
