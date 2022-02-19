package com.doool.feedroid.datasource.repository

import com.doool.feedroid.Library
import com.doool.feedroid.datasource.local.LibraryEntity

interface LibraryRepository {

    suspend fun getAllLibrary() : List<LibraryEntity>
    suspend fun updateLibrary()
}