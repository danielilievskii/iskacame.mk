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
        // Request permission
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

                    {/* Upload overlay */}
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

                {/* Avatar action buttons */}
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
                <Text style={styles.sectionLabel}>ACCOUNT INFO</Text>

                <InfoRow label="Name" value={user?.name ?? '—'} />
                <View style={styles.divider} />
                <InfoRow label="Username" value={`@${user?.username}`} />
                <View style={styles.divider} />
                <InfoRow label="User ID" value={`#${user?.id}`} />
                {user?.phone && (
                    <>
                        <View style={styles.divider} />
                        <InfoRow label="Phone" value={user.phone} />
                    </>
                )}
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
    sectionLabel: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
        marginBottom: 16,
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
});