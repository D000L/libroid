package com.doool.libroid.domain.repository

import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getBookmarkList(): Flow<List<String>>
    suspend fun addBookmark(library: String)
    suspend fun removeBookmark(library: String)
}