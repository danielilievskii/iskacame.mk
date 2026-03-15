import { Tabs } from 'expo-router';
import { HapticTab } from '@/components/haptic-tab';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { primaryColor } from '@/constants/theme';

export default function TabLayout() {
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
                        <IconSymbol size={24} name="house.fill" color={color}/>
                    ),
                }}
            />
            <Tabs.Screen
                name="profile"
                options={{
                    title: 'Profile',
                    tabBarIcon: ({ color }) => (
                        <IconSymbol size={24} name="paperplane.fill" color={color}/>
                    ),
                }}
            />
        </Tabs>
    );
}