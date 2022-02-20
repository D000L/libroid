package com.doool.feedroid.data.datasource.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookMarkPreference constructor(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "bookmark")

    private val LIBRARY_NAMES = stringSetPreferencesKey("library_names")

    val bookmarked: Flow<List<String>> = context.dataStore.data.map {
        it[LIBRARY_NAMES]?.toList() ?: emptyList()
    }

    suspend fun addBookmark(library: String) {
        context.dataStore.edit {
            val current = it[LIBRARY_NAMES].orEmpty()
            it[LIBRARY_NAMES] = current.plus(library)
        }
    }

    suspend fun removeBookmark(library: String) {
        context.dataStore.edit {
            val current = it[LIBRARY_NAMES].orEmpty()
            it[LIBRARY_NAMES] = current.minus(library)
        }
    }
}

