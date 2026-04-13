import { TouchableOpacity, Text, StyleSheet, ActivityIndicator, View, Alert, Platform } from 'react-native';
import { useState, useEffect } from 'react';
import * as Google from 'expo-auth-session/providers/google';
import * as WebBrowser from 'expo-web-browser';
import { makeRedirectUri } from 'expo-auth-session';
import { useAuth } from '@/context/auth-context';

WebBrowser.maybeCompleteAuthSession();

const redirectUri = makeRedirectUri();
const GOOGLE_CLIENT_ID = process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID;

export function GoogleSignInButton() {
    const [loading, setLoading] = useState(false);
    const { signInWithGoogle } = useAuth();

    const [request, response, promptAsync] = Google.useAuthRequest({
        clientId: GOOGLE_CLIENT_ID,
        webClientId: GOOGLE_CLIENT_ID,
    });

    useEffect(() => {
        if (!response) return;
        if (response.type !== 'success') return;

        const code = response.params?.code;
        if (!code) return;

        setLoading(true);
        signInWithGoogle(code, redirectUri)
            .catch((err: any) => {
                Alert.alert('Google Sign-In failed', err.message ?? 'Something went wrong.');
            })
            .finally(() => setLoading(false));
    }, [response]);

    useEffect(() => {
        if (Platform.OS !== 'web') return;

        const params = new URLSearchParams(window.location.search);
        const code = params.get('code');
        if (!code) return;

        window.history.replaceState({}, '', window.location.pathname);

        setLoading(true);
        signInWithGoogle(code, window.location.origin)
            .catch((err: any) => {
                Alert.alert('Google Sign-In failed', err.message ?? 'Something went wrong.');
            })
            .finally(() => setLoading(false));
    }, []);

    const handlePress = async () => {
        if (Platform.OS === 'web') {
            const authUrl =
                `https://accounts.google.com/o/oauth2/v2/auth?` +
                `client_id=${GOOGLE_CLIENT_ID}` +
                `&redirect_uri=${encodeURIComponent(window.location.origin)}` +
                `&response_type=code` +
                `&scope=${encodeURIComponent('openid email profile')}` +
                `&access_type=offline` +
                `&prompt=consent`;
            window.location.href = authUrl;
            return;
        }

        setLoading(true);
        try {
            await promptAsync();
        } catch (err: any) {
            Alert.alert('Google Sign-In failed', err.message ?? 'Something went wrong.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <TouchableOpacity
            style={styles.button}
            onPress={handlePress}
            disabled={loading || !request}
            activeOpacity={0.85}
        >
            {loading ? (
                <ActivityIndicator color="#F0EBE1" />
            ) : (
                <View style={styles.content}>
                    <Text style={styles.googleG}>G</Text>
                    <Text style={styles.text}>Continue with Google</Text>
                </View>
            )}
        </TouchableOpacity>
    );
}

const styles = StyleSheet.create({
    button: {
        backgroundColor: '#1F1F2E',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#252530',
    },
    content: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 10,
    },
    googleG: {
        color: '#4285F4',
        fontSize: 18,
        fontWeight: '800',
    },
    text: {
        color: '#F0EBE1',
        fontWeight: '600',
        fontSize: 15,
    },
});
