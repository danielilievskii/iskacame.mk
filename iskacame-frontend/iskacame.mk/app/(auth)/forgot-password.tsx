import { useState } from 'react';
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
import { useLocalSearchParams, useRouter } from 'expo-router';
import { authService } from '@/service/auth-service';
import { primaryColor } from '@/constants/theme';

export default function ForgotPasswordScreen() {
    const router = useRouter();
    const params = useLocalSearchParams<{ identifier?: string }>();

    const [step, setStep] = useState<'request' | 'reset'>('request');
    const [identifier, setIdentifier] = useState(params.identifier ?? '');
    const [token, setToken] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);

    const handleRequest = async () => {
        if (!identifier.trim()) {
            Alert.alert('Validation', 'Please enter your email or username.');
            return;
        }
        setLoading(true);
        try {
            await authService.forgotPassword({ identifier: identifier.trim() });
            Alert.alert('Code sent', 'A verification code has been sent to your email.');
            setStep('reset');
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to send reset code.');
        } finally {
            setLoading(false);
        }
    };

    const handleReset = async () => {
        if (!token.trim() || token.trim().length !== 6) {
            Alert.alert('Validation', 'Please enter the 6-digit code.');
            return;
        }
        if (newPassword.length < 6) {
            Alert.alert('Validation', 'Password must be at least 6 characters.');
            return;
        }
        if (newPassword !== confirmPassword) {
            Alert.alert('Validation', 'Passwords do not match.');
            return;
        }
        setLoading(true);
        try {
            await authService.resetPassword({
                identifier: identifier.trim(),
                token: token.trim(),
                newPassword,
            });
            Alert.alert('Success', 'Your password has been reset. You can now sign in.', [
                { text: 'OK', onPress: () => router.replace('/(auth)/login') },
            ]);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to reset password.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <KeyboardAvoidingView
            style={styles.flex}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
            <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
                <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
                    <Text style={styles.backText}>&#8249; Back</Text>
                </TouchableOpacity>

                <View style={styles.card}>
                    <Text style={styles.cardTitle}>
                        {step === 'request' ? 'Forgot password' : 'Reset password'}
                    </Text>
                    <Text style={styles.cardSub}>
                        {step === 'request'
                            ? 'Enter your email or username and we\'ll send you a code.'
                            : 'Enter the code you received and your new password.'}
                    </Text>

                    {step === 'request' ? (
                        <>
                            <View style={styles.field}>
                                <Text style={styles.label}>EMAIL OR USERNAME</Text>
                                <TextInput
                                    style={styles.input}
                                    value={identifier}
                                    onChangeText={setIdentifier}
                                    placeholder="you@example.com"
                                    placeholderTextColor="#6B7280"
                                    autoCapitalize="none"
                                    autoCorrect={false}
                                />
                            </View>

                            <TouchableOpacity
                                style={[styles.button, loading && styles.buttonDisabled]}
                                onPress={handleRequest}
                                disabled={loading}
                                activeOpacity={0.85}
                            >
                                {loading ? (
                                    <ActivityIndicator color="#0B0B0F" />
                                ) : (
                                    <Text style={styles.buttonText}>Send code</Text>
                                )}
                            </TouchableOpacity>
                        </>
                    ) : (
                        <>
                            <View style={styles.field}>
                                <Text style={styles.label}>VERIFICATION CODE</Text>
                                <TextInput
                                    style={[styles.input, styles.codeInput]}
                                    value={token}
                                    onChangeText={setToken}
                                    placeholder="000000"
                                    placeholderTextColor="#6B7280"
                                    keyboardType="number-pad"
                                    maxLength={6}
                                />
                            </View>

                            <View style={styles.field}>
                                <Text style={styles.label}>NEW PASSWORD</Text>
                                <TextInput
                                    style={styles.input}
                                    value={newPassword}
                                    onChangeText={setNewPassword}
                                    placeholder="Min. 6 characters"
                                    placeholderTextColor="#6B7280"
                                    secureTextEntry
                                />
                            </View>

                            <View style={styles.field}>
                                <Text style={styles.label}>CONFIRM PASSWORD</Text>
                                <TextInput
                                    style={styles.input}
                                    value={confirmPassword}
                                    onChangeText={setConfirmPassword}
                                    placeholder="Repeat password"
                                    placeholderTextColor="#6B7280"
                                    secureTextEntry
                                />
                            </View>

                            <TouchableOpacity
                                style={[styles.button, loading && styles.buttonDisabled]}
                                onPress={handleReset}
                                disabled={loading}
                                activeOpacity={0.85}
                            >
                                {loading ? (
                                    <ActivityIndicator color="#0B0B0F" />
                                ) : (
                                    <Text style={styles.buttonText}>Reset password</Text>
                                )}
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={styles.resendBtn}
                                onPress={handleRequest}
                                disabled={loading}
                            >
                                <Text style={styles.resendText}>Resend code</Text>
                            </TouchableOpacity>
                        </>
                    )}
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    flex: { flex: 1, backgroundColor: '#0B0B0F' },
    container: { flexGrow: 1, justifyContent: 'center', padding: 24 },
    backBtn: { marginBottom: 24 },
    backText: { color: primaryColor, fontSize: 16, fontWeight: '600' },
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
        marginBottom: 8,
        letterSpacing: -0.5,
    },
    cardSub: {
        fontSize: 14,
        color: '#6B7280',
        marginBottom: 28,
        lineHeight: 20,
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
    codeInput: {
        textAlign: 'center',
        fontSize: 24,
        fontWeight: '700',
        letterSpacing: 8,
    },
    button: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 8,
    },
    buttonDisabled: { opacity: 0.6 },
    buttonText: { color: '#0B0B0F', fontWeight: '800', fontSize: 16 },
    resendBtn: { marginTop: 16, alignItems: 'center' },
    resendText: { color: primaryColor, fontSize: 14, fontWeight: '600' },
});
