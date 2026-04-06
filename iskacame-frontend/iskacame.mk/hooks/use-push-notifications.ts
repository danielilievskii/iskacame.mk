import { useEffect, useRef } from 'react';
import { Platform } from 'react-native';
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import Constants from 'expo-constants';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useRouter } from 'expo-router';
import { deviceTokenService } from '@/service/device-token-service';

Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: true,
    }),
});

export function usePushNotifications(isAuthenticated: boolean) {
    const router = useRouter();
    const tokenRef = useRef<string | null>(null);

    useEffect(() => {
        if (!isAuthenticated) return;

        let notificationListener: Notifications.EventSubscription;
        let responseListener: Notifications.EventSubscription;

        async function setup() {
            if (!Device.isDevice) {
                console.log('Push notifications require a physical device');
                return;
            }

            const { status: existingStatus } = await Notifications.getPermissionsAsync();
            let finalStatus = existingStatus;

            if (existingStatus !== 'granted') {
                const { status } = await Notifications.requestPermissionsAsync();
                finalStatus = status;
            }

            if (finalStatus !== 'granted') {
                console.log('Push notification permission not granted');
                return;
            }

            if (Platform.OS === 'android') {
                await Notifications.setNotificationChannelAsync('default', {
                    name: 'default',
                    importance: Notifications.AndroidImportance.MAX,
                    vibrationPattern: [0, 250, 250, 250],
                });
            }

            try {
                const projectId = Constants.expoConfig?.extra?.eas?.projectId;
                const pushToken = await Notifications.getExpoPushTokenAsync({
                    ...(projectId ? { projectId } : {}),
                });
                tokenRef.current = pushToken.data;
                await AsyncStorage.setItem('push_token', pushToken.data);
                const platform = Platform.OS;
                await deviceTokenService.register(pushToken.data, platform);
            } catch (e) {
                console.error('Failed to register push token', e);
            }

            // Listen for notifications received while app is in foreground
            notificationListener = Notifications.addNotificationReceivedListener((notification) => {
                // Notification received in foreground — no action needed, it will be displayed
            });

            // Listen for when user taps on a notification
            responseListener = Notifications.addNotificationResponseReceivedListener((response) => {
                const data = response.notification.request.content.data;
                if (data?.gatheringId) {
                    router.push(`/gathering/${data.gatheringId}`);
                }
            });
        }

        setup();

        return () => {
            if (notificationListener) Notifications.removeNotificationSubscription(notificationListener);
            if (responseListener) Notifications.removeNotificationSubscription(responseListener);
        };
    }, [isAuthenticated]);

    const unregisterToken = async () => {
        if (tokenRef.current) {
            try {
                await deviceTokenService.unregister(tokenRef.current);
                tokenRef.current = null;
            } catch (e) {
                console.error('Failed to unregister push token', e);
            }
        }
    };

    return { unregisterToken };
}
