package com.archix.app.data.repository

import com.archix.app.data.model.ArchiveEntryItem
import com.archix.app.utils.FileUtils
import com.github.junrar.Archive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ArchiveRepository {

    suspend fun createZip(
        sourceFiles: List<File>,
        outputZipFile: File,
        onProgress: (progress: Float, currentFileName: String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (outputZipFile.exists()) outputZipFile.delete()

            val totalFilesCount = countTotalFiles(sourceFiles)
            var processedCount = 0

            ZipOutputStream(FileOutputStream(outputZipFile).buffered()).use { zipOut ->
                for (source in sourceFiles) {
                    compressFileToZip(source, "", zipOut) { name ->
                        processedCount++
                        val progress = if (totalFilesCount > 0) processedCount.toFloat() / totalFilesCount else 0f
                        onProgress(progress, name)
                    }
                }
            }
            Result.success(outputZipFile)
        } catch (e: Exception) {
            if (outputZipFile.exists()) outputZipFile.delete()
            Result.failure(e)
        }
    }

    private fun countTotalFiles(files: List<File>): Int {
        var count = 0
        for (f in files) {
            if (f.isDirectory) {
                val subFiles = f.listFiles() ?: emptyArray()
                count += countTotalFiles(subFiles.toList())
            } else {
                count++
            }
        }
        return count
    }

    private fun compressFileToZip(
        file: File,
        parentPath: String,
        zipOut: ZipOutputStream,
        onFileAdded: (String) -> Unit
    ) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        if (file.isDirectory) {
            val dirEntry = ZipEntry("$entryName/")
            dirEntry.time = file.lastModified()
            zipOut.putNextEntry(dirEntry)
            zipOut.closeEntry()

            val children = file.listFiles() ?: return
            for (child in children) {
                compressFileToZip(child, entryName, zipOut, onFileAdded)
            }
        } else {
            val fileEntry = ZipEntry(entryName)
            fileEntry.time = file.lastModified()
            zipOut.putNextEntry(fileEntry)

            file.inputStream().buffered().use { input ->
                input.copyTo(zipOut)
            }
            zipOut.closeEntry()
            onFileAdded(file.name)
        }
    }

    suspend fun extractArchive(
        archiveFile: File,
        targetDir: File,
        onProgress: (progress: Float, currentFileName: String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!targetDir.exists()) targetDir.mkdirs()

            val ext = archiveFile.extension.lowercase()
            when (ext) {
                "zip" -> extractZip(archiveFile, targetDir, onProgress)
                "rar" -> extractRar(archiveFile, targetDir, onProgress)
                "7z" -> extract7z(archiveFile, targetDir, onProgress)
                "tar" -> extractTar(archiveFile, targetDir, onProgress)
                "gz", "tgz" -> extractGz(archiveFile, targetDir, onProgress)
                else -> Result.failure(Exception("Unsupported archive format: .$ext"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractZip(
        archiveFile: File,
        targetDir: File,
        onProgress: (Float, String) -> Unit
    ): Result<File> {
        val zipFile = ZipFile(archiveFile)
        val entriesCount = zipFile.size()
        var extracted = 0

        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val outFile = File(targetDir, entry.name)

            // Zip Slip protection
            if (!FileUtils.isSafePath(targetDir, outFile)) {
                throw SecurityException("Zip Slip detected in entry: ${entry.name}")
            }

            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                zipFile.getInputStream(entry).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            extracted++
            onProgress(extracted.toFloat() / entriesCount, entry.name)
        }
        zipFile.close()
        return Result.success(targetDir)
    }

    private fun extractRar(
        archiveFile: File,
        targetDir: File,
        onProgress: (Float, String) -> Unit
    ): Result<File> {
        val archive = Archive(archiveFile)
        val headers = archive.fileHeaders
        val total = headers.size
        var count = 0

        for (header in headers) {
            val fileName = header.fileName.replace('\\', '/')
            val outFile = File(targetDir, fileName)

            if (!FileUtils.isSafePath(targetDir, outFile)) {
                throw SecurityException("Zip Slip detected in RAR entry: $fileName")
            }

            if (header.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { outStream ->
                    archive.extractFile(header, outStream)
                }
            }
            count++
            onProgress(count.toFloat() / total, fileName)
        }
        archive.close()
        return Result.success(targetDir)
    }

    private fun extract7z(
        archiveFile: File,
        targetDir: File,
        onProgress: (Float, String) -> Unit
    ): Result<File> {
        val sevenZFile = SevenZFile(archiveFile)
        var count = 0
        val entries = sevenZFile.entries.toList()
        val total = entries.size

        for (entry in entries) {
            val outFile = File(targetDir, entry.name)
            if (!FileUtils.isSafePath(targetDir, outFile)) {
                throw SecurityException("Zip Slip detected in 7Z entry: ${entry.name}")
            }

            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { output ->
                    val buffer = ByteArray(8192) // 8KB buffer
                    var bytesRemaining = entry.size
                    while (bytesRemaining > 0) {
                        val bytesToRead = minOf(buffer.size.toLong(), bytesRemaining).toInt()
                        val bytesRead = sevenZFile.read(buffer, 0, bytesToRead)
                        if (bytesRead <= 0) break
                        output.write(buffer, 0, bytesRead)
                        bytesRemaining -= bytesRead
                    }
                }
            }
            count++
            onProgress(count.toFloat() / total, entry.name)
        }
        sevenZFile.close()
        return Result.success(targetDir)
    }

    private fun extractTar(
        archiveFile: File,
        targetDir: File,
        onProgress: (Float, String) -> Unit
    ): Result<File> {
        TarArchiveInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { tarIn ->
            var entry = tarIn.nextEntry
            var count = 0
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (!FileUtils.isSafePath(targetDir, outFile)) {
                    throw SecurityException("Zip Slip detected in TAR entry: ${entry.name}")
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { output ->
                        tarIn.copyTo(output)
                    }
                }
                count++
                onProgress(count.toFloat() / 100, entry.name)
                entry = tarIn.nextEntry
            }
        }
        return Result.success(targetDir)
    }

    private fun extractGz(
        archiveFile: File,
        targetDir: File,
        onProgress: (Float, String) -> Unit
    ): Result<File> {
        val outFileName = archiveFile.nameWithoutExtension
        val outFile = File(targetDir, outFileName)
        if (!FileUtils.isSafePath(targetDir, outFile)) {
            throw SecurityException("Zip Slip detected in GZ file")
        }

        GzipCompressorInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzIn ->
            FileOutputStream(outFile).use { output ->
                gzIn.copyTo(output)
            }
        }
        onProgress(1.0f, outFileName)
        return Result.success(targetDir)
    }

    suspend fun getArchiveContents(archiveFile: File): Result<List<ArchiveEntryItem>> = withContext(Dispatchers.IO) {
        try {
            val ext = archiveFile.extension.lowercase()
            val list = when (ext) {
                "zip" -> getZipContents(archiveFile)
                "rar" -> getRarContents(archiveFile)
                "7z" -> get7zContents(archiveFile)
                "tar" -> getTarContents(archiveFile)
                "gz", "tgz" -> listOf(
                    ArchiveEntryItem(
                        name = archiveFile.nameWithoutExtension,
                        path = archiveFile.nameWithoutExtension,
                        isDirectory = false,
                        uncompressedSize = archiveFile.length()
                    )
                )
                else -> emptyList()
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getZipContents(archiveFile: File): List<ArchiveEntryItem> {
        val zipFile = ZipFile(archiveFile)
        val result = mutableListOf<ArchiveEntryItem>()
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val isDir = entry.isDirectory
            val name = if (isDir) entry.name.trimEnd('/') else entry.name
            val simpleName = name.substringAfterLast('/')

            result.add(
                ArchiveEntryItem(
                    name = if (simpleName.isEmpty()) name else simpleName,
                    path = entry.name,
                    isDirectory = isDir,
                    uncompressedSize = entry.size,
                    compressedSize = entry.compressedSize,
                    lastModified = entry.time
                )
            )
        }
        zipFile.close()
        return result
    }

    private fun getRarContents(archiveFile: File): List<ArchiveEntryItem> {
        val archive = Archive(archiveFile)
        val result = mutableListOf<ArchiveEntryItem>()
        val headers = archive.fileHeaders

        for (header in headers) {
            val fileName = header.fileName.replace('\\', '/')
            val isDir = header.isDirectory
            val simpleName = fileName.substringAfterLast('/')

            result.add(
                ArchiveEntryItem(
                    name = if (simpleName.isEmpty()) fileName else simpleName,
                    path = fileName,
                    isDirectory = isDir,
                    uncompressedSize = header.unpSize,
                    compressedSize = header.packSize,
                    isEncrypted = header.isEncrypted
                )
            )
        }
        archive.close()
        return result
    }

    private fun get7zContents(archiveFile: File): List<ArchiveEntryItem> {
        val sevenZFile = SevenZFile(archiveFile)
        val result = mutableListOf<ArchiveEntryItem>()
        val entries = sevenZFile.entries

        for (entry in entries) {
            val isDir = entry.isDirectory
            val simpleName = entry.name.substringAfterLast('/')

            result.add(
                ArchiveEntryItem(
                    name = if (simpleName.isEmpty()) entry.name else simpleName,
                    path = entry.name,
                    isDirectory = isDir,
                    uncompressedSize = entry.size,
                    compressedSize = entry.size
                )
            )
        }
        sevenZFile.close()
        return result
    }

    private fun getTarContents(archiveFile: File): List<ArchiveEntryItem> {
        val result = mutableListOf<ArchiveEntryItem>()
        TarArchiveInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { tarIn ->
            var entry = tarIn.nextEntry
            while (entry != null) {
                val isDir = entry.isDirectory
                val simpleName = entry.name.substringAfterLast('/')
                result.add(
                    ArchiveEntryItem(
                        name = if (simpleName.isEmpty()) entry.name else simpleName,
                        path = entry.name,
                        isDirectory = isDir,
                        uncompressedSize = entry.size,
                        compressedSize = entry.size
                    )
                )
                entry = tarIn.nextEntry
            }
        }
        return result
    }
}
