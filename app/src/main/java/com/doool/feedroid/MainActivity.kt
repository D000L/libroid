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
import com.doool.feedroid.service.Feed
import com.doool.feedroid.service.FeedService
import com.doool.feedroid.ui.theme.AndroidFeedTheme
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object AppRetrofit {

    private var instance: Retrofit? = null

    private fun newInstance(): Retrofit {
        instance?.let {
            return it
        } ?: run {
            val newRetrofit = Retrofit.Builder()
                .baseUrl("https://developer.android.com")
                .addConverterFactory(
                    TikXmlConverterFactory.create(
                        TikXml.Builder().exceptionOnUnreadXml(false).build()
                    )
                )
                .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }).build())
                .build()
            instance = newRetrofit
            return newRetrofit
        }
    }

    fun getFeedService(): FeedService {
        return newInstance().create(FeedService::class.java)
    }
}

data class LibraryGroup(
    val group: String,
    val items: List<Library>
)

data class Library(
    val library: String,
    val items: List<ReleaseData>
)

data class ReleaseData(
    val group: String,
    val name: String,
    val version: String,
    val updated: String,
    val url: String,
)

enum class SortType {
    Date, Group
}

class FeedViewModel constructor(private val service: FeedService) : ViewModel() {

    val feedGroupList = mutableStateListOf<LibraryGroup>()
    private val sortType = MutableLiveData<SortType>(SortType.Group)

    fun load() {
        viewModelScope.launch {
            val feed = service.getFeed()

            feedGroupList.clear()
            feedGroupList.addAll(
//                when (sortType.value) {
//                    SortType.Date -> loadSortedByDate(feed)
//                    SortType.Group -> loadSortedByGroup(feed)
//                    else -> loadSortedByDate(feed)
//                }
                loadSortedByGroup(feed)
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

    private fun loadSortedByGroup(feed: Feed): List<LibraryGroup> {
        val all = feed.entry.flatMap { entry ->
            parseReleaseDataFromHtml(entry.content, entry.updated)
        }

        val library = all.groupBy { it.name }.map {
            Library(it.key, it.value)
        }

        return library.groupBy { parseGroup(it.library) }.map {
            LibraryGroup(it.key, it.value)
        }
    }

    private fun parseGroup(library: String): String {
        return  library.split(" ", "-").first()
    }

    private fun parseReleaseDataFromHtml(html: String, updatedDate: String): List<ReleaseData> {
        return html.removeSurrounding("<ul>", "</ul>")
            .split("\n").mapNotNull {
                val feed = Regex("<li><a href=\"(.*)\">(.*)</a>").find(it)

                feed?.let {
                    val item = feed.groupValues[2]

                    val (name, version) = if (item.contains("Version")) {
                        feed.groupValues[2].split("Version")
                    } else {
                        val list = item.split(" ")
                        listOf(list.dropLast(1).reduce { acc, s -> "$acc $s" }, list.last())
                    }

                    val group = parseGroup(name)
                    ReleaseData(group, name, version, updatedDate, feed.groupValues[1])
                }
            }
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel = FeedViewModel(AppRetrofit.getFeedService())

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
                        Row() {
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
                                    items(it.items){
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
private fun FeedItem(releaseData: ReleaseData) {
//    Text(text = releaseData.group)
//    Text(text = releaseData.name)
    Text(text = releaseData.version)
}