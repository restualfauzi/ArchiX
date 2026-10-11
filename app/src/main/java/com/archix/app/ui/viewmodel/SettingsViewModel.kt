package com.archix.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archix.app.data.local.DataStoreManager
import com.archix.app.data.model.AppLanguage
import com.archix.app.data.model.AppSettings
import com.archix.app.data.model.SortType
import com.archix.app.data.model.ViewMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    val settingsState: StateFlow<AppSettings> = dataStoreManager.appSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDarkMode(enabled)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            dataStoreManager.setLanguage(language)
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            dataStoreManager.setViewMode(viewMode)
        }
    }

    fun setSortType(sortType: SortType) {
        viewModelScope.launch {
            dataStoreManager.setSortType(sortType)
        }
    }

    fun setConfirmDelete(confirm: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setConfirmDelete(confirm)
        }
    }
}
