package com.archix.app.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups.coerceAtMost(units.size - 1)])
            .replace(".0 ", " ")
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Prevents Zip Slip (Directory Traversal) Vulnerability when extracting files.
     */
    fun isSafePath(targetDir: File, targetFile: File): Boolean {
        val canonicalTargetDir = targetDir.canonicalPath
        val canonicalTargetFile = targetFile.canonicalPath
        return canonicalTargetFile.startsWith(canonicalTargetDir + File.separator) || canonicalTargetFile == canonicalTargetDir
    }
}
