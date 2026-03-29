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
import { authService } from '@/service/auth-service';
import { StatusBar } from 'expo-status-bar';
import { Logo } from "@/components/ui/logo";
import { primaryColor } from "@/constants/theme";

export default function RegisterScreen() {
    const [name, setName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    const handleRegister = async () => {
        if (!name.trim() || !username.trim() || !email.trim() || !password.trim()) {
            Alert.alert('Missing fields', 'Please fill in all fields.');
            return;
        }
        if (password.length < 6) {
            Alert.alert('Password too short', 'Password must be at least 6 characters.');
            return;
        }
        setLoading(true);
        try {
            await authService.signUp({ name: name.trim(), username: username.trim(), email: email.trim(), password });
            router.push({ pathname: '/(auth)/verify', params: { email: email.trim() } });
        } catch (err: any) {
            Alert.alert('Registration failed', err.message ?? 'Something went wrong.');
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

                <View style={styles.header}>
                    <Logo/>
                </View>
                <Text style={styles.tagline}>Join the gathering.</Text>

                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Create account</Text>

                    {[
                        { label: 'FULL NAME', value: name, setter: setName, placeholder: 'Your name', opts: {} },
                        {
                            label: 'USERNAME',
                            value: username,
                            setter: setUsername,
                            placeholder: 'e.g. johndoe_mk',
                            opts: { autoCapitalize: 'none' as const }
                        },
                        {
                            label: 'EMAIL',
                            value: email,
                            setter: setEmail,
                            placeholder: 'you@example.com',
                            opts: { keyboardType: 'email-address' as const, autoCapitalize: 'none' as const }
                        },
                        {
                            label: 'PASSWORD',
                            value: password,
                            setter: setPassword,
                            placeholder: '••••••••',
                            opts: { secureTextEntry: true }
                        },
                    ].map(({ label, value, setter, placeholder, opts }) => (
                        <View key={label} style={styles.field}>
                            <Text style={styles.label}>{label}</Text>
                            <TextInput
                                style={styles.input}
                                value={value}
                                onChangeText={setter}
                                placeholder={placeholder}
                                placeholderTextColor="#6B7280"
                                autoCorrect={false}
                                {...opts}
                            />
                        </View>
                    ))}

                    <TouchableOpacity
                        style={[styles.button, loading && styles.buttonDisabled]}
                        onPress={handleRegister}
                        disabled={loading}
                        activeOpacity={0.85}>
                        {loading
                            ? <ActivityIndicator color="#0B0B0F"/>
                            : <Text style={styles.buttonText}>Create account</Text>
                        }
                    </TouchableOpacity>

                    <View style={styles.footer}>
                        <Text style={styles.footerText}>Already have an account? </Text>
                        <Link href="/(auth)/login" asChild>
                            <TouchableOpacity>
                                <Text style={styles.link}>Sign in</Text>
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
    container: { flexGrow: 1, justifyContent: 'center', padding: 24 },
    header: { flexDirection: 'row', alignItems: 'flex-end', marginBottom: 4 },
    tagline: { fontSize: 16, color: '#6B7280', marginBottom: 40, letterSpacing: 0.3 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 24,
        borderWidth: 1,
        borderColor: '#1F1F2E',
    },
    cardTitle: { fontSize: 22, fontWeight: '700', color: '#F0EBE1', marginBottom: 28, letterSpacing: -0.5 },
    field: { marginBottom: 18 },
    label: { fontSize: 10, fontWeight: '700', color: '#4B5563', letterSpacing: 1.5, marginBottom: 8 },
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
    button: { backgroundColor: primaryColor, borderRadius: 12, paddingVertical: 16, alignItems: 'center', marginTop: 8 },
    buttonDisabled: { opacity: 0.6 },
    buttonText: { color: '#0B0B0F', fontWeight: '800', fontSize: 16, letterSpacing: 0.3 },
    footer: { flexDirection: 'row', justifyContent: 'center', marginTop: 20 },
    footerText: { color: '#6B7280', fontSize: 14 },
    link: { color: primaryColor, fontSize: 14, fontWeight: '600' },
});