package com.archix.app.data.model

enum class OperationType {
    COMPRESS,
    EXTRACT
}

data class OperationHistoryItem(
    val id: String,
    val type: OperationType,
    val sourceName: String,
    val targetPath: String,
    val timestamp: Long,
    val isSuccess: Boolean,
    val details: String = ""
)
