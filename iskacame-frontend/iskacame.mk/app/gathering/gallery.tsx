import React, { useCallback, useEffect, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    FlatList,
    TouchableOpacity,
    Image,
    ActivityIndicator,
    Alert,
    Modal,
    Dimensions,
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useAuth } from '@/context/auth-context';
import { gatheringService } from '@/service/gathering-service';
import type { GatheringImageDto } from '@/service/dtos/gathering-types';
import { primaryColor } from '@/constants/theme';

const SCREEN_WIDTH = Dimensions.get('window').width;
const IMAGE_GAP = 4;
const NUM_COLUMNS = 3;
const IMAGE_SIZE = (SCREEN_WIDTH - 48 - IMAGE_GAP * (NUM_COLUMNS - 1)) / NUM_COLUMNS;

export default function GalleryScreen() {
    const { gatheringId } = useLocalSearchParams<{ gatheringId: string }>();
    const router = useRouter();
    const { user } = useAuth();
    const [images, setImages] = useState<GatheringImageDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);
    const [previewImage, setPreviewImage] = useState<GatheringImageDto | null>(null);

    const load = useCallback(async () => {
        if (!gatheringId) return;
        try {
            const data = await gatheringService.getGallery(Number(gatheringId));
            setImages(data);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to load gallery.');
        } finally {
            setLoading(false);
        }
    }, [gatheringId]);

    useEffect(() => {
        load();
    }, [load]);

    const handlePickImages = async () => {
        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ImagePicker.MediaTypeOptions.Images,
            allowsMultipleSelection: true,
            quality: 0.5,
            selectionLimit: 5,
        });

        if (result.canceled || result.assets.length === 0) return;

        const uris = result.assets.map((a) => a.uri);
        setUploading(true);
        try {
            const uploaded = await gatheringService.uploadImages(Number(gatheringId), uris);
            setImages((prev) => [...prev, ...uploaded]);
        } catch (e: any) {
            Alert.alert('Error', e.message ?? 'Failed to upload images.');
        } finally {
            setUploading(false);
        }
    };

    const handleDelete = (image: GatheringImageDto) => {
        Alert.alert('Delete Photo', 'Are you sure you want to delete this photo?', [
            { text: 'Cancel', style: 'cancel' },
            {
                text: 'Delete',
                style: 'destructive',
                onPress: async () => {
                    try {
                        await gatheringService.deleteImage(Number(gatheringId), image.id);
                        setImages((prev) => prev.filter((img) => img.id !== image.id));
                        setPreviewImage(null);
                    } catch (e: any) {
                        Alert.alert('Error', e.message ?? 'Failed to delete image.');
                    }
                },
            },
        ]);
    };

    const formatDate = (dateStr: string) => {
        const d = new Date(dateStr);
        return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
    };

    const renderImage = ({ item }: { item: GatheringImageDto }) => (
        <TouchableOpacity
            onPress={() => setPreviewImage(item)}
            activeOpacity={0.85}
        >
            <Image source={{ uri: item.url }} style={styles.thumbnail} />
        </TouchableOpacity>
    );

    if (loading) {
        return (
            <View style={styles.center}>
                <ActivityIndicator color={primaryColor} size="large" />
            </View>
        );
    }

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.topBar}>
                <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
                    <Text style={styles.backText}>{'< Back'}</Text>
                </TouchableOpacity>
                <TouchableOpacity
                    onPress={handlePickImages}
                    disabled={uploading}
                    style={[styles.uploadBtn, uploading && { opacity: 0.5 }]}
                    activeOpacity={0.8}
                >
                    {uploading ? (
                        <ActivityIndicator color="#0B0B0F" size="small" />
                    ) : (
                        <Text style={styles.uploadBtnText}>+ Upload</Text>
                    )}
                </TouchableOpacity>
            </View>

            <Text style={styles.title}>Gallery</Text>
            <Text style={styles.subtitle}>
                {images.length} {images.length === 1 ? 'photo' : 'photos'}
            </Text>

            {images.length === 0 ? (
                <View style={styles.emptyState}>
                    <Text style={styles.emptyTitle}>No photos yet</Text>
                    <Text style={styles.emptySubtitle}>
                        Upload photos to share with the group.
                    </Text>
                </View>
            ) : (
                <FlatList
                    data={images}
                    renderItem={renderImage}
                    keyExtractor={(item) => String(item.id)}
                    numColumns={NUM_COLUMNS}
                    columnWrapperStyle={styles.row}
                    contentContainerStyle={styles.grid}
                    showsVerticalScrollIndicator={false}
                />
            )}

            {/* Full preview modal */}
            {previewImage && (
                <Modal
                    visible={!!previewImage}
                    transparent
                    animationType="fade"
                    onRequestClose={() => setPreviewImage(null)}
                >
                    <View style={previewStyles.overlay}>
                        <View style={previewStyles.header}>
                            <View style={{ flex: 1 }}>
                                <Text style={previewStyles.uploaderName}>
                                    {previewImage.uploader.name}
                                </Text>
                                <Text style={previewStyles.date}>
                                    {formatDate(previewImage.uploadedAt)}
                                </Text>
                            </View>
                            <TouchableOpacity
                                onPress={() => setPreviewImage(null)}
                                style={previewStyles.closeBtn}
                            >
                                <Text style={previewStyles.closeBtnText}>X</Text>
                            </TouchableOpacity>
                        </View>

                        <View style={previewStyles.imageContainer}>
                            <Image
                                source={{ uri: previewImage.url }}
                                style={previewStyles.image}
                                resizeMode="contain"
                            />
                        </View>

                        {previewImage.uploader.id === user?.id && (
                            <TouchableOpacity
                                style={previewStyles.deleteBtn}
                                onPress={() => handleDelete(previewImage)}
                                activeOpacity={0.8}
                            >
                                <Text style={previewStyles.deleteBtnText}>Delete</Text>
                            </TouchableOpacity>
                        )}
                    </View>
                </Modal>
            )}
        </View>
    );
}

