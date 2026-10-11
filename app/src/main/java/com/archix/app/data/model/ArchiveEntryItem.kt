package com.archix.app.data.model

data class ArchiveEntryItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val uncompressedSize: Long = 0L,
    val compressedSize: Long = 0L,
    val lastModified: Long = 0L,
    val isEncrypted: Boolean = false
)
