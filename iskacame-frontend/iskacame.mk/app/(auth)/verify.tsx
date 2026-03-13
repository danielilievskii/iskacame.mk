import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ActivityIndicator,
    Alert,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useRef, useState } from 'react';
import { authService } from '@/lib/auth-service';
import { StatusBar } from 'expo-status-bar';
import { Logo } from "@/components/ui/logo";
import { primaryColor } from "@/constants/theme";

export default function VerifyScreen() {
    const { email } = useLocalSearchParams<{ email: string }>();
    const [code, setCode] = useState(['', '', '', '', '', '']);
    const [loading, setLoading] = useState(false);
    const [resending, setResending] = useState(false);
    const inputs = useRef<TextInput[]>([]);
    const router = useRouter();

    const handleChange = (val: string, idx: number) => {
        const digits = val.replace(/[^0-9]/g, '');

        if (digits.length > 1) {
            const split = digits.slice(0, 6).split('');
            const next = [...code];

            split.forEach((d, i) => {
                if (idx + i < 6) {
                    next[idx + i] = d;
                }
            });

            setCode(next);

            const token = next.join('');
            if (token.length === 6) {
                handleVerifyWithToken(token);
            }

            return;
        }

        const next = [...code];
        next[idx] = digits;
        setCode(next);

        if (digits && idx < 5) {
            inputs.current[idx + 1]?.focus();
        }
    };

    const handleVerifyWithToken = async (token: string) => {
        setLoading(true);

        try {
            await authService.verifyEmail({ email, token });

            Alert.alert('Email verified!', 'You can now sign in.', [
                { text: 'Sign in', onPress: () => router.replace('/(auth)/login') },
            ]);
        } catch (err: any) {
            Alert.alert('Verification failed', err.message ?? 'Invalid or expired code.');
        } finally {
            setLoading(false);
        }
    };

    const handleKeyPress = (e: any, idx: number) => {
        if (e.nativeEvent.key === 'Backspace' && !code[idx] && idx > 0) {
            inputs.current[idx - 1]?.focus();
        }
    };

    const handleVerify = async () => {
        const token = code.join('');

        if (token.length < 6) {
            Alert.alert('Incomplete code', 'Please enter all 6 digits.');
            return;
        }

        await handleVerifyWithToken(token);
    };

    const handleResend = async () => {
        setResending(true);
        try {
            await authService.resendCode({ email });
            Alert.alert('Code sent', 'A new verification code has been sent to your email.');
        } catch (err: any) {
            Alert.alert('Error', err.message ?? 'Failed to resend code.');
        } finally {
            setResending(false);
        }
    };

    return (
        <KeyboardAvoidingView
            style={styles.flex}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
            <StatusBar style="light"/>
            <View style={styles.container}>

                <View style={styles.header}>
                    <Logo/>
                </View>

                <View style={styles.card}>
                    <View style={styles.iconBadge}>
                        <Text style={styles.iconText}>✉️</Text>
                    </View>
                    <Text style={styles.cardTitle}>Verify your email</Text>
                    <Text style={styles.subtitle}>
                        We sent a 6-digit code to{'\n'}
                        <Text style={styles.emailHighlight}>{email}</Text>
                    </Text>

                    <View style={styles.codeRow}>
                        {code.map((digit, idx) => (
                            <TextInput
                                key={idx}
                                ref={(el) => {
                                    if (el) inputs.current[idx] = el;
                                }}
                                style={[styles.codeBox, digit ? styles.codeBoxFilled : null]}
                                value={digit}
                                onChangeText={(v) => handleChange(v, idx)}
                                onKeyPress={(e) => handleKeyPress(e, idx)}
                                keyboardType="number-pad"
                                maxLength={1}
                                selectTextOnFocus
                            />
                        ))}
                    </View>

                    <TouchableOpacity
                        style={[styles.button, loading && styles.buttonDisabled]}
                        onPress={handleVerify}
                        disabled={loading}
                        activeOpacity={0.85}>
                        {loading
                            ? <ActivityIndicator color={primaryColor}/>
                            : <Text style={styles.buttonText}>Verify email</Text>
                        }
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={styles.resendBtn}
                        onPress={handleResend}
                        disabled={resending}>
                        {resending
                            ? <ActivityIndicator size="small" color="#C8F55A"/>
                            : <Text style={styles.resendText}>Didn't receive it? <Text style={styles.resendLink}>Resend
                                code</Text></Text>
                        }
                    </TouchableOpacity>
                </View>
            </View>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    flex: { flex: 1, backgroundColor: '#0B0B0F' },
    container: { flex: 1, justifyContent: 'center', padding: 24 },
    header: { flexDirection: 'row', alignItems: 'flex-end', marginBottom: 32 },
    card: {
        backgroundColor: '#16161D',
        borderRadius: 20,
        padding: 28,
        borderWidth: 1,
        borderColor: '#1F1F2E',
        alignItems: 'center',
    },
    iconBadge: {
        width: 56,
        height: 56,
        borderRadius: 16,
        backgroundColor: '#1F1F2E',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 16,
    },
    iconText: { fontSize: 26 },
    cardTitle: {
        fontSize: 22,
        fontWeight: '700',
        color: '#F0EBE1',
        marginBottom: 10,
        letterSpacing: -0.5,
    },
    subtitle: {
        fontSize: 14,
        color: '#6B7280',
        textAlign: 'center',
        lineHeight: 22,
        marginBottom: 28,
    },
    emailHighlight: { color: primaryColor, fontWeight: '600' },
    codeRow: {
        flexDirection: 'row',
        gap: 10,
        marginBottom: 28,
    },
    codeBox: {
        width: 46,
        height: 56,
        backgroundColor: '#0B0B0F',
        borderWidth: 1,
        borderColor: '#252530',
        borderRadius: 12,
        color: '#F0EBE1',
        fontSize: 22,
        fontWeight: '700',
        textAlign: 'center',
    },
    codeBoxFilled: {
        borderColor: primaryColor,
    },
    button: {
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        width: '100%',
    },
    buttonDisabled: { opacity: 0.6 },
    buttonText: { color: '#0B0B0F', fontWeight: '800', fontSize: 16, letterSpacing: 0.3 },
    resendBtn: { marginTop: 20, padding: 8 },
    resendText: { color: '#6B7280', fontSize: 14 },
    resendLink: { color: primaryColor, fontWeight: '600' },
});