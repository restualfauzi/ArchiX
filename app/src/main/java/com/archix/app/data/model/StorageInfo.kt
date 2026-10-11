package com.archix.app.data.model

data class StorageInfo(
    val usedBytes: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val percentageUsed: Int
)
