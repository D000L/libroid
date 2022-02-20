package com.doool.libroid.domain.repository

import com.doool.libroid.domain.model.LibraryModel
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    fun getAllLibrary(group: String? = null, name: String? = null): Flow<List<LibraryModel>>
    suspend fun getAllGroupNames(): List<String>
    suspend fun updateLibrary()
}
