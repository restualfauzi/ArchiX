package com.archix.app.data.model

enum class AppLanguage(val code: String, val displayName: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    ENGLISH("en", "English")
}

data class AppSettings(
    val isDarkMode: Boolean = false,
    val language: AppLanguage = AppLanguage.INDONESIAN,
    val defaultViewMode: ViewMode = ViewMode.LIST,
    val defaultSortType: SortType = SortType.NAME,
    val confirmDelete: Boolean = true
)
