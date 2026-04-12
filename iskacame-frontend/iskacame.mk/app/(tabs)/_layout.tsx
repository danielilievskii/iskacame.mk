import { Tabs } from 'expo-router';
import { View, StyleSheet } from 'react-native';
import { HapticTab } from '@/components/haptic-tab';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { primaryColor } from '@/constants/theme';
import { useNotifications } from '@/context/notification-context';

export default function TabLayout() {
    const { unreadCount } = useNotifications();

    return (
        <Tabs
            screenOptions={{
                tabBarActiveTintColor: primaryColor,
                tabBarInactiveTintColor: '#4B5563',
                headerShown: false,
                tabBarButton: HapticTab,
                tabBarStyle: {
                    backgroundColor: '#0D0D14',
                    borderTopColor: '#1F1F2E',
                    borderTopWidth: 1,
                    paddingTop: 4,
                },
                tabBarLabelStyle: {
                    fontSize: 11,
                    fontWeight: '600',
                    letterSpacing: 0.3,
                },
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Gatherings',
                    tabBarIcon: ({ color }) => (
                        <IconSymbol size={24} name="rectangle.3.group" color={color}/>
                    ),
                }}
            />
            <Tabs.Screen
                name="add-gathering"
                options={{
                    title: 'Create',
                    tabBarIcon: ({ color }) => (
                        <IconSymbol size={24} name="plus.circle.fill" color={color}/>
                    ),
                }}
            />
            <Tabs.Screen
                name="notifications"
                options={{
                    title: 'Notifications',
                    tabBarIcon: ({ color }) => (
                        <View>
                            <IconSymbol size={24} name="bell.fill" color={color}/>
                            {unreadCount > 0 && <View style={tabStyles.unreadDot} />}
                        </View>
                    ),
                }}
            />
            <Tabs.Screen
                name="profile"
                options={{
                    title: 'Profile',
                    tabBarIcon: ({ color }) => (
                        <IconSymbol size={24} name="person.fill" color={color}/>
                    ),
                }}
            />
        </Tabs>
    );
}

const tabStyles = StyleSheet.create({
    unreadDot: {
        position: 'absolute',
        top: -2,
        right: -4,
        width: 9,
        height: 9,
        borderRadius: 5,
        backgroundColor: '#EF4444',
        borderWidth: 1.5,
        borderColor: '#0D0D14',
    },
});