const previewStyles = StyleSheet.create({
    overlay: {
        flex: 1,
        backgroundColor: '#0B0B0F',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingTop: 56,
        paddingHorizontal: 24,
        paddingBottom: 12,
        gap: 12,
    },
    uploaderName: {
        fontSize: 15,
        fontWeight: '700',
        color: '#F0EBE1',
    },
    date: {
        fontSize: 12,
        color: '#6B7280',
        marginTop: 2,
    },
    closeBtn: {
        width: 32,
        height: 32,
        borderRadius: 16,
        backgroundColor: '#16161D',
        justifyContent: 'center',
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#252530',
    },
    closeBtnText: {
        color: '#6B7280',
        fontWeight: '700',
        fontSize: 14,
    },
    imageContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    image: {
        width: '100%',
        height: '100%',
    },
    deleteBtn: {
        backgroundColor: '#EF4444',
        margin: 24,
        marginBottom: 40,
        borderRadius: 12,
        paddingVertical: 14,
        alignItems: 'center',
    },
    deleteBtnText: {
        color: '#FFFFFF',
        fontWeight: '800',
        fontSize: 15,
    },
});

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0B0B0F',
        paddingTop: 56,
        paddingHorizontal: 24,
    },
    center: {
        flex: 1,
        backgroundColor: '#0B0B0F',
        justifyContent: 'center',
        alignItems: 'center',
    },
    topBar: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 24,
    },
    backBtn: {},
    backText: {
        color: primaryColor,
        fontSize: 16,
        fontWeight: '600',
    },
    uploadBtn: {
        backgroundColor: primaryColor,
        borderRadius: 20,
        paddingHorizontal: 16,
        paddingVertical: 8,
    },
    uploadBtnText: {
        color: '#0B0B0F',
        fontWeight: '800',
        fontSize: 14,
    },
    title: {
        fontSize: 26,
        fontWeight: '800',
        color: '#F0EBE1',
        letterSpacing: -0.5,
        marginBottom: 4,
    },
    subtitle: {
        fontSize: 14,
        color: '#6B7280',
        marginBottom: 20,
    },
    emptyState: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    emptyTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#F0EBE1',
        marginBottom: 4,
    },
    emptySubtitle: {
        fontSize: 14,
        color: '#6B7280',
        textAlign: 'center',
    },
    grid: {
        paddingBottom: 24,
    },
    row: {
        gap: IMAGE_GAP,
        marginBottom: IMAGE_GAP,
    },
    thumbnail: {
        width: IMAGE_SIZE,
        height: IMAGE_SIZE,
        borderRadius: 8,
        backgroundColor: '#16161D',
    },
});
