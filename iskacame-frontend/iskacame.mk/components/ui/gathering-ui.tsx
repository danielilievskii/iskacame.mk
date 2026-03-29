import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import type { GatheringStatus } from '@/service/dtos/gathering-types';

const STATUS_CONFIG: Record<GatheringStatus, { label: string; bg: string; color: string }> = {
    DRAFT: { label: 'Draft', bg: '#1F1F2E', color: '#9CA3AF' },
    OPEN: { label: 'Open', bg: '#14302A', color: '#34D399' },
    FINALIZED: { label: 'Finalized', bg: '#1A2744', color: '#60A5FA' },
    CANCELLED: { label: 'Cancelled', bg: '#2A1515', color: '#F87171' },
};

export function StatusBadge({ status }: { status: GatheringStatus }) {
    const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.DRAFT;
    return (
        <View style={[badgeStyles.badge, { backgroundColor: cfg.bg }]}>
            <View style={[badgeStyles.dot, { backgroundColor: cfg.color }]}/>
            <Text style={[badgeStyles.label, { color: cfg.color }]}>{cfg.label}</Text>
        </View>
    );
}

const badgeStyles = StyleSheet.create({
    badge: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 20,
        gap: 5,
    },
    dot: { width: 6, height: 6, borderRadius: 3 },
    label: { fontSize: 12, fontWeight: '600', letterSpacing: 0.3 },
});

export function UserAvatar({
                               name,
                               avatarUrl,
                               size = 36,
                           }: {
    name: string;
    avatarUrl: string | null;
    size?: number;
}) {
    const initials = name
        .split(' ')
        .map((w) => w[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);

    if (avatarUrl) {
        return (
            <Image
                source={{ uri: avatarUrl }}
                style={{ width: size, height: size, borderRadius: size / 2 }}
            />
        );
    }

    return (
        <View
            style={{
                width: size,
                height: size,
                borderRadius: size / 2,
                backgroundColor: '#2D2A45',
                justifyContent: 'center',
                alignItems: 'center',
            }}
        >
            <Text style={{ fontSize: size * 0.35, fontWeight: '700', color: '#B8AEDE' }}>{initials}</Text>
        </View>
    );
}

export function SectionHeader({ title }: { title: string }) {
    return (
        <Text
            style={{
                fontSize: 10,
                fontWeight: '700',
                color: '#4B5563',
                letterSpacing: 1.5,
                marginBottom: 12,
                marginTop: 8,
            }}
        >
            {title}
        </Text>
    );
}

export function EmptyState({
                               icon,
                               title,
                               subtitle,
                           }: {
    icon: string;
    title: string;
    subtitle?: string;
}) {
    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', gap: 8, padding: 32 }}>
            <Text style={{ fontSize: 44, marginBottom: 8 }}>{icon}</Text>
            <Text style={{ fontSize: 18, fontWeight: '700', color: '#F0EBE1' }}>{title}</Text>
            {subtitle && (
                <Text style={{ fontSize: 14, color: '#6B7280', textAlign: 'center', lineHeight: 22 }}>
                    {subtitle}
                </Text>
            )}
        </View>
    );
}

export function formatDate(iso: string): string {
    try {
        const d = new Date(iso);
        return d.toLocaleDateString('en-GB', {
            day: 'numeric',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    } catch {
        return iso;
    }
}

export function formatShortDate(iso: string): string {
    try {
        const d = new Date(iso);
        return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
    } catch {
        return iso;
    }
}