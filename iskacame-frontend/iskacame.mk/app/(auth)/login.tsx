import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { Link, useRouter } from 'expo-router';
import { useState } from 'react';
import { useAuth } from '@/context/auth-context';
import { StatusBar } from 'expo-status-bar';
import { Logo } from "@/components/ui/logo";
import { primaryColor } from "@/constants/theme";

export default function LoginScreen() {
    const [identifier, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const { signIn } = useAuth();
    const router = useRouter();

    const handleLogin = async () => {
        if (!identifier.trim() || !password.trim()) {
            Alert.alert('Missing fields', 'Please enter your email or username and password.');
            return;
        }
        setLoading(true);
        try {
            await signIn(identifier.trim(), password);
        } catch (err: any) {
            if (err.status === 403) {
                Alert.alert(
                    'Email not verified',
                    'Please verify your email to continue.',
                    [
                        { text: 'Cancel', style: 'cancel' },
                        {
                            text: 'Verify now',
                            onPress: () => router.push({ pathname: '/(auth)/verify', params: { identifier: identifier.trim() } })
                        },
                    ]
                );
            } else {
                Alert.alert('Login failed', err.message ?? 'Invalid credentials.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <KeyboardAvoidingView
            style={styles.flex}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
            <StatusBar style="light"/>
            <ScrollView
                contentContainerStyle={styles.container}
                keyboardShouldPersistTaps="handled">

                {/* Header */}
                <View style={styles.header}>
                    <Logo/>
                </View>

                <Text style={styles.tagline}>Let's meet up.</Text>

                {/* Card */}
                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Welcome back</Text>

                    <View style={styles.field}>
                        <Text style={styles.label}>EMAIL</Text>
                        <TextInput
                            style={styles.input}
                            value={identifier}
                            onChangeText={setEmail}
                            placeholder="you@example.com"
                            placeholderTextColor="#6B7280"
                            keyboardType="email-address"
                            autoCapitalize="none"
                            autoCorrect={false}
                        />
                    </View>

                    <View style={styles.field}>
                        <Text style={styles.label}>PASSWORD</Text>
                        <TextInput
                            style={styles.input}
                            value={password}
                            onChangeText={setPassword}
                            placeholder="••••••••"
                            placeholderTextColor="#6B7280"
                            secureTextEntry
                        />
                    </View>

                    <TouchableOpacity
                        style={[styles.button, loading && styles.buttonDisabled]}
                        onPress={handleLogin}
                        disabled={loading}
                        activeOpacity={0.85}>
                        {loading
                            ? <ActivityIndicator color="#0B0B0F"/>
                            : <Text style={styles.buttonText}>Sign in</Text>
                        }
                    </TouchableOpacity>

                    <View style={styles.footer}>
                        <Text style={styles.footerText}>Don't have an account? </Text>
                        <Link href="/(auth)/register" asChild>
                            <TouchableOpacity>
                                <Text style={styles.link}>Register</Text>
                            </TouchableOpacity>
                        </Link>
                    </View>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    flex: { flex: 1, backgroundColor: '#0B0B0F' },
    container: {
        flexGrow: 1,
        justifyContent: 'center',
        padding: 24,
    },
    header: {
        flexDirection: 'row',
        alignItems: 'flex-end',
        marginBottom: 4,
    },
    tagline: {
        fontSize: 16,
        color: '#6B7280',
        marginBottom: 40,
        letterSpacing: 0.3,
    },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 24,
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    cardTitle: {
        fontSize: 22,
        fontWeight: '700',
        color: '#F0EBE1',
        marginBottom: 28,
        letterSpacing: -0.5,
    },
    field: {
        marginBottom: 20,
    },
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
    button: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 8,
    },
    buttonDisabled: {
        opacity: 0.6,
    },
    buttonText: {
        color: '#0B0B0F',
        fontWeight: '800',
        fontSize: 16,
        letterSpacing: 0.3,
    },
    footer: {
        flexDirection: 'row',
        justifyContent: 'center',
        marginTop: 20,
    },
    footerText: {
        color: '#6B7280',
        fontSize: 14,
    },
    link: {
        color: primaryColor,
        fontSize: 14,
        fontWeight: '600',
    },
});