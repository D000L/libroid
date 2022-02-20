package com.doool.feedroid.data.repository

import com.doool.feedroid.data.datasource.local.BookMarkPreference
import com.doool.feedroid.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class BookmarkRepositoryImpl constructor(private val bookMarkPreference: BookMarkPreference) :
    BookmarkRepository {

    override fun getBookmarkList(): Flow<List<String>> {
        return bookMarkPreference.bookmarked
    }

    override suspend fun addBookmark(library: String) {
        bookMarkPreference.addBookmark(library)
    }

    override suspend fun removeBookmark(library: String) {
        bookMarkPreference.removeBookmark(library)
    }
}