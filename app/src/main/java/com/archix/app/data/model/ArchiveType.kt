package com.archix.app.data.model

enum class ArchiveType(val displayName: String, val extension: String, val hexColor: Long) {
    ZIP("ZIP", "zip", 0xFF1677FF),
    RAR("RAR", "rar", 0xFF8B5CF6),
    SEVEN_Z("7Z", "7z", 0xFF10B981),
    TAR("TAR", "tar", 0xFF64748B),
    GZ("GZ", "gz", 0xFF0EA5E9)
}
