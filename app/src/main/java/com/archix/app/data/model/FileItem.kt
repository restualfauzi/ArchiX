package com.archix.app.data.model

import android.net.Uri

data class FileItem(
    val name: String,
    val path: String,
    val uri: Uri? = null,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val itemCount: Int = 0,
    val extension: String = "",
    val mimeType: String = "*/*",
    val isFavorite: Boolean = false
) {
    val archiveType: ArchiveType?
        get() = when (extension.lowercase()) {
            "zip" -> ArchiveType.ZIP
            "rar" -> ArchiveType.RAR
            "7z" -> ArchiveType.SEVEN_Z
            "tar" -> ArchiveType.TAR
            "gz", "tgz" -> ArchiveType.GZ
            else -> null
        }

    val isArchive: Boolean
        get() = archiveType != null

    val categoryType: CategoryType
        get() = if (isDirectory) {
            CategoryType.OTHERS
        } else {
            when (extension.lowercase()) {
                "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> CategoryType.IMAGES
                "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "3gp" -> CategoryType.VIDEO
                "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus" -> CategoryType.AUDIO
                "pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx", "csv", "epub" -> CategoryType.DOCUMENTS
                "apk" -> CategoryType.APK
                "zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "xz" -> CategoryType.ARCHIVES
                else -> CategoryType.OTHERS
            }
        }
}
