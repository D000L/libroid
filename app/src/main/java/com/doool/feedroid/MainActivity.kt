package com.doool.feedroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class FeedGroup(
    val title: String,
    val updated: String,
    val items: List<FeedModel>
)

data class FeedModel(
    val name: String,
    val url: String,
)

class FeedViewModel constructor(private val service: FeedService) : ViewModel() {

    val feedGroupList = mutableStateListOf<FeedGroup>()

    fun load() {
        feedGroupList
        viewModelScope.launch {
            val feed = service.getFeed()

            feedGroupList.addAll(feed.entry.map {
                val items = it.content.removeSurrounding("<ul>", "</ul>")
                    .split("\n").mapNotNull {
                        val feed = Regex("<li><a href=\"(.*)\">(.*)</a>").find(it)
                        feed?.let { FeedModel(feed.groupValues[2], feed.groupValues[1]) }
                    }
                FeedGroup(it.title, it.updated, items)
            })
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

                    LazyColumn {
                        feedGroups.forEach {
                            item {
                                FeedHeader(title = it.title)
                            }
                            items(it.items) {
                                FeedItem(feedModel = it)
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
private fun FeedItem(feedModel: FeedModel) {
    Text(text = feedModel.name)
}