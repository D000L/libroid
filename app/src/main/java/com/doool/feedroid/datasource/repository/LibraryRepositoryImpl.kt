package com.doool.feedroid.datasource.repository

import com.doool.feedroid.datasource.local.LibraryDao
import com.doool.feedroid.datasource.local.LibraryEntity
import com.doool.feedroid.datasource.remote.LibraryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepositoryImpl constructor(
    private val local: LibraryDao,
    private val remote: LibraryService
) : LibraryRepository {

    override suspend fun getAllLibrary(): List<LibraryEntity> {
        return local.getAll()
    }

    override suspend fun updateLibrary() {
        val remoteData = remote.getFeed().entry.flatMap { entry ->
            parseReleaseDataFromHtml(entry.content, entry.updated)
        }

        withContext(Dispatchers.IO) {
            local.insertAll(*remoteData.toTypedArray())
        }
    }

    private fun parseGroup(library: String): String {
        return library.split(" ", "-").first()
    }

    private fun parseReleaseDataFromHtml(html: String, updatedDate: String): List<LibraryEntity> {
        return html.removeSurrounding("<ul>", "</ul>")
            .split("\n").mapNotNull {
                val feed = Regex("<li><a href=\"(.*)\">(.*)</a>").find(it)

                feed?.let {
                    val item = feed.groupValues[2]

                    val (name, version) = if (item.contains(" Version ")) {
                        feed.groupValues[2].split(" Version ")
                    } else {
                        val list = item.split(" ")
                        listOf(list.dropLast(1).reduce { acc, s -> "$acc $s" }, list.last())
                    }

                    val group = parseGroup(name)
                    LibraryEntity(group, name, version, updatedDate, feed.groupValues[1])
                }
            }
    }
}