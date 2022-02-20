package com.doool.feedroid.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.doool.feedroid.data.datasource.local.AppDatabase
import com.doool.feedroid.data.datasource.local.BookMarkPreference
import com.doool.feedroid.data.datasource.remote.AppRetrofit
import com.doool.feedroid.data.repository.BookmarkRepositoryImpl
import com.doool.feedroid.data.repository.LibraryRepositoryImpl
import com.doool.feedroid.domain.model.LibraryModel
import com.doool.feedroid.domain.model.Version
import com.doool.feedroid.domain.model.VersionState
import com.doool.feedroid.domain.repository.BookmarkRepository
import com.doool.feedroid.domain.repository.LibraryRepository
import com.doool.feedroid.presentation.ui.theme.AndroidFeedTheme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


data class LibraryGroup(
    val group: String = "",
    val items: List<Library> = emptyList(),
) {
    var bookmarked by mutableStateOf(false)
    var opened by mutableStateOf(false)
}

data class Library(
    val library: String,
    val releaseVersion: Version?,
    val latestVersion: Version?
)

sealed class LibraryType {
    data class Item(val data: LibraryGroup) : LibraryType()
    object Divider : LibraryType()
}


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

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        LibraryViewModel(
            LibraryRepositoryImpl(
                AppDatabase.getInstance(application).libraryDao(),
                AppRetrofit.getFeedService()
            ),
            BookmarkRepositoryImpl(
                BookMarkPreference(applicationContext)
            )
        )
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidFeedTheme {
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                val navController = rememberNavController(bottomSheetNavigator)

                ModalBottomSheetLayout(
                    bottomSheetNavigator,
                    sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Scaffold(topBar = {
                        AppBar()
                    }) {
                        MainNavHost(this, navController, viewModel)
                    }
                }
            }
        }

        viewModel.load()
    }
}

@Composable
private fun AppBar() {
    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = "Android Library List",
            style = MaterialTheme.typography.h1
        )
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
private fun MainNavHost(
    activity: MainActivity,
    navController: NavHostController,
    viewModel: LibraryViewModel
) {
    NavHost(navController = navController, "home") {
        composable("home") {
            Home(viewModel, navController)
        }
        bottomSheet(
            "history/{library}",
            arguments = listOf(navArgument("library") {
                type = NavType.StringType
            })
        ) {
            val library = it.arguments?.getString("library") ?: ""

            History(viewModel, library) { url ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
                activity.startActivity(browserIntent)
            }
        }
    }
}