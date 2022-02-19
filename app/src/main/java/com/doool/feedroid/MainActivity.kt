package com.doool.feedroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doool.feedroid.datasource.local.AppDatabase
import com.doool.feedroid.datasource.local.LibraryEntity
import com.doool.feedroid.datasource.remote.AppRetrofit
import com.doool.feedroid.datasource.repository.LibraryRepository
import com.doool.feedroid.datasource.repository.LibraryRepositoryImpl
import com.doool.feedroid.ui.theme.AndroidFeedTheme
import kotlinx.coroutines.launch


data class LibraryGroup(
    val group: String,
    val items: List<Library>
)

data class Library(
    val library: String,
    val items: List<LibraryEntity>
)

enum class SortType {
    Date, Group
}

class FeedViewModel constructor(private val libraryRepository: LibraryRepository) : ViewModel() {

    val feedGroupList = mutableStateListOf<LibraryGroup>()
    private val sortType = MutableLiveData<SortType>(SortType.Group)

    init {
        viewModelScope.launch {
            libraryRepository.updateLibrary()
        }
    }

    fun load() {
        viewModelScope.launch {
            val libraries = libraryRepository.getAllLibrary()

            feedGroupList.clear()
            feedGroupList.addAll(
//                when (sortType.value) {
//                    SortType.Date -> loadSortedByDate(feed)
//                    SortType.Group -> loadSortedByGroup(feed)
//                    else -> loadSortedByDate(feed)
//                }
                loadSortedByGroup(libraries)
            )
        }
    }

    fun SetSortType(sortType: SortType) {
        this.sortType.postValue(sortType)
        load()
    }

//    private fun loadSortedByDate(feed: Feed): List<LibraryGroup> {
//        return feed.entry.map { entry ->
//            val items = parseReleaseDataFromHtml(entry.content, entry.updated)
//            LibraryGroup(entry.title, items)
//        }
//    }

    enum class VersionState(val order: Int) {
        Release(0), Rc(1), Beta(2), Alpha(3)
    }

    data class Version(val number: Int, val state: VersionState, val code: Int?) :
        Comparable<Version> {

        override fun compareTo(other: Version): Int {
            return if (number == other.number) {
                if (state == other.state) compareValues(other.code, code)
                else compareValues(state.order, other.state.order)
            } else compareValues(other.number, number)
        }
    }

    private fun parseVersion(version: String): Version {
        val number = version.split("-")[0].split(".").joinToString("").toInt()

        val (state, code) = version.split("-").getOrNull(1)?.let { string ->
            val state = VersionState.values().first { string.contains(it.name.lowercase()) }
            val code = string.removePrefix(state.name.lowercase()).toInt()
            Pair(state, code)
        } ?: Pair(VersionState.Release, null)

        return Version(number, state, code)
    }

    private fun loadSortedByGroup(libraries: List<LibraryEntity>): List<LibraryGroup> {
        val library = libraries.groupBy { it.name }.map {
            Library(it.key, it.value.sortedBy { parseVersion(it.version) })
        }

        return library.groupBy { parseGroup(it.library) }.map {
            LibraryGroup(it.key, it.value)
        }
    }

    private fun parseGroup(library: String): String {
        return library.split(" ", "-").first()
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        FeedViewModel(
            LibraryRepositoryImpl(
                AppDatabase.getInstance(application).libraryDao(),
                AppRetrofit.getFeedService()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidFeedTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val feedGroups = viewModel.feedGroupList

                    Column {
                        Row {
                            Button(onClick = { viewModel.SetSortType(SortType.Date) }) {

                            }
                            Button(onClick = { viewModel.SetSortType(SortType.Group) }) {

                            }
                        }

                        LazyColumn {
                            feedGroups.forEach {
                                item {
                                    FeedHeader(title = it.group)
                                }
                                it.items.forEach {
                                    item {
                                        FeedHeader(title = it.library)
                                    }
                                    items(it.items) {
                                        FeedItem(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModel.load()
    }
}

@Composable
private fun FeedHeader(title: String) {
    Spacer(modifier = Modifier.height(10.dp))
    Text(text = title)
    Spacer(modifier = Modifier.height(10.dp))
}


@Composable
private fun FeedItem(releaseData: LibraryEntity) {
//    Text(text = releaseData.group)
//    Text(text = releaseData.name)
    Text(text = releaseData.version)
}