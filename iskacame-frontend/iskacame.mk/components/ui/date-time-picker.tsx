import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    Modal,
    Platform,
} from 'react-native';
import RNDateTimePicker, {
    DateTimePickerEvent,
} from '@react-native-community/datetimepicker';
import { primaryColor } from '@/constants/theme';

function pad(n: number) {
    return String(n).padStart(2, '0');
}

export function toLocalDateTimeString(d: Date): string {
    return (
        `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
        `T${pad(d.getHours())}:${pad(d.getMinutes())}:00`
    );
}

function formatLabel(d: Date): string {
    return d.toLocaleDateString('en-GB', {
            day: 'numeric',
            month: 'short',
            year: 'numeric',
        }) +
        '  ·  ' +
        d.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
}

function IOSPicker({
                       label,
                       value,
                       onChange,
                       minimumDate,
                   }: {
    label: string;
    value: Date;
    onChange: (d: Date) => void;
    minimumDate?: Date;
}) {
    const [open, setOpen] = useState(false);
    const [mode, setMode] = useState<'date' | 'time'>('date');
    const [temp, setTemp] = useState(value);

    const onPickerChange = (_: DateTimePickerEvent, selected?: Date) => {
        if (selected) setTemp(selected);
    };

    const handleNext = () => setMode('time');

    const handleDone = () => {
        onChange(temp);
        setOpen(false);
        setMode('date');
    };

    const handleCancel = () => {
        setTemp(value);
        setOpen(false);
        setMode('date');
    };

    return (
        <View style={styles.field}>
            <Text style={styles.label}>{label}</Text>
            <TouchableOpacity style={styles.pickerBtn} onPress={() => setOpen(true)} activeOpacity={0.75}>
                <Text style={styles.pickerBtnText}>{formatLabel(value)}</Text>
                <Text style={styles.pickerChevron}>▾</Text>
            </TouchableOpacity>

            <Modal visible={open} transparent animationType="fade">
                <View style={styles.modalBackdrop}>
                    <View style={styles.modalCard}>
                        <View style={styles.modalHeader}>
                            <Text style={styles.modalTitle}>
                                {mode === 'date' ? 'Pick a date' : 'Pick a time'}
                            </Text>
                            <Text style={styles.modalStep}>{mode === 'date' ? '1 / 2' : '2 / 2'}</Text>
                        </View>

                        <RNDateTimePicker
                            value={temp}
                            mode={mode}
                            display="spinner"
                            onChange={onPickerChange}
                            minimumDate={mode === 'date' ? minimumDate : undefined}
                            textColor="#F0EBE1"
                            style={styles.spinner}
                        />

                        <View style={styles.modalActions}>
                            <TouchableOpacity style={styles.modalBtnSecondary} onPress={handleCancel}>
                                <Text style={styles.modalBtnSecondaryText}>Cancel</Text>
                            </TouchableOpacity>
                            {mode === 'date' ? (
                                <TouchableOpacity style={styles.modalBtnPrimary} onPress={handleNext}>
                                    <Text style={styles.modalBtnPrimaryText}>Next →</Text>
                                </TouchableOpacity>
                            ) : (
                                <TouchableOpacity style={styles.modalBtnPrimary} onPress={handleDone}>
                                    <Text style={styles.modalBtnPrimaryText}>Done</Text>
                                </TouchableOpacity>
                            )}
                        </View>
                    </View>
                </View>
            </Modal>
        </View>
    );
}

function AndroidPicker({
                           label,
                           value,
                           onChange,
                           minimumDate,
                       }: {
    label: string;
    value: Date;
    onChange: (d: Date) => void;
    minimumDate?: Date;
}) {
    const [showDate, setShowDate] = useState(false);
    const [showTime, setShowTime] = useState(false);
    const [tempDate, setTempDate] = useState(value);

    const onDateChange = (_: DateTimePickerEvent, selected?: Date) => {
        setShowDate(false);
        if (selected) {
            setTempDate(selected);
            setShowTime(true);
        }
    };

    const onTimeChange = (_: DateTimePickerEvent, selected?: Date) => {
        setShowTime(false);
        if (selected) onChange(selected);
    };

    return (
        <View style={styles.field}>
            <Text style={styles.label}>{label}</Text>
            <TouchableOpacity
                style={styles.pickerBtn}
                onPress={() => {
                    setTempDate(value);
                    setShowDate(true);
                }}
                activeOpacity={0.75}
            >
                <Text style={styles.pickerBtnText}>{formatLabel(value)}</Text>
                <Text style={styles.pickerChevron}>▾</Text>
            </TouchableOpacity>

            {showDate && (
                <RNDateTimePicker
                    value={tempDate}
                    mode="date"
                    display="default"
                    onChange={onDateChange}
                    minimumDate={minimumDate}
                />
            )}
            {showTime && (
                <RNDateTimePicker
                    value={tempDate}
                    mode="time"
                    display="default"
                    onChange={onTimeChange}
                />
            )}
        </View>
    );
}

export function DateTimePicker({
                                   label,
                                   value,
                                   onChange,
                                   minimumDate,
                               }: {
    label: string;
    value: Date;
    onChange: (d: Date) => void;
    minimumDate?: Date;
}) {
    if (Platform.OS === 'ios') {
        return <IOSPicker label={label} value={value} onChange={onChange} minimumDate={minimumDate} />;
    }
    return <AndroidPicker label={label} value={value} onChange={onChange} minimumDate={minimumDate} />;
}

const styles = StyleSheet.create({
    field: { marginBottom: 20 },
    label: {
        fontSize: 10,
        fontWeight: '700',
        color: '#4B5563',
        letterSpacing: 1.5,
        marginBottom: 8,
    },
    pickerBtn: {
        backgroundColor: '#0B0B0F',
        borderWidth: 1,
        borderColor: '#252530',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 14,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    pickerBtnText: { color: '#F0EBE1', fontSize: 15, fontWeight: '500' },
    pickerChevron: { color: '#6B7280', fontSize: 14 },

    modalBackdrop: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.6)',
        justifyContent: 'flex-end',
    },
    modalCard: {
        backgroundColor: '#16161D',
        borderTopLeftRadius: 24,
        borderTopRightRadius: 24,
        paddingBottom: 36,
        borderTopWidth: 1,
        borderColor: '#1F1F2E',
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 24,
        paddingTop: 20,
        paddingBottom: 4,
    },
    modalTitle: { fontSize: 17, fontWeight: '700', color: '#F0EBE1' },
    modalStep: { fontSize: 13, color: '#6B7280' },
    spinner: { backgroundColor: 'transparent' },
    modalActions: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingHorizontal: 24,
        paddingTop: 12,
        gap: 12,
    },
    modalBtnSecondary: {
        flex: 1,
        backgroundColor: '#1F1F2E',
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#252530',
    },
    modalBtnSecondaryText: { color: '#9CA3AF', fontWeight: '600', fontSize: 15 },
    modalBtnPrimary: {
        flex: 1,
        backgroundColor: primaryColor,
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
    },
    modalBtnPrimaryText: { color: '#0B0B0F', fontWeight: '800', fontSize: 15 },
});
