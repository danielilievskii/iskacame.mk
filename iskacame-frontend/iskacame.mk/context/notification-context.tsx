import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { notificationService } from '@/service/notification-service';
import { useAuth } from '@/context/auth-context';

interface NotificationContextType {
    unreadCount: number;
    refresh: () => void;
}

const NotificationContext = createContext<NotificationContextType>({
    unreadCount: 0,
    refresh: () => {},
});

export function NotificationProvider({ children }: { children: React.ReactNode }) {
    const { user } = useAuth();
    const [unreadCount, setUnreadCount] = useState(0);

    const refresh = useCallback(async () => {
        if (!user) return;
        try {
            const count = await notificationService.getUnreadCount();
            setUnreadCount(count);
        } catch {}
    }, [user]);

    useEffect(() => {
        refresh();
        const interval = setInterval(refresh, 30000);
        return () => clearInterval(interval);
    }, [refresh]);

    return (
        <NotificationContext.Provider value={{ unreadCount, refresh }}>
            {children}
        </NotificationContext.Provider>
    );
}

export const useNotifications = () => useContext(NotificationContext);
