package com.archix.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archix.app.data.local.DataStoreManager
import com.archix.app.data.model.FileItem
import com.archix.app.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

data class MoreUiState(
    val favoriteFiles: List<FileItem> = emptyList(),
    val recentFiles: List<FileItem> = emptyList(),
    val largeFiles: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MoreViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)
    private val dataStoreManager = DataStoreManager(application)

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
        loadMoreData()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            dataStoreManager.favoritesFlow.collectLatest { favoritePaths ->
                val favoriteItems = favoritePaths.mapNotNull { path ->
                    val file = File(path)
                    if (file.exists()) {
                        FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = if (file.isDirectory) 0L else file.length(),
                            lastModified = file.lastModified(),
                            extension = file.extension.lowercase(),
                            isFavorite = true
                        )
                    } else null
                }
                _uiState.value = _uiState.value.copy(favoriteFiles = favoriteItems)
            }
        }
    }

    fun loadMoreData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val recents = fileRepository.getRecentFiles()
                val minSize = 50 * 1024 * 1024L // 50MB
                val lrg = fileRepository.getLargeFiles(minSize)

                _uiState.value = _uiState.value.copy(
                    recentFiles = recents,
                    largeFiles = lrg,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load files"
                )
            }
        }
    }

    fun toggleFavorite(fileItem: FileItem) {
        viewModelScope.launch {
            dataStoreManager.toggleFavorite(fileItem.path)
        }
    }
}
