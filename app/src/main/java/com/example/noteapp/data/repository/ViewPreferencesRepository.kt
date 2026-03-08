package com.example.noteapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ViewPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
    }

    val isGridView: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_GRID_VIEW] ?: false
    }

    suspend fun setGridView(isGrid: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_GRID_VIEW] = isGrid
        }
    }
}

val Context.dataStore by preferencesDataStore(name = "view_preferences")