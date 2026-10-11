package com.archix.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archix.app.data.model.ArchiveEntryItem
import com.archix.app.data.model.ArchiveType
import com.archix.app.data.model.FileItem
import com.archix.app.data.repository.ArchiveRepository
import com.archix.app.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class ArchiveFilterTab(val title: String) {
    ALL("Semua"),
    ZIP("ZIP"),
    RAR("RAR"),
    SEVEN_Z("7Z"),
    OTHERS("Lainnya")
}

data class ArchiveUiState(
    val allArchives: List<FileItem> = emptyList(),
    val filteredArchives: List<FileItem> = emptyList(),
    val selectedTab: ArchiveFilterTab = ArchiveFilterTab.ALL,
    val selectedArchive: FileItem? = null,
    val archiveEntries: List<ArchiveEntryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingEntries: Boolean = false,
    val isExtracting: Boolean = false,
    val extractionProgress: Float = 0f,
    val currentExtractingFile: String = "",
    val message: String? = null,
    val error: String? = null
)

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)
    private val archiveRepository = ArchiveRepository()

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadArchives()
    }

    fun loadArchives() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val archives = fileRepository.getArchiveFiles()
                _uiState.value = _uiState.value.copy(
                    allArchives = archives,
                    isLoading = false
                )
                applyTabFilter(_uiState.value.selectedTab)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to scan archive files"
                )
            }
        }
    }

    fun selectTab(tab: ArchiveFilterTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        applyTabFilter(tab)
    }

    private fun applyTabFilter(tab: ArchiveFilterTab) {
        val all = _uiState.value.allArchives
        val filtered = when (tab) {
            ArchiveFilterTab.ALL -> all
            ArchiveFilterTab.ZIP -> all.filter { it.archiveType == ArchiveType.ZIP }
            ArchiveFilterTab.RAR -> all.filter { it.archiveType == ArchiveType.RAR }
            ArchiveFilterTab.SEVEN_Z -> all.filter { it.archiveType == ArchiveType.SEVEN_Z }
            ArchiveFilterTab.OTHERS -> all.filter { it.archiveType == ArchiveType.TAR || it.archiveType == ArchiveType.GZ }
        }
        _uiState.value = _uiState.value.copy(filteredArchives = filtered)
    }

    fun selectArchive(archiveItem: FileItem?) {
        _uiState.value = _uiState.value.copy(selectedArchive = archiveItem)
        if (archiveItem != null) {
            loadArchiveEntries(File(archiveItem.path))
        }
    }

    fun loadArchiveEntries(archiveFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEntries = true, archiveEntries = emptyList())
            val res = archiveRepository.getArchiveContents(archiveFile)
            res.onSuccess { entries ->
                _uiState.value = _uiState.value.copy(
                    isLoadingEntries = false,
                    archiveEntries = entries
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoadingEntries = false,
                    error = "Unable to read archive contents: ${e.message}"
                )
            }
        }
    }

    fun extractHere(archiveItem: FileItem) {
        val archiveFile = File(archiveItem.path)
        val targetDir = archiveFile.parentFile ?: fileRepository.getRootDirectory()
        extractArchiveToDir(archiveFile, targetDir)
    }

    fun extractToFolder(archiveItem: FileItem, targetFolder: File) {
        val archiveFile = File(archiveItem.path)
        val folderName = archiveFile.nameWithoutExtension
        val outputDir = File(targetFolder, folderName)
        extractArchiveToDir(archiveFile, outputDir)
    }

    private fun extractArchiveToDir(archiveFile: File, targetDir: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExtracting = true,
                extractionProgress = 0f,
                currentExtractingFile = archiveFile.name
            )

            val result = archiveRepository.extractArchive(
                archiveFile = archiveFile,
                targetDir = targetDir
            ) { progress, currentName ->
                _uiState.value = _uiState.value.copy(
                    extractionProgress = progress,
                    currentExtractingFile = currentName
                )
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    message = "Extracted successfully to: ${targetDir.name}"
                )
                loadArchives()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    error = "Extraction failed: ${e.message}"
                )
            }
        }
    }

    fun deleteArchive(archiveItem: FileItem) {
        viewModelScope.launch {
            val res = fileRepository.deleteFile(File(archiveItem.path))
            res.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Deleted: ${archiveItem.name}")
                loadArchives()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to delete archive")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
