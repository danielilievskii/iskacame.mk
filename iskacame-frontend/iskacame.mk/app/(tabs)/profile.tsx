import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    Alert,
    ActivityIndicator,
    ScrollView,
    Image,
} from 'react-native';
import { useState } from 'react';
import { useAuth } from '@/context/auth-context';
import { StatusBar } from 'expo-status-bar';
import { primaryColor } from "@/constants/theme";

export default function ProfileScreen() {
    const { user, signOut } = useAuth();
    const [signingOut, setSigningOut] = useState(false);

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

    const initials = user?.name
        ?.split(' ')
        .map((w) => w[0])
        .join('')
        .toUpperCase()
        .slice(0, 2) ?? '??';

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.content}>
            <StatusBar style="light" />

            {/* Avatar */}
            <View style={styles.avatarSection}>
                {user?.avatarUrl ? (
                    <Image source={{ uri: user.avatarUrl }} style={styles.avatar} />
                ) : (
                    <View style={styles.avatarPlaceholder}>
                        <Text style={styles.avatarInitials}>{initials}</Text>
                    </View>
                )}
                <Text style={styles.name}>{user?.name}</Text>
                <Text style={styles.handle}>@{user?.username}</Text>
            </View>

            {/* Stats row */}
            <View style={styles.statsRow}>
                {[
                    { label: 'Gatherings', value: '—' },
                    { label: 'Invites', value: '—' },
                    { label: 'Places', value: '—' },
                ].map(({ label, value }) => (
                    <View key={label} style={styles.stat}>
                        <Text style={styles.statValue}>{value}</Text>
                        <Text style={styles.statLabel}>{label}</Text>
                    </View>
                ))}
            </View>

            {/* Info card */}
            <View style={styles.infoCard}>
                <Text style={styles.sectionLabel}>ACCOUNT INFO</Text>

                <View style={styles.infoRow}>
                    <Text style={styles.infoKey}>Name</Text>
                    <Text style={styles.infoValue}>{user?.name}</Text>
                </View>
                <View style={styles.divider} />
                <View style={styles.infoRow}>
                    <Text style={styles.infoKey}>Username</Text>
                    <Text style={styles.infoValue}>@{user?.username}</Text>
                </View>
                <View style={styles.divider} />
                <View style={styles.infoRow}>
                    <Text style={styles.infoKey}>User ID</Text>
                    <Text style={styles.infoValue}>#{user?.id}</Text>
                </View>
            </View>

            {/* Sign out */}
            <TouchableOpacity
                style={[styles.signOutBtn, signingOut && styles.btnDisabled]}
                onPress={handleSignOut}
                disabled={signingOut}
                activeOpacity={0.8}>
                {signingOut
                    ? <ActivityIndicator color="#EF4444" />
                    : <Text style={styles.signOutText}>Sign out</Text>
                }
            </TouchableOpacity>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#0B0B0F' },
    content: { paddingTop: 70, paddingHorizontal: 24, paddingBottom: 40 },
    avatarSection: { alignItems: 'center', marginBottom: 28 },
    avatar: { width: 88, height: 88, borderRadius: 44, marginBottom: 14 },
    avatarPlaceholder: {
        width: 88,
        height: 88,
        borderRadius: 44,
        backgroundColor: primaryColor,
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 14,
    },
    avatarInitials: { fontSize: 30, fontWeight: '800', color: '#0B0B0F' },
    name: { fontSize: 24, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
    handle: { fontSize: 14, color: '#6B7280', marginTop: 4 },
    statsRow: {
        flexDirection: 'row',
        backgroundColor: '#16161D',
        borderRadius: 16,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        marginBottom: 20,
        overflow: 'hidden',
    },
    stat: { flex: 1, paddingVertical: 18, alignItems: 'center' },
    statValue: { fontSize: 20, fontWeight: '800', color: '#F0EBE1', letterSpacing: -0.5 },
    statLabel: { fontSize: 11, color: '#6B7280', marginTop: 2, letterSpacing: 0.3 },
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
    infoRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 4 },
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