package com.archix.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archix.app.data.model.CategoryType
import com.archix.app.data.model.FileItem
import com.archix.app.data.model.StorageInfo
import com.archix.app.data.repository.FileRepository
import com.archix.app.utils.StorageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val storageInfo: StorageInfo? = null,
    val categoryCounts: Map<CategoryType, Pair<Int, Long>> = emptyMap(),
    val primaryFolders: List<FileItem> = emptyList(),
    val recentFiles: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val storage = StorageUtils.getStorageInfo(getApplication())
                val folders = StorageUtils.getPrimaryFolders()
                val counts = StorageUtils.getCategoryCounts(getApplication())
                val recents = fileRepository.getRecentFiles().take(10)

                _uiState.value = HomeUiState(
                    storageInfo = storage,
                    categoryCounts = counts,
                    primaryFolders = folders,
                    recentFiles = recents,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load home data"
                )
            }
        }
    }
}
