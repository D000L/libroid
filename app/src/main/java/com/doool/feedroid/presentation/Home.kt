package com.doool.feedroid.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.doool.feedroid.domain.model.Version

@Composable
fun Home(viewModel: LibraryViewModel, navController: NavController) {
    val group = viewModel.groupList

    LibraryList(group, onClickBookmark = { library, bookmarked ->
        if (!bookmarked) {
            viewModel.addBookmark(library)
        } else {
            viewModel.removeBookmark(library)
        }
    }) {
        navController.navigate("history/$it")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryList(
    groups: SnapshotStateList<LibraryType>,
    onClickBookmark: (String, Boolean) -> Unit,
    onClickLibrary: (String) -> Unit
) {
    LazyColumn() {
        items(groups, key = {
            when (it) {
                LibraryType.Divider -> "DIVIDER"
                is LibraryType.Item -> it.data.group
            }
        }) {
            when (it) {
                LibraryType.Divider -> Divider(modifier = Modifier.animateItemPlacement())
                is LibraryType.Item -> GroupItem(
                    Modifier.animateItemPlacement(),
                    it.data,
                    onClickBookmark,
                    onClickLibrary
                )
            }
        }
    }
}

@Composable
private fun GroupItem(
    modifier: Modifier = Modifier,
    group: LibraryGroup,
    onClickBookmark: (String, Boolean) -> Unit,
    onClickLibrary: (String) -> Unit
) {
    GroupHeader(modifier = modifier, group = group) {
        onClickBookmark(group.group, group.bookmarked)
        group.opened = false
    }
    AnimatedVisibility(visible = group.opened, enter = fadeIn() + slideIn {
        IntOffset(0, -it.height / 2)
    }, exit = shrinkOut(targetSize = {
        IntSize(it.width, 0)
    }) + fadeOut()) {
        Column {
            group.items.forEach { LibraryItem(it) { onClickLibrary(it.library) } }
        }
    }
}

@Composable
private fun GroupHeader(
    modifier: Modifier = Modifier,
    group: LibraryGroup,
    onClickLibrary: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable { group.opened = !group.opened }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onClickLibrary() }) {
            Icon(
                imageVector = Icons.Default.Star, contentDescription = null,
                tint = if (group.bookmarked) Color.Blue else Color.Gray
            )
        }
        Text(group.group, style = MaterialTheme.typography.h3)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { group.opened = !group.opened }) {
            Icon(
                imageVector = if (group.opened) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun LibraryItem(library: Library, onClickLibrary: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Text(text = library.library, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(4.dp))
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
