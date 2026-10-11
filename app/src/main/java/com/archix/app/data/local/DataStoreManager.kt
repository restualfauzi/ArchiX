package com.archix.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.archix.app.data.model.AppLanguage
import com.archix.app.data.model.AppSettings
import com.archix.app.data.model.SortType
import com.archix.app.data.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "archix_preferences")

class DataStoreManager(private val context: Context) {

    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        private val VIEW_MODE = stringPreferencesKey("view_mode")
        private val SORT_TYPE = stringPreferencesKey("sort_type")
        private val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        private val FAVORITE_PATHS = stringSetPreferencesKey("favorite_paths")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            isDarkMode = preferences[IS_DARK_MODE] ?: false,
            language = if (preferences[LANGUAGE] == "en") AppLanguage.ENGLISH else AppLanguage.INDONESIAN,
            defaultViewMode = if (preferences[VIEW_MODE] == "GRID") ViewMode.GRID else ViewMode.LIST,
            defaultSortType = SortType.valueOf(preferences[SORT_TYPE] ?: SortType.NAME.name),
            confirmDelete = preferences[CONFIRM_DELETE] ?: true
        )
    }

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_PATHS] ?: emptySet()
    }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDarkMode
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language.code
        }
    }

    suspend fun setViewMode(viewMode: ViewMode) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE] = viewMode.name
        }
    }

    suspend fun setSortType(sortType: SortType) {
        context.dataStore.edit { preferences ->
            preferences[SORT_TYPE] = sortType.name
        }
    }

    suspend fun setConfirmDelete(confirmDelete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CONFIRM_DELETE] = confirmDelete
        }
    }

    suspend fun toggleFavorite(path: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_PATHS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(path)) {
                current.remove(path)
            } else {
                current.add(path)
            }
            preferences[FAVORITE_PATHS] = current
        }
    }
}
