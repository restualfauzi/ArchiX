package com.archix.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archix.app.data.model.CategoryType
import com.archix.app.data.model.SearchUiState
import com.archix.app.data.repository.FileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        triggerSearch()
    }

    fun onCategoryFilterChanged(category: CategoryType?) {
        val next = if (_uiState.value.categoryFilter == category) null else category
        _uiState.value = _uiState.value.copy(categoryFilter = next)
        triggerSearch()
    }

    private fun triggerSearch() {
        searchJob?.cancel()
        val q = _uiState.value.query
        val cat = _uiState.value.categoryFilter

        if (q.isBlank() && cat == null) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce typing
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val results = fileRepository.searchFiles(q, cat)
                _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, error = e.localizedMessage)
            }
        }
    }

    fun clearSearch() {
        _uiState.value = SearchUiState()
    }
}
