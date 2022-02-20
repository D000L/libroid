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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.doool.feedroid.data.datasource.local.AppDatabase
import com.doool.feedroid.data.datasource.remote.AppRetrofit
import com.doool.feedroid.data.repository.LibraryRepositoryImpl
import com.doool.feedroid.domain.model.LibraryModel
import com.doool.feedroid.domain.model.Version
import com.doool.feedroid.domain.model.VersionState
import com.doool.feedroid.domain.repository.LibraryRepository
import com.doool.feedroid.presentation.ui.theme.AndroidFeedTheme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.launch


data class LibraryGroup(
    val group: String = "",
    val items: List<Library> = emptyList(),
) {
    var isOpen by mutableStateOf(false)
}

data class Library(
    val library: String,
    val releaseVersion: Version?,
    val latestVersion: Version?
)

enum class SortType {
    Date, Group
}

class LibraryViewModel constructor(private val libraryRepository: LibraryRepository) : ViewModel() {

    val group = mutableStateListOf<LibraryGroup>()

    private val sortType = MutableLiveData<SortType>(SortType.Group)

    init {
        viewModelScope.launch {
            libraryRepository.updateLibrary()
        }
    }

    fun load() {
        viewModelScope.launch {
            group.clear()
            group.addAll(loadSortedByGroup(libraryRepository.getAllLibrary()))
        }
    }

    fun loadLibraryHistory(name: String): SnapshotStateList<LibraryModel> {
        val item = mutableStateListOf<LibraryModel>()
        viewModelScope.launch {
            item.addAll(libraryRepository.getAllLibrary(name = name).sortedBy { it.version })
        }
        return item
    }

    fun loadGroup(group: String): MutableState<LibraryGroup> {
        val item = mutableStateOf<LibraryGroup>(LibraryGroup())
        viewModelScope.launch {
            val libraries = libraryRepository.getAllLibrary(group = group)

            item.value = loadSortedByGroup(libraries).first()
        }
        return item
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


    private fun loadSortedByGroup(libraries: List<LibraryModel>): List<LibraryGroup> {
        return libraries.groupBy { it.group }.map {
            val library = it.value.groupBy { it.name }.map {
                val release = it.value.filter { it.version.state == VersionState.Release }
                    .sortedBy { it.version }.firstOrNull()?.version

                val latest = it.value.sortedBy { it.version }.firstOrNull()?.version

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
            )
        )
    }

    @OptIn(
        ExperimentalMaterialNavigationApi::class,
        ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidFeedTheme {
                // A surface container using the 'background' color from the theme
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                val navController = rememberNavController(bottomSheetNavigator)

                ModalBottomSheetLayout(
                    bottomSheetNavigator,
                    sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Scaffold(topBar = {
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
                    }) {
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

                                History(viewModel, library){ url->
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(url)
                                    )
                                    startActivity(browserIntent)
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