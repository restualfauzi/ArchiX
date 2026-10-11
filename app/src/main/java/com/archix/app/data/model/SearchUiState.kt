package com.archix.app.data.model

data class SearchUiState(
    val query: String = "",
    val categoryFilter: CategoryType? = null,
    val searchResults: List<FileItem> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)
