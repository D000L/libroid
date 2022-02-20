package com.doool.feedroid.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doool.feedroid.domain.model.LibraryModel
import com.doool.feedroid.domain.model.Version
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun History(viewModel: LibraryViewModel, library: String, onClickItem: (String) -> Unit) {
    Box(
        Modifier.padding(top = 16.dp)
    ) {
        val history = viewModel.loadLibraryHistory(library)
        Column() {
            HistoryHeader(library)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
                items(history) {
                    HistoryItem(it) { onClickItem(it.url) }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(title: String) {
    Text(
        modifier = Modifier.padding(horizontal = 20.dp),
        text = title,
        style = MaterialTheme.typography.h2
    )
}

@Composable
private fun HistoryItem(history: LibraryModel, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .height(28.dp)
                .padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            DateText(history.updated)
            Spacer(modifier = Modifier.weight(1f))
            VersionText(history.version)
            IconButton(onClick = onClick) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
        Divider(thickness = 0.5.dp)
    }

}

@Composable
private fun DateText(updated: String) {
    val dateString = remember(updated) {
        derivedStateOf {
            LocalDate.parse(updated, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString()
        }
    }
    Text(text = dateString.value, style = MaterialTheme.typography.h4)
}

@Composable
private fun VersionText(version: Version) {
    Text(text = version.toString(), style = MaterialTheme.typography.h3)
}
