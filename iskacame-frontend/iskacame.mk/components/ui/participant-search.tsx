import React, { useEffect, useRef, useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ActivityIndicator,
    Image,
    ScrollView,
    Keyboard,
} from 'react-native';
import { userSearchService, UserSearchDto } from '@/service/user-search-service';
import { primaryColor } from '@/constants/theme';

function Avatar({ user, size = 38 }: { user: UserSearchDto; size?: number }) {
    const initials = user.name
        .split(' ')
        .map((w) => w[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);

    if (user.avatarUrl) {
        return (
            <Image
                source={{ uri: user.avatarUrl }}
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
            <Text style={{ fontSize: size * 0.36, fontWeight: '700', color: primaryColor }}>
                {initials}
            </Text>
        </View>
    );
}

function SearchResultRow({
                             user,
                             onAdd,
                             alreadyAdded,
                         }: {
    user: UserSearchDto;
    onAdd: (user: UserSearchDto) => void;
    alreadyAdded: boolean;
}) {
    return (
        <TouchableOpacity
            style={[resultStyles.row, alreadyAdded && resultStyles.rowAdded]}
            onPress={() => !alreadyAdded && onAdd(user)}
            activeOpacity={alreadyAdded ? 1 : 0.65}
        >
            <Avatar user={user} size={36}/>
            <View style={resultStyles.info}>
                <Text style={resultStyles.name}>{user.name}</Text>
                <Text style={resultStyles.username}>@{user.username}</Text>
            </View>
            {alreadyAdded ? (
                <View style={resultStyles.addedBadge}>
                    <Text style={resultStyles.addedBadgeText}>✓ Added</Text>
                </View>
            ) : (
                <View style={resultStyles.addBtn}>
                    <Text style={resultStyles.addBtnText}>+</Text>
                </View>
            )}
        </TouchableOpacity>
    );
}

const resultStyles = StyleSheet.create({
    row: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 10,
        paddingHorizontal: 14,
        gap: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#1A1A26',
    },
    rowAdded: { opacity: 0.5 },
    info: { flex: 1 },
    name: { fontSize: 14, fontWeight: '600', color: '#F0EBE1' },
    username: { fontSize: 12, color: '#6B7280', marginTop: 1 },
    addBtn: {
        width: 28,
        height: 28,
        borderRadius: 14,
        backgroundColor: primaryColor,
        justifyContent: 'center',
        alignItems: 'center',
    },
    addBtnText: { color: '#0B0B0F', fontSize: 18, fontWeight: '700', lineHeight: 22 },
    addedBadge: {
        backgroundColor: '#14302A',
        borderRadius: 10,
        paddingHorizontal: 8,
        paddingVertical: 3,
    },
    addedBadgeText: { color: '#34D399', fontSize: 11, fontWeight: '600' },
});

function ParticipantChip({
                             user,
                             onRemove,
                         }: {
    user: UserSearchDto;
    onRemove: (id: number) => void;
}) {
    return (
        <View style={chipStyles.chip}>
            <Avatar user={user} size={28}/>
            <View style={chipStyles.info}>
                <Text style={chipStyles.name} numberOfLines={1}>
                    {user.name}
                </Text>
                <Text style={chipStyles.username}>@{user.username}</Text>
            </View>
            <TouchableOpacity
                style={chipStyles.removeBtn}
                onPress={() => onRemove(user.id)}
                hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            >
                <Text style={chipStyles.removeBtnText}>×</Text>
            </TouchableOpacity>
        </View>
    );
}

const chipStyles = StyleSheet.create({
    chip: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#0B0B0F',
        borderRadius: 12,
        borderWidth: 1,
        borderColor: '#252530',
        paddingVertical: 8,
        paddingHorizontal: 10,
        gap: 8,
        marginBottom: 8,
    },
    info: { flex: 1 },
    name: { fontSize: 13, fontWeight: '600', color: '#F0EBE1' },
    username: { fontSize: 11, color: '#6B7280', marginTop: 1 },
    removeBtn: {
        width: 22,
        height: 22,
        borderRadius: 11,
        backgroundColor: '#252530',
        justifyContent: 'center',
        alignItems: 'center',
    },
    removeBtnText: { color: '#9CA3AF', fontSize: 16, lineHeight: 20, fontWeight: '400' },
});

interface Props {
    selectedUsers: UserSearchDto[];
    onAdd: (user: UserSearchDto) => void;
    onRemove: (id: number) => void;
}

