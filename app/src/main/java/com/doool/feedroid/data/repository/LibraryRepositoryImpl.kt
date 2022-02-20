package com.doool.feedroid.data.repository

import com.doool.feedroid.data.datasource.local.LibraryDao
import com.doool.feedroid.data.datasource.local.LibraryEntity
import com.doool.feedroid.data.datasource.local.toModel
import com.doool.feedroid.data.datasource.remote.LibraryService
import com.doool.feedroid.domain.model.LibraryModel
import com.doool.feedroid.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LibraryRepositoryImpl constructor(
    private val local: LibraryDao,
    private val remote: LibraryService
) : LibraryRepository {

    override fun getAllLibrary(group: String?, name: String?): Flow<List<LibraryModel>> {
        return (when {
            group != null -> local.getAllByGroup(group)
            name != null -> local.getAllByName(name)
            else -> local.getAll()
        }).map {
            it.map { it.toModel() }
        }
    }

    override suspend fun getAllGroupNames(): List<String> {
        return local.getAllGroupName()
    }

    override suspend fun updateLibrary() {
        try {
            val remoteData = remote.getFeed().entry.flatMap { entry ->
                parseReleaseDataFromHtml(entry.content, entry.updated)
            }

            withContext(Dispatchers.IO) {
                local.insertAll(*remoteData.toTypedArray())
            }
        } catch (ex: Exception) {

        }
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

                    val group = name.split(" ", "-").first()
                    LibraryEntity(group, name, version, updatedDate, feed.groupValues[1])
                }
            }
    }
}