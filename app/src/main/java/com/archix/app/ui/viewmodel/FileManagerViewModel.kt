package com.archix.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archix.app.data.local.DataStoreManager
import com.archix.app.data.model.FileItem
import com.archix.app.data.model.SortOrder
import com.archix.app.data.model.SortType
import com.archix.app.data.model.ViewMode
import com.archix.app.data.repository.ArchiveRepository
import com.archix.app.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

data class FileManagerUiState(
    val currentDirectory: File,
    val breadcrumbs: List<File> = emptyList(),
    val files: List<FileItem> = emptyList(),
    val selectedFiles: Set<FileItem> = emptySet(),
    val sortType: SortType = SortType.NAME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val viewMode: ViewMode = ViewMode.LIST,
    val favorites: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val operationProgress: Float = 0f,
    val operationStatus: String = "",
    val message: String? = null,
    val error: String? = null,
    val isCopyingOrMoving: Boolean = false,
    val clipboardFiles: List<File> = emptyList(),
    val isCutOperation: Boolean = false
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)
    private val archiveRepository = ArchiveRepository()
    private val dataStoreManager = DataStoreManager(application)

    private val rootDir = fileRepository.getRootDirectory()

    fun getRootDirectory(): File = rootDir

    private val _uiState = MutableStateFlow(FileManagerUiState(currentDirectory = rootDir))
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    init {
        observeFavoritesAndSettings()
        loadDirectory(rootDir)
    }

    private fun observeFavoritesAndSettings() {
        viewModelScope.launch {
            dataStoreManager.favoritesFlow.collectLatest { favs ->
                _uiState.value = _uiState.value.copy(favorites = favs)
                refreshCurrentDirectory()
            }
        }
        viewModelScope.launch {
            dataStoreManager.appSettingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(
                    sortType = settings.defaultSortType,
                    viewMode = settings.defaultViewMode
                )
                refreshCurrentDirectory()
            }
        }
    }

    fun loadDirectory(directory: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedFiles = emptySet())
            val breadcrumbs = generateBreadcrumbs(directory)

            val fileItems = fileRepository.getFiles(
                directory = directory,
                sortType = _uiState.value.sortType,
                sortOrder = _uiState.value.sortOrder,
                favorites = _uiState.value.favorites
            )

            _uiState.value = _uiState.value.copy(
                currentDirectory = directory,
                breadcrumbs = breadcrumbs,
                files = fileItems,
                isLoading = false
            )
        }
    }

    fun refreshCurrentDirectory() {
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun navigateToFolder(folderPath: String) {
        val folder = File(folderPath)
        if (folder.exists() && folder.isDirectory) {
            loadDirectory(folder)
        } else {
            _uiState.value = _uiState.value.copy(error = "Folder does not exist")
        }
    }

    fun navigateUp(): Boolean {
        val current = _uiState.value.currentDirectory
        val parent = current.parentFile
        return if (parent != null && current.absolutePath != rootDir.absolutePath && parent.absolutePath.startsWith(rootDir.absolutePath)) {
            loadDirectory(parent)
            true
        } else if (current.absolutePath != rootDir.absolutePath) {
            loadDirectory(rootDir)
            true
        } else {
            false
        }
    }

    private fun generateBreadcrumbs(directory: File): List<File> {
        val crumbs = mutableListOf<File>()
        var current: File? = directory
        while (current != null && current.absolutePath.startsWith(rootDir.absolutePath)) {
            crumbs.add(0, current)
            if (current.absolutePath == rootDir.absolutePath) break
            current = current.parentFile
        }
        return crumbs
    }

    fun setSortType(type: SortType) {
        val newOrder = if (_uiState.value.sortType == type) {
            if (_uiState.value.sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
        } else {
            SortOrder.ASCENDING
        }
        _uiState.value = _uiState.value.copy(sortType = type, sortOrder = newOrder)
        refreshCurrentDirectory()
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun toggleFileSelection(fileItem: FileItem) {
        val current = _uiState.value.selectedFiles.toMutableSet()
        if (current.contains(fileItem)) {
            current.remove(fileItem)
        } else {
            current.add(fileItem)
        }
        _uiState.value = _uiState.value.copy(selectedFiles = current)
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(selectedFiles = _uiState.value.files.toSet())
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedFiles = emptySet())
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            val result = fileRepository.createDirectory(_uiState.value.currentDirectory, folderName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Folder created: $folderName")
                refreshCurrentDirectory()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to create folder")
            }
        }
    }

    fun renameFile(fileItem: FileItem, newName: String) {
        viewModelScope.launch {
            val file = File(fileItem.path)
            val result = fileRepository.renameFile(file, newName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Renamed successfully")
                refreshCurrentDirectory()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to rename")
            }
        }
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            val targets = _uiState.value.selectedFiles.toList()
            if (targets.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(isOperating = true, operationStatus = "Deleting files...")
            var successCount = 0

            for (item in targets) {
                val file = File(item.path)
                val res = fileRepository.deleteFile(file)
                if (res.isSuccess) successCount++
            }

            _uiState.value = _uiState.value.copy(
                isOperating = false,
                message = "Deleted $successCount file(s)",
                selectedFiles = emptySet()
            )
            refreshCurrentDirectory()
        }
    }

    fun deleteFile(fileItem: FileItem) {
        viewModelScope.launch {
            val res = fileRepository.deleteFile(File(fileItem.path))
            res.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Deleted: ${fileItem.name}")
                refreshCurrentDirectory()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to delete")
            }
        }
    }

    fun compressSelectedToZip(archiveName: String) {
        viewModelScope.launch {
            val selected = _uiState.value.selectedFiles.toList()
            val filesToCompress = if (selected.isNotEmpty()) {
                selected.map { File(it.path) }
            } else {
                return@launch
            }

            val zipFileName = if (archiveName.endsWith(".zip", ignoreCase = true)) archiveName else "$archiveName.zip"
            val outputZip = File(_uiState.value.currentDirectory, zipFileName)

            _uiState.value = _uiState.value.copy(
                isOperating = true,
                operationProgress = 0f,
                operationStatus = "Creating ZIP archive..."
            )

            val result = archiveRepository.createZip(
                sourceFiles = filesToCompress,
                outputZipFile = outputZip
            ) { progress, currentName ->
                _uiState.value = _uiState.value.copy(
                    operationProgress = progress,
                    operationStatus = "Compressing $currentName"
                )
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isOperating = false,
                    message = "Created $zipFileName successfully",
                    selectedFiles = emptySet()
                )
                refreshCurrentDirectory()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isOperating = false,
                    error = e.message ?: "Failed to create ZIP"
                )
            }
        }
    }

    fun toggleFavorite(fileItem: FileItem) {
        viewModelScope.launch {
            dataStoreManager.toggleFavorite(fileItem.path)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun prepareCopy() {
        val selected = _uiState.value.selectedFiles.map { File(it.path) }
        if (selected.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                clipboardFiles = selected,
                isCutOperation = false,
                isCopyingOrMoving = true,
                selectedFiles = emptySet(),
                message = "${selected.size} file disalin. Buka folder tujuan lalu tekan Tempel."
            )
        }
    }

    fun prepareCut() {
        val selected = _uiState.value.selectedFiles.map { File(it.path) }
        if (selected.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                clipboardFiles = selected,
                isCutOperation = true,
                isCopyingOrMoving = true,
                selectedFiles = emptySet(),
                message = "${selected.size} file dipotong. Buka folder tujuan lalu tekan Tempel."
            )
        }
    }

    fun pasteClipboard() {
        viewModelScope.launch {
            val clipboard = _uiState.value.clipboardFiles
            if (clipboard.isEmpty()) return@launch

            val isCut = _uiState.value.isCutOperation
            val targetDir = _uiState.value.currentDirectory

            _uiState.value = _uiState.value.copy(
                isOperating = true,
                operationStatus = if (isCut) "Memindahkan file..." else "Menyalin file...",
                operationProgress = 0f
            )

            var successCount = 0
            val total = clipboard.size

            for ((index, sourceFile) in clipboard.withIndex()) {
                val result = if (isCut) {
                    fileRepository.moveFile(sourceFile, targetDir)
                } else {
                    fileRepository.copyFile(sourceFile, targetDir)
                }

                if (result.isSuccess) successCount++
                _uiState.value = _uiState.value.copy(
                    operationProgress = (index + 1).toFloat() / total
                )
            }

            _uiState.value = _uiState.value.copy(
                isOperating = false,
                isCopyingOrMoving = false,
                clipboardFiles = emptyList(),
                message = "Selesai " + (if (isCut) "memindahkan" else "menyalin") + " $successCount dari $total file."
            )
            refreshCurrentDirectory()
        }
    }

    fun cancelCopyOrMove() {
        _uiState.value = _uiState.value.copy(
            isCopyingOrMoving = false,
            clipboardFiles = emptyList()
        )
    }
}
