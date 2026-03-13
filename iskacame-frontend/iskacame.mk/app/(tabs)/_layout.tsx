import { Tabs } from 'expo-router';
import { HapticTab } from '@/components/haptic-tab';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

export default function TabLayout() {
    const colorScheme = useColorScheme();

    return (
        <Tabs
            screenOptions={{
                tabBarActiveTintColor: Colors[colorScheme ?? 'light'].tint,
                headerShown: false,
                tabBarButton: HapticTab,
                tabBarStyle: {
                    backgroundColor: colorScheme === 'dark' ? '#16161D' : '#fff',
                    borderTopColor: colorScheme === 'dark' ? '#1F1F2E' : '#E5E7EB',
                },
            }}>
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Gatherings',
                    tabBarIcon: ({ color }) => <IconSymbol size={26} name="house.fill" color={color} />,
                }}
            />
            <Tabs.Screen
                name="profile"
                options={{
                    title: 'Profile',
                    tabBarIcon: ({ color }) => <IconSymbol size={26} name="paperplane.fill" color={color} />,
                }}
            />
        </Tabs>
    );
}