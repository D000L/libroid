package com.doool.feedroid.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.doool.feedroid.domain.model.Version

@Composable
fun Home(viewModel: LibraryViewModel, navController: NavController) {
    val groups = viewModel.group

    LibraryList(groups) {
        navController.navigate("history/$it")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryList(
    groups: SnapshotStateList<LibraryGroup>,
    onClickLibrary: (String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp)) {
        groups.forEach {
            item {
                Row(
                    modifier = Modifier.clickable { it.isOpen = !it.isOpen },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it.group, style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { it.isOpen = !it.isOpen }) {
                        Icon(
                            imageVector = if (it.isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }
            if (it.isOpen) {
                items(it.items) { LibraryItem(it) { onClickLibrary(it.library) } }
            }
        }
    }
}

@Composable
private fun LibraryItem(library: Library, onClickLibrary: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = library.library, style = MaterialTheme.typography.body1)
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VersionItem("Release", library.releaseVersion)
            VersionItem("Latest", library.latestVersion)
            IconButton(onClick = onClickLibrary) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null)
            }
        }
    }
}

@Composable
private fun VersionItem(title: String, version: Version?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            Modifier
                .border(0.5.dp, Color.Black)
                .padding(4.dp)
        ) {
            val string = version?.let { it.toString() } ?: "No Data"
            Text(text = string, style = MaterialTheme.typography.body1)
        }
    }
}
