package com.doool.libroid.presentation

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.doool.libroid.data.datasource.local.AppDatabase
import com.doool.libroid.data.datasource.local.BookMarkPreference
import com.doool.libroid.data.datasource.remote.AppRetrofit
import com.doool.libroid.data.repository.BookmarkRepositoryImpl
import com.doool.libroid.data.repository.LibraryRepositoryImpl
import com.doool.libroid.presentation.ui.theme.AndroidFeedTheme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

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