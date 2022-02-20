package com.doool.feedroid.domain.repository

import com.doool.feedroid.domain.model.LibraryModel

interface LibraryRepository {

    suspend fun getAllLibrary(group: String? = null, name: String? = null): List<LibraryModel>
    suspend fun getAllGroupNames():List<String>
    suspend fun updateLibrary()
}