export function ParticipantSearch({ selectedUsers, onAdd, onRemove }: Props) {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<UserSearchDto[]>([]);
    const [searching, setSearching] = useState(false);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        if (debounceTimer.current) clearTimeout(debounceTimer.current);

        if (!query.trim()) {
            setResults([]);
            setDropdownOpen(false);
            return;
        }

        debounceTimer.current = setTimeout(async () => {
            setSearching(true);
            try {
                const data = await userSearchService.searchUsers(query);
                setResults(data);
                setDropdownOpen(true);
            } catch {
                setResults([]);
            } finally {
                setSearching(false);
            }
        }, 400);

        return () => {
            if (debounceTimer.current) clearTimeout(debounceTimer.current);
        };
    }, [query]);

    const handleAdd = (user: UserSearchDto) => {
        onAdd(user);
        setQuery('');
        setResults([]);
        setDropdownOpen(false);
        Keyboard.dismiss();
    };

    const selectedIds = new Set(selectedUsers.map((u) => u.id));

    return (
        <View style={searchStyles.container}>
            <Text style={searchStyles.label}>PARTICIPANTS *</Text>

            {/* Search input */}
            <View style={searchStyles.inputWrapper}>
                <Text style={searchStyles.searchIcon}>🔍</Text>
                <TextInput
                    style={searchStyles.input}
                    value={query}
                    onChangeText={setQuery}
                    placeholder="Search by name, username or email…"
                    placeholderTextColor="#6B7280"
                    autoCapitalize="none"
                    autoCorrect={false}
                    returnKeyType="search"
                />
                {searching && (
                    <ActivityIndicator
                        size="small"
                        color={primaryColor}
                        style={searchStyles.spinner}
                    />
                )}
                {query.length > 0 && !searching && (
                    <TouchableOpacity
                        onPress={() => {
                            setQuery('');
                            setResults([]);
                            setDropdownOpen(false);
                        }}
                        hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                    >
                        <Text style={searchStyles.clearBtn}>×</Text>
                    </TouchableOpacity>
                )}
            </View>

            {/* Dropdown results */}
            {dropdownOpen && results.length > 0 && (
                <View style={searchStyles.dropdown}>
                    <ScrollView
                        keyboardShouldPersistTaps="handled"
                        nestedScrollEnabled
                        style={{ maxHeight: 220 }}
                        showsVerticalScrollIndicator={false}
                    >
                        {results.map((user) => (
                            <SearchResultRow
                                key={user.id}
                                user={user}
                                onAdd={handleAdd}
                                alreadyAdded={selectedIds.has(user.id)}
                            />
                        ))}
                    </ScrollView>
                </View>
            )}

            {dropdownOpen && results.length === 0 && !searching && query.trim().length > 0 && (
                <View style={searchStyles.emptyDropdown}>
                    <Text style={searchStyles.emptyText}>No users found for "{query}"</Text>
                </View>
            )}

            {/* Selected participants */}
            {selectedUsers.length > 0 && (
                <View style={searchStyles.selectedSection}>
                    <Text style={searchStyles.selectedLabel}>
                        {selectedUsers.length} participant{selectedUsers.length !== 1 ? 's' : ''} added
                    </Text>
                    {selectedUsers.map((user) => (
                        <ParticipantChip key={user.id} user={user} onRemove={onRemove}/>
                    ))}
                </View>
            )}

            {selectedUsers.length === 0 && (
                <Text style={searchStyles.hint}>Search and add at least one participant</Text>
            )}
        </View>
    );
}

const searchStyles = StyleSheet.create({
    container: { marginBottom: 20 },
    label: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
        marginBottom: 8,
    },
    inputWrapper: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#0B0B0F',
        borderWidth: 1,
        borderColor: '#252530',
        borderRadius: 12,
        paddingHorizontal: 12,
        gap: 8,
    },
    searchIcon: { fontSize: 15 },
    input: {
        flex: 1,
        color: '#F0EBE1',
        fontSize: 15,
        paddingVertical: 14,
    },
    spinner: { marginRight: 4 },
    clearBtn: { color: '#6B7280', fontSize: 20, lineHeight: 24, marginRight: 2 },

    dropdown: {
        backgroundColor: '#16161D',
        borderWidth: 1,
        borderColor: '#1F1F2E',
        borderRadius: 12,
        marginTop: 4,
        overflow: 'hidden',
    },
    emptyDropdown: {
        backgroundColor: '#16161D',
        borderWidth: 1,
        borderColor: '#1F1F2E',
        borderRadius: 12,
        marginTop: 4,
        padding: 16,
        alignItems: 'center',
    },
    emptyText: { color: '#6B7280', fontSize: 13 },

    selectedSection: { marginTop: 12 },
    selectedLabel: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.2,
        marginBottom: 8,
    },
    hint: { fontSize: 11, color: '#4B5563', marginTop: 6 },
});