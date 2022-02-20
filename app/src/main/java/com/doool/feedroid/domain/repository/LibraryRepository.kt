package com.doool.feedroid.domain.repository

import com.doool.feedroid.domain.model.LibraryModel
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    fun getAllLibrary(group: String? = null, name: String? = null): Flow<List<LibraryModel>>
    suspend fun getAllGroupNames(): List<String>
    suspend fun updateLibrary()
}
