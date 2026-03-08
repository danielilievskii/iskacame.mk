import { View, Text, StyleSheet } from 'react-native';
import { useAuth } from '@/context/auth-context';

export default function HomeScreen() {
    const { user } = useAuth();

    return (
        <View style={styles.container}>
            <View style={styles.headerRow}>
                <View>
                    <Text style={styles.greeting}>Hey, {user?.name?.split(' ')[0]} 👋</Text>
                    <Text style={styles.subtitle}>Your gatherings</Text>
                </View>
            </View>

            <View style={styles.empty}>
                <Text style={styles.emptyIcon}>🗓️</Text>
                <Text style={styles.emptyTitle}>No gatherings yet</Text>
                <Text style={styles.emptyText}>
                    Create your first gathering and invite your friends.
                </Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#0B0B0F', paddingTop: 60, paddingHorizontal: 24 },
    headerRow: { marginBottom: 32 },
    greeting: { fontSize: 28, fontWeight: '800', color: '#F0EBE1', letterSpacing: -1 },
    subtitle: { fontSize: 15, color: '#6B7280', marginTop: 2 },
    empty: { flex: 1, justifyContent: 'center', alignItems: 'center', gap: 8 },
    emptyIcon: { fontSize: 48, marginBottom: 8 },
    emptyTitle: { fontSize: 20, fontWeight: '700', color: '#F0EBE1' },
    emptyText: { fontSize: 14, color: '#6B7280', textAlign: 'center', maxWidth: 220, lineHeight: 22 },
});