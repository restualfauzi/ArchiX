package com.archix.app.data.repository

import android.content.Context
import android.os.Environment
import com.archix.app.data.model.CategoryType
import com.archix.app.data.model.FileItem
import com.archix.app.data.model.SortOrder
import com.archix.app.data.model.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileRepository(private val context: Context) {

    fun getRootDirectory(): File {
        return Environment.getExternalStorageDirectory()
    }

    suspend fun getFiles(
        directory: File,
        sortType: SortType = SortType.NAME,
        sortOrder: SortOrder = SortOrder.ASCENDING,
        favorites: Set<String> = emptySet()
    ): List<FileItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) return@withContext emptyList()

        val rawFiles = directory.listFiles() ?: return@withContext emptyList()

        val items = rawFiles.map { file ->
            val isDir = file.isDirectory
            val itemCount = if (isDir) (file.listFiles()?.size ?: 0) else 0
            val ext = if (isDir) "" else file.extension.lowercase()

            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = isDir,
                size = if (isDir) 0L else file.length(),
                lastModified = file.lastModified(),
                itemCount = itemCount,
                extension = ext,
                isFavorite = favorites.contains(file.absolutePath)
            )
        }

        val sorted = when (sortType) {
            SortType.NAME -> items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            SortType.SIZE -> items.sortedWith(compareBy({ !it.isDirectory }, { it.size }))
            SortType.DATE -> items.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified }))
            SortType.TYPE -> items.sortedWith(compareBy({ !it.isDirectory }, { it.extension }, { it.name.lowercase() }))
        }

        if (sortOrder == SortOrder.DESCENDING) {
            val directories = sorted.filter { it.isDirectory }.reversed()
            val files = sorted.filter { !it.isDirectory }.reversed()
            directories + files
        } else {
            sorted
        }
    }

    suspend fun createDirectory(parentDirectory: File, folderName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val newDir = File(parentDirectory, folderName)
            if (newDir.exists()) {
                Result.failure(Exception("Folder with name '$folderName' already exists"))
            } else if (newDir.mkdirs()) {
                Result.success(newDir)
            } else {
                Result.failure(Exception("Failed to create folder '$folderName'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameFile(file: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val parent = file.parentFile ?: return@withContext Result.failure(Exception("Invalid parent directory"))
            val newFile = File(parent, newName)
            if (newFile.exists()) {
                return@withContext Result.failure(Exception("File with name '$newName' already exists"))
            }
            if (file.renameTo(newFile)) {
                Result.success(newFile)
            } else {
                Result.failure(Exception("Failed to rename file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (file.isDirectory) {
                Result.success(file.deleteRecursively())
            } else {
                Result.success(file.delete())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun copyFile(source: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (source.isDirectory) {
                val targetSubDir = File(targetDir, source.name)
                source.copyRecursively(targetSubDir, overwrite = true)
                Result.success(targetSubDir)
            } else {
                val targetFile = File(targetDir, source.name)
                source.inputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success(targetFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun moveFile(source: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(targetDir, source.name)
            if (source.renameTo(targetFile)) {
                Result.success(targetFile)
            } else {
                val copyResult = copyFile(source, targetDir)
                if (copyResult.isSuccess) {
                    deleteFile(source)
                    Result.success(targetFile)
                } else {
                    Result.failure(Exception("Failed to move file"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFiles(
        query: String,
        categoryFilter: CategoryType? = null,
        directory: File = getRootDirectory()
    ): List<FileItem> = withContext(Dispatchers.IO) {
        if (query.isBlank() && categoryFilter == null) return@withContext emptyList()

        val results = mutableListOf<FileItem>()
        scanSearch(directory, query.trim().lowercase(), categoryFilter, results, maxDepth = 4, currentDepth = 0)
        results
    }

    private fun scanSearch(
        directory: File,
        queryLower: String,
        categoryFilter: CategoryType?,
        results: MutableList<FileItem>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                if (file.name.equals("Android", ignoreCase = true) && currentDepth == 0) continue
                if (queryLower.isNotEmpty() && file.name.lowercase().contains(queryLower) && categoryFilter == null) {
                    results.add(
                        FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = true,
                            itemCount = file.listFiles()?.size ?: 0
                        )
                    )
                }
                scanSearch(file, queryLower, categoryFilter, results, maxDepth, currentDepth + 1)
            } else {
                val item = FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = false,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    extension = file.extension.lowercase()
                )

                val matchesQuery = queryLower.isEmpty() || file.name.lowercase().contains(queryLower) || file.extension.lowercase().contains(queryLower)
                val matchesCategory = categoryFilter == null || item.categoryType == categoryFilter

                if (matchesQuery && matchesCategory) {
                    results.add(item)
                }
            }
        }
    }

    suspend fun getRecentFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val root = getRootDirectory()
        val allFiles = mutableListOf<FileItem>()
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

        scanRecent(root, sevenDaysAgo, allFiles, maxDepth = 3, currentDepth = 0)
        allFiles.sortedByDescending { it.lastModified }.take(30)
    }

    private fun scanRecent(
        directory: File,
        sinceTimestamp: Long,
        results: MutableList<FileItem>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                if (file.name.equals("Android", ignoreCase = true) && currentDepth == 0) continue
                scanRecent(file, sinceTimestamp, results, maxDepth, currentDepth + 1)
            } else {
                if (file.lastModified() >= sinceTimestamp) {
                    results.add(
                        FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = false,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            extension = file.extension.lowercase()
                        )
                    )
                }
            }
        }
    }

    suspend fun getLargeFiles(minSizeBytes: Long = 50 * 1024 * 1024L): List<FileItem> = withContext(Dispatchers.IO) {
        val root = getRootDirectory()
        val results = mutableListOf<FileItem>()

        scanLarge(root, minSizeBytes, results, maxDepth = 4, currentDepth = 0)
        results.sortedByDescending { it.size }.take(30)
    }

    private fun scanLarge(
        directory: File,
        minSizeBytes: Long,
        results: MutableList<FileItem>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                if (file.name.equals("Android", ignoreCase = true) && currentDepth == 0) continue
                scanLarge(file, minSizeBytes, results, maxDepth, currentDepth + 1)
            } else {
                if (file.length() >= minSizeBytes) {
                    results.add(
                        FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = false,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            extension = file.extension.lowercase()
                        )
                    )
                }
            }
        }
    }

    suspend fun getArchiveFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val root = getRootDirectory()
        val results = mutableListOf<FileItem>()

        scanArchives(root, results, maxDepth = 4, currentDepth = 0)
        results.sortedByDescending { it.lastModified }
    }

    private fun scanArchives(
        directory: File,
        results: MutableList<FileItem>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                if (file.name.equals("Android", ignoreCase = true) && currentDepth == 0) continue
                scanArchives(file, results, maxDepth, currentDepth + 1)
            } else {
                val ext = file.extension.lowercase()
                val isArch = ext in listOf("zip", "rar", "7z", "tar", "gz", "tgz")
                if (isArch) {
                    results.add(
                        FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = false,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            extension = ext
                        )
                    )
                }
            }
        }
    }
}
