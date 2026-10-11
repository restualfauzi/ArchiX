package com.archix.app.data.model

enum class CategoryType(
    val titleId: String,
    val titleEn: String,
    val hexColor: Long
) {
    IMAGES("Gambar", "Images", 0xFF1677FF),
    VIDEO("Video", "Video", 0xFF8B5CF6),
    AUDIO("Audio", "Audio", 0xFFF97316),
    DOCUMENTS("Dokumen", "Documents", 0xFFEF4444),
    APK("APK", "APK", 0xFF10B981),
    ARCHIVES("Arsip", "Archives", 0xFF3B82F6),
    OTHERS("Lainnya", "Others", 0xFF64748B)
}
