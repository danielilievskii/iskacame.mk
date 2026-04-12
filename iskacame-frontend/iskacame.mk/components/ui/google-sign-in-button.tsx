import { TouchableOpacity, Text, StyleSheet, ActivityIndicator, View, Alert } from 'react-native';
import { useState, useEffect } from 'react';
import * as Google from 'expo-auth-session/providers/google';
import * as WebBrowser from 'expo-web-browser';
import { makeRedirectUri } from 'expo-auth-session';
import { useAuth } from '@/context/auth-context';

WebBrowser.maybeCompleteAuthSession();

const redirectUri = makeRedirectUri({ scheme: 'iskacamemk' });

export function GoogleSignInButton() {
    const [loading, setLoading] = useState(false);
    const { signInWithGoogle } = useAuth();

    const [request, response, promptAsync] = Google.useAuthRequest({
        iosClientId: process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID,
        androidClientId: process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID,
        webClientId: process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID,
        redirectUri,
    });

    useEffect(() => {
        if (!response) return;

        if (response.type === 'success' && response.params.code) {
            setLoading(true);
            signInWithGoogle(response.params.code, redirectUri)
                .catch((err: any) => {
                    Alert.alert('Google Sign-In failed', err.message ?? 'Something went wrong.');
                })
                .finally(() => setLoading(false));
        }
    }, [response]);

    const handlePress = async () => {
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
