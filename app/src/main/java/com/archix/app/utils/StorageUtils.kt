package com.archix.app.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.archix.app.data.model.CategoryType
import com.archix.app.data.model.FileItem
import com.archix.app.data.model.StorageInfo
import java.io.File

object StorageUtils {

    fun getStorageInfo(context: Context): StorageInfo {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val freeBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - freeBytes

        val percentageUsed = if (totalBytes > 0) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
        } else {
            0
        }

        return StorageInfo(
            usedBytes = usedBytes,
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            percentageUsed = percentageUsed
        )
    }

    fun getPrimaryFolders(): List<FileItem> {
        val storageDir = Environment.getExternalStorageDirectory()
        val folderNames = listOf("DCIM", "Download", "Documents", "Movies", "Music", "Pictures")
        
        return folderNames.mapNotNull { folderName ->
            val folder = File(storageDir, folderName)
            if (folder.exists() && folder.isDirectory) {
                val files = folder.listFiles()
                FileItem(
                    name = folder.name,
                    path = folder.absolutePath,
                    isDirectory = true,
                    size = folder.length(),
                    lastModified = folder.lastModified(),
                    itemCount = files?.size ?: 0
                )
            } else {
                null
            }
        }
    }

    fun getCategoryCounts(context: Context): Map<CategoryType, Pair<Int, Long>> {
        val counts = mutableMapOf<CategoryType, Pair<Int, Long>>()
        CategoryType.entries.forEach { counts[it] = Pair(0, 0L) }

        val storageDir = Environment.getExternalStorageDirectory()
        if (storageDir.exists() && storageDir.isDirectory) {
            scanDirectory(storageDir, counts, maxDepth = 3, currentDepth = 0)
        }

        return counts
    }

    private fun scanDirectory(
        directory: File,
        counts: MutableMap<CategoryType, Pair<Int, Long>>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                // Avoid scanning Android/data or system folders to remain fast and safe
                if (file.name.equals("Android", ignoreCase = true) && currentDepth == 0) continue
                scanDirectory(file, counts, maxDepth, currentDepth + 1)
            } else {
                val ext = file.extension.lowercase()
                val item = FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = false,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    extension = ext
                )
                val cat = item.categoryType
                val current = counts[cat] ?: Pair(0, 0L)
                counts[cat] = Pair(current.first + 1, current.second + file.length())
            }
        }
    }
}
