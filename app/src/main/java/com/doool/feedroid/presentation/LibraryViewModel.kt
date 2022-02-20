package com.doool.feedroid.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doool.feedroid.domain.model.LibraryModel
import com.doool.feedroid.domain.model.VersionState
import com.doool.feedroid.domain.repository.BookmarkRepository
import com.doool.feedroid.domain.repository.LibraryRepository
import com.doool.feedroid.presentation.item.Library
import com.doool.feedroid.presentation.item.LibraryGroup
import com.doool.feedroid.presentation.item.LibraryType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LibraryViewModel constructor(
    private val libraryRepository: LibraryRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    val groupList = mutableStateListOf<LibraryType>()

    init {
        viewModelScope.launch {
            libraryRepository.updateLibrary()
        }
    }

    fun addBookmark(library: String) {
        viewModelScope.launch { bookmarkRepository.addBookmark(library) }
    }

    fun removeBookmark(library: String) {
        viewModelScope.launch { bookmarkRepository.removeBookmark(library) }
    }

    fun load() {
        viewModelScope.launch {
            groupList.clear()

            libraryRepository.getAllLibrary().map {
                loadSortedByGroup(it).map { LibraryType.Item(it) }
            }.combine(bookmarkRepository.getBookmarkList()) { items, bookmarkList ->
                items.map {
                    it.apply { data.bookmarked = bookmarkList.contains(data.group) }
                }
            }.collectLatest {
                groupList.clear()

                val grouping = it.groupBy { it.data.bookmarked }

                groupList.addAll(grouping[true] ?: emptyList())
                if (groupList.isNotEmpty()) groupList.add(LibraryType.Divider)
                groupList.addAll(grouping[false] ?: emptyList())
            }
        }
    }

    fun loadLibraryHistory(name: String): SnapshotStateList<LibraryModel> {
        val item = mutableStateListOf<LibraryModel>()
        viewModelScope.launch {
            libraryRepository.getAllLibrary(name = name).collectLatest {
                item.addAll(it.sortedBy { it.version })
            }

        }
        return item
    }

    private fun loadSortedByGroup(libraries: List<LibraryModel>): List<LibraryGroup> {
        return libraries.groupBy { it.group }.map {
            val library = it.value.groupBy { it.name }.map {
                val release = it.value.filter { it.version.state == VersionState.Release }
                    .minByOrNull { it.version }?.version

                val latest = it.value.minByOrNull { it.version }?.version

                Library(it.key, release, latest)
            }

            LibraryGroup(it.key, library)
        }
    }
